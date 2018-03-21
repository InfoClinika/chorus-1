package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.view.BillingFileView;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.ArchiveStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.DailyArchiveStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.HourlyArchiveStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyArchiveStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.HourlyArchiveStorageUsageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.google.common.collect.FluentIterable.from;


/**
 * @author Herman Zamula
 */
@Service("archiveStorageLogHelper")
public class ArchiveStorageLogHelper extends AbstractStorageLogHelper {

    private final int hourlyMaxThreads;
    private final int dailyMaxThreads;
    private final DailyArchiveStorageUsageRepository dailyUsageRepository;
    private final HourlyArchiveStorageUsageRepository hourlyUsageRepository;

    protected Function<Lab, Page<ChargeableItemUsage>> sumLogsFn(final long dayToLog, final Pageable pageable) {

        return input -> {

            logger.debug("Daily summing archive storage usages for lab {" + input.getId() + "}");

            final Page<GroupedStorageUsage<HourlyArchiveStorageUsage>> storageUsagePage = hourlyUsageRepository.groupedNotCompressedUsagesByFile(input.getId(), dayToLog, pageable);
            return new PageImpl<>(from(storageUsagePage)
                    .transform(sumDailyUsageFn(dayToLog))
                    .toList(), pageable, storageUsagePage.getTotalElements());

        };
    }

    private Function<GroupedStorageUsage<HourlyArchiveStorageUsage>, ChargeableItemUsage> sumDailyUsageFn(final long dayToLog) {
        return input -> {

            final HourlyArchiveStorageUsage usage = input.itemUsage;
            final Date timestamp = new Date(input.itemUsage.getTimestamp());
            final DailyArchiveStorageUsage groupedUsage = new DailyArchiveStorageUsage(
                    usage.getLab(),
                    usage.getUser(),
                    usage.getFile(),
                    usage.getBytes(),
                    timestamp,
                    usage.getUsedBy(),
                    usage.getInstrument(),
                    input.summedCharge,
                    usage.getFileName()
            );
            groupedUsage.hours = input.hours.intValue();
            groupedUsage.compressed = true;
            groupedUsage.setBalance(input.itemUsage.getBalance());
            groupedUsage.setScaledToPayValue(input.itemUsage.getScaledToPayValue());
            groupedUsage.setDay(dayToLog);

            return groupedUsage;
        };
    }

    @Inject
    public ArchiveStorageLogHelper(HourlyArchiveStorageUsageRepository hourlyUsageRepository,
                                   DailyArchiveStorageUsageRepository dailyUsageRepository,
                                   @Value("${billing.logging.archive.threads.hourly}") int maxThreads,
                                   @Value("${billing.logging.archive.threads.daily}") int dailyMaxThreads) {
        this.hourlyUsageRepository = hourlyUsageRepository;
        this.dailyUsageRepository = dailyUsageRepository;
        this.hourlyMaxThreads = maxThreads;
        this.dailyMaxThreads = dailyMaxThreads;
    }


    @Override
    public void log(final long logInterval) {

        final ImmutableMap<String, S3ObjectSummary> files = readFiles(storedObjectPaths.getArchiveBucket(), null);
        final int totalFilesSize = files.size();
        logger.debug("Files found in archive bucket: " + totalFilesSize);
        final ExecutorService executorService = Executors.newFixedThreadPool(hourlyMaxThreads);


        int counter = 0;

        final int filesLogOperationCount = 1000;

        Map<String, S3ObjectSummary> filesMap = new HashMap<>(filesLogOperationCount);

        for (Map.Entry<String, S3ObjectSummary> summaryEntry : files.entrySet()) {

            filesMap.put(summaryEntry.getKey(), summaryEntry.getValue());
            counter++;

            if ((counter % filesLogOperationCount == 0) || (counter == (totalFilesSize))) {

                logger.debug("** Building log tasks... Files built: " + counter + " of " + totalFilesSize);

                logger.debug("Building archive usage tasks... Files built: " + counter + " of " + totalFilesSize);
                final Set<Callable<Iterable<HourlyArchiveStorageUsage>>> tasks = buildArchiveUsageTasks(logInterval, filesMap);

                final Date filesLoggedTime = new Date();

                logger.debug("Executing archive usage tasks... Files: " + counter + " of " + totalFilesSize);
                final List<Future<Iterable<HourlyArchiveStorageUsage>>> processedTasks = invokeAll(executorService, tasks);

                logger.debug("Saving and post process archived files usages...");
                saveUsagesAndPostProcessTasks(hourlyUsageRepository, processedTasks, filesLoggedTime);
                logger.debug("Building archive usage tasks saved. Files " + counter + " of " + totalFilesSize);

                filesMap = new HashMap<>(filesLogOperationCount);

            }

        }

        logger.debug("*** Log archived files ended ***");
        executorService.shutdownNow();
    }

    private Set<Callable<Iterable<HourlyArchiveStorageUsage>>> buildArchiveUsageTasks(final long logInterval, final Map<String, S3ObjectSummary> files) {

        final List<BillingFileView> allByArchiveId = files.keySet().isEmpty() ? Collections.<BillingFileView>emptyList() : fileViewRepository.findAllByArchiveId(files.keySet());
        logger.debug("Archived files count found in DB: " + allByArchiveId.size());

        return allByArchiveId.stream()
                .map(fileMetaData -> (Callable<Iterable<HourlyArchiveStorageUsage>>)
                        () -> logMetaFile(fileMetaData, files.get(fileMetaData.getArchiveId()), logInterval).asSet())
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized void sumLogs(Date dateDayToLog) {

        doSumLogs(hourlyUsageRepository, dailyUsageRepository, dailyMaxThreads, dateDayToLog);

    }

    private Optional<HourlyArchiveStorageUsage> logMetaFile(BillingFileView fileMetaData, S3ObjectSummary file, long logInterval) {

        final Date now = new Date();

        if (fileMetaData == null) {
            logger.error("Cannot find archived file in database. Bucket: '" + file.getBucketName() + "',  Key: '" + file.getKey() + "'");
            return Optional.absent();
        }

        final long lab = Optional.fromNullable(fileMetaData.getBillLab()).or(fileMetaData.getInstrumentLab());

        if (featuresRepository.enabledForLab(ApplicationFeature.BILLING.getFeatureName(), lab)) {

            final HourlyArchiveStorageUsage entry = newArchiveStorageUsage(lab, fileMetaData, now, file);
            handleMissedLogs(now, fileMetaData, logInterval, onMissedFoundFn(entry));
            return Optional.of(entry);

        }

        return Optional.absent();
    }

    private Function<Integer, ArchiveStorageUsage> onMissedFoundFn(final ArchiveStorageUsage entry) {
        return missedCount -> {
            entry.hours = entry.hours + missedCount;
            entry.setCharge(entry.getCharge() * missedCount);
            return entry;
        };
    }


    private HourlyArchiveStorageUsage newArchiveStorageUsage(Long lab, BillingFileView file, Date now, S3ObjectSummary s3ObjectSummary) {

        final long amount = 0;

        final HourlyArchiveStorageUsage usage = new HourlyArchiveStorageUsage(lab, file.getOwner(), file.getId(), s3ObjectSummary.getSize(),
                now, file.getOwnerName(), file.getInstrumentName(), amount, file.getFileName());
        usage.setDay(daysSinceEpoch(now));

        return usage;
    }

    @Override
    protected <T extends ChargeableItemUsage> Function<T, Long> totalPriceFn() {
        return new Function<T, Long>() {
            @Nullable
            @Override
            public Long apply(T input) {
                return input.getCharge();
            }
        };
    }
}
