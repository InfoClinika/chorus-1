package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.view.BillingFileView;
import com.infoclinika.mssharing.model.internal.entity.view.BillingUserFunctionsView;
import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.AnalyzableStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.DailyAnalyzableStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.HourlyAnalyzableStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyAnalyseStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.HourlyAnalyseStorageUsageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Elena Kurilina
 */

@Service("analysableStorageLogHelper")
public class AnalysableStorageLogHelper extends AbstractStorageLogHelper {

    private final int executorMaxThreads;
    private final int dailyMaxThreads;
    private final DailyAnalyseStorageUsageRepository dailyUsageRepository;
    private final HourlyAnalyseStorageUsageRepository hourlyUsageRepository;

    @Inject
    public AnalysableStorageLogHelper(DailyAnalyseStorageUsageRepository dailyUsageRepository,
                                      HourlyAnalyseStorageUsageRepository hourlyUsageRepository,
                                      BillingPropertiesProvider billingPropertiesProvider) {
        this.dailyUsageRepository = dailyUsageRepository;
        this.hourlyUsageRepository = hourlyUsageRepository;
        this.executorMaxThreads = billingPropertiesProvider.getHourlyMaxAnalyzableThreads();
        this.dailyMaxThreads = billingPropertiesProvider.getDailyMaxAnalyzableThreads();
    }

    @Override
    protected Function<Lab, Page<ChargeableItemUsage>> sumLogsFn(final long day, final Pageable page) {
        return lab -> {
            logger.debug("Daily summing active storage usages for lab {" + lab.getId() + "} ...");

            final Page<GroupedStorageUsage<HourlyAnalyzableStorageUsage>> storageUsagePage =
                hourlyUsageRepository.groupedNotCompressedUsagesByFile(lab.getId(), day, page);

            final Function<GroupedStorageUsage<HourlyAnalyzableStorageUsage>, ChargeableItemUsage> function =
                processSumLabStorageUsageFn(day);
            final ImmutableList<ChargeableItemUsage> chargeableItemUsages =
                from(storageUsagePage).transform(function).toList();
            return new PageImpl<>(chargeableItemUsages, page, storageUsagePage.getTotalElements());

        };
    }

    private <T extends AnalyzableStorageUsage> Function<GroupedStorageUsage<T>,
        ChargeableItemUsage> processSumLabStorageUsageFn(final long day) {
        return storageUsage -> {

            final AnalyzableStorageUsage usage = storageUsage.itemUsage;
            //final Long hours = usageRepository.sumNotCompressedHoursForFile(file);

            final Date timestamp = new Date(storageUsage.itemUsage.getTimestamp());

            final AnalyzableStorageUsage groupedUsage = new DailyAnalyzableStorageUsage(usage.getLab(),
                usage.getUser(), usage.getFile(), usage.getBytes(), timestamp,
                usage.getUsedBy(), usage.getInstrument(), storageUsage.summedCharge, usage.getFileName()
            );

            groupedUsage.setHours(storageUsage.hours.intValue());
            groupedUsage.setCompressed(true);
            groupedUsage.setBalance(storageUsage.itemUsage.getBalance());
            groupedUsage.setScaledToPayValue(storageUsage.itemUsage.getScaledToPayValue());
            groupedUsage.setDay(day);

            return groupedUsage;
        };
    }

    @Override
    public void log(long logInterval) {

        final ImmutableMap<String, S3ObjectSummary> rawFiles =
            readFiles(storedObjectPaths.getRawFilesBucket(), storedObjectPaths.getRawFilesPrefix());
        final int totalFilesSize = rawFiles.size();
        logger.debug(totalFilesSize + " raw files was found on S3");
        final ExecutorService executorService = Executors.newFixedThreadPool(executorMaxThreads);

        logger.debug(" *** Logging started ***");

        performLogging(logInterval, rawFiles, totalFilesSize, executorService);

        logger.debug("Logging translation data of archived files");

        final List<Future<Iterable<HourlyAnalyzableStorageUsage>>> futures =
            invokeAll(executorService, buildAllTranslationDataOfArchivedFilesLogTasks(logInterval));
        saveUsagesAndPostProcessTasks(hourlyUsageRepository, futures, new Date());

        logger.debug("*** Logging ended ***");
        executorService.shutdownNow();

    }

    private void performLogging(long logInterval, ImmutableMap<String, S3ObjectSummary> rawFiles, int totalFilesSize,
                                ExecutorService executorService) {
        final int filesLogOperationCount = 1000;

        Map<String, S3ObjectSummary> filesMap = new HashMap<>(filesLogOperationCount);
        int counter = 0;

        for (Map.Entry<String, S3ObjectSummary> summaryEntry : rawFiles.entrySet()) {

            filesMap.put(summaryEntry.getKey(), summaryEntry.getValue());
            counter++;

            if ((counter % filesLogOperationCount == 0) || (counter == (totalFilesSize))) {

                logFiles(logInterval, totalFilesSize, executorService, filesMap, counter);

                filesMap = new HashMap<>(filesLogOperationCount);

            }
        }
    }

    private HourlyAnalyzableStorageUsage performLogging(S3ObjectSummary objectSummary, Date now, BillingFileView file,
                                                        long logInterval, Long lab) {

        final HourlyAnalyzableStorageUsage entry = newStorageUsage(now, file, objectSummary.getSize(), lab);

        handleMissedLogs(now, file, logInterval, onMissedFoundFn(entry));

        return entry;
    }

    private void logFiles(long logInterval, int totalFilesSize, ExecutorService executorService,
                          Map<String, S3ObjectSummary> filesMap, int counter) {

        final Builder<Callable<Iterable<HourlyAnalyzableStorageUsage>>> builder = ImmutableSet.builder();

        logger.debug("** Building log tasks... Files to build: " + counter + " of " + totalFilesSize);

        builder.addAll(buildAllNotArchivedFilesLogTasks(filesMap, logInterval));

        Date filesLoggedTime = new Date();

        logger.debug(
            "** Executing active files storage usage charging tasks for files... Files to execute: " + counter +
                " of " + totalFilesSize);

        final List<Future<Iterable<HourlyAnalyzableStorageUsage>>> futures =
            invokeAll(executorService, builder.build());

        saveUsagesAndPostProcessTasks(hourlyUsageRepository, futures, filesLoggedTime);

        logger.debug(" Logging ended for files " + counter + " of " + totalFilesSize);

    }

    private ImmutableSet<Callable<Iterable<HourlyAnalyzableStorageUsage>>>
        buildAllTranslationDataOfArchivedFilesLogTasks(final long logInterval) {

        final List<BillingFileView> withTranslationData = fileViewRepository.findAllArchivedWithTranslationData();
        logger.debug("*** Charging archived files with translation data: " + withTranslationData.size());

        final Builder<Callable<Iterable<HourlyAnalyzableStorageUsage>>> builder = ImmutableSet.builder();

        for (final BillingFileView metaData : withTranslationData) {
            builder.add(() -> logTranslationDataOnly(metaData, logInterval));
        }
        return builder.build();
    }

    private ImmutableSet<HourlyAnalyzableStorageUsage> logTranslationDataOnly(BillingFileView metaData,
                                                                              long logInterval) {

        final Set<BillingUserFunctionsView> usersFunctions = metaData.getUserFunctionsViews();
        final Builder<HourlyAnalyzableStorageUsage> builder = ImmutableSet.builder();

        final HashMap<Long, Set<BillingUserFunctionsView>> functionsPerUser = buildFunctionsPerUser(usersFunctions);

        for (Long user : functionsPerUser.keySet()) {
            doLogTranslationData(new Date(), metaData, functionsPerUser.get(user), logInterval)
                .ifPresent(builder::add);
        }

        return builder.build();
    }

    private HashMap<Long, Set<BillingUserFunctionsView>> buildFunctionsPerUser(
        Iterable<BillingUserFunctionsView> usersFunctions) {
        final HashMap<Long, Set<BillingUserFunctionsView>> hashMap = newHashMap();

        for (BillingUserFunctionsView usersFunction : usersFunctions) {
            Set<BillingUserFunctionsView> views = hashMap.get(usersFunction.getUserId());
            if (views == null) {
                views = newHashSet();
                hashMap.put(usersFunction.getUserId(), views);
            }
            views.add(usersFunction);
        }
        return hashMap;
    }

    private Set<Callable<Iterable<HourlyAnalyzableStorageUsage>>> buildAllNotArchivedFilesLogTasks(
        final Map<String, S3ObjectSummary> rawFiles, final long logInterval) {

        final List<BillingFileView> allByContentId = rawFiles.keySet().isEmpty() ? Collections.emptyList() :
                fileViewRepository.findByContentId(rawFiles.keySet());
        logger.debug("*** Active files count found in DB: " + allByContentId.size());

        return allByContentId
            .stream()
            .filter(fileView -> rawFiles.get(fileView.getContentId()) != null)
            .map((fileView -> (Callable<Iterable<HourlyAnalyzableStorageUsage>>)
                () -> logMetaFile(fileView, rawFiles.get(fileView.getContentId()), logInterval)))
            .collect(Collectors.toSet());

    }

    @Override
    public void sumLogs(Date dateDayToLog) {
        doSumLogs(hourlyUsageRepository, dailyUsageRepository, dailyMaxThreads, dateDayToLog);
    }

    private ImmutableSet<HourlyAnalyzableStorageUsage> logMetaFile(BillingFileView fileMetaData,
                                                                   S3ObjectSummary objectSummary, long logInterval) {
        final Date now = new Date();
        logger.trace("Logging file by path: {" + objectSummary.getKey() + "}");

        final Builder<HourlyAnalyzableStorageUsage> builder = ImmutableSet.builder();

        if (featuresHelper.isEnabledForLab(ApplicationFeature.BILLING, fileMetaData.getInstrumentLab())) {
            builder.add(logFileUsagesForOwner(objectSummary, now, fileMetaData, logInterval));
        }

        final Builder<HourlyAnalyzableStorageUsage> usageBuilder =
            newTranslationStorageUsageForOtherUsersAndLabs(now, fileMetaData, logInterval);
        return builder.addAll(usageBuilder.build()).build();
    }

    private HourlyAnalyzableStorageUsage logFileUsagesForOwner(S3ObjectSummary objectSummary, final Date now,
                                                               BillingFileView file, long logInterval) {
        final Long labToBill = fromNullable(file.getBillLab()).or(file.getInstrumentLab());

        return performLogging(objectSummary, now, file, logInterval, labToBill);
    }

    private Builder<HourlyAnalyzableStorageUsage> newTranslationStorageUsageForOtherUsersAndLabs(Date now,
                                                                                                 BillingFileView file,
                                                                                                 long logInterval) {

        final Iterable<BillingUserFunctionsView> usersFunctions =
            filter(file.getUserFunctionsViews(), not(translationDataForUser(file.getOwner())));
        final Builder<HourlyAnalyzableStorageUsage> builder = ImmutableSet.builder();
        final HashMap<Long, Set<BillingUserFunctionsView>> functionsPerUser = buildFunctionsPerUser(usersFunctions);

        for (Long user : functionsPerUser.keySet()) {
            doLogTranslationData(now, file, functionsPerUser.get(user), logInterval).ifPresent(builder::add);
        }

        return builder;
    }

    private Optional<HourlyAnalyzableStorageUsage> doLogTranslationData(Date now, BillingFileView file,
                                                                        Set<BillingUserFunctionsView> usersFunction,
                                                                        long logInterval) {
        final BillingUserFunctionsView sampleUserFunctionView = usersFunction.iterator().next();
        final Long lab = sampleUserFunctionView.getLab();

        if (!featuresHelper.isEnabledForLab(ApplicationFeature.BILLING, lab)) {
            logger.debug("** Skipping log usage of translation data for lab: " + lab);
            return Optional.empty();
        }

        long totalBytesOfUsageByUser = sumFunctionsDataSize(usersFunction);

        return Optional.of((logStorageForTranslationData(now, file, sampleUserFunctionView, totalBytesOfUsageByUser,
            logInterval
        )));
    }

    private long sumFunctionsDataSize(Iterable<BillingUserFunctionsView> functions) {

        long totalBytesOfUsageByUser = 0;

        for (BillingUserFunctionsView msFunctionItem : functions) {
            totalBytesOfUsageByUser += evaluateMSFunctionUsageTotalBytes(msFunctionItem);
        }

        return totalBytesOfUsageByUser;
    }

    private HourlyAnalyzableStorageUsage logStorageForTranslationData(Date now, BillingFileView file,
                                                                      BillingUserFunctionsView sampleUserFunctionView,
                                                                      long totalBytesOfUsageByUser, long logInterval) {

        final Long labToLog = sampleUserFunctionView.getLab();

        final HourlyAnalyzableStorageUsage usage = newAnalyzableStorageUsage(now, file, 0,
            labToLog, 0, sampleUserFunctionView.getUserId(), sampleUserFunctionView.getUserName()
        );

        handleMissedLogs(now, file, logInterval, onMissedFoundFn(usage));

        return usage;
    }

    private Function<Integer, AnalyzableStorageUsage> onMissedFoundFn(final AnalyzableStorageUsage usage) {
        return missedLogsCount -> {
            usage.setHours(usage.getHours() + missedLogsCount);
            usage.setCharge(usage.getCharge() * missedLogsCount);
            return usage;
        };
    }

    private HourlyAnalyzableStorageUsage newStorageUsage(Date now, BillingFileView file, long size, Long lab) {
        final long amount = 0;
        final Long usedUser = file.getOwner();
        return newAnalyzableStorageUsage(now, file, size, lab, amount, usedUser, file.getOwnerName());
    }

    private HourlyAnalyzableStorageUsage newAnalyzableStorageUsage(Date now, BillingFileView file, long size, Long lab,
                                                                   long amount, Long usedUser, String userName) {

        final HourlyAnalyzableStorageUsage usage =
            new HourlyAnalyzableStorageUsage(lab, usedUser, file.getId(), size, now, userName, file.getInstrumentName(),
                amount, file.getFileName()
            );
        usage.setDay(daysSinceEpoch(now));

        return usage;
    }

    private long evaluateMSFunctionUsageTotalBytes(BillingUserFunctionsView item) {

        long transTotalBytes = 0;

        ImmutableMap<String, S3ObjectSummary> summaries =
            readFiles(storedObjectPaths.getRawFilesBucket(), item.getPath());

        for (Map.Entry<String, S3ObjectSummary> s3ObjectSummary : summaries.entrySet()) {
            transTotalBytes = transTotalBytes + s3ObjectSummary.getValue().getSize();
        }

        return transTotalBytes;
    }

    private FluentIterable<BillingUserFunctionsView> getMsFunctionItemsByPredicate(
        BillingFileView file,
        Predicate<BillingUserFunctionsView> predicate) {

        return from(file.getUserFunctionsViews())
            .filter(predicate);
    }

    private Predicate<BillingUserFunctionsView> translationDataForUser(final Long user) {

        return input -> input.getUserId().equals(user);
    }

    @Override
    protected <T extends ChargeableItemUsage> Function<T, Long> totalPriceFn() {
        return input -> ((AnalyzableStorageUsage) input).getTotalPrice();
    }
}
