package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.entity.view.BillingFileView;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.BillingFileViewRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.repository.StorageFeatureUsageRepository;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Functions.forPredicate;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.services.billing.persistence.helper.BillingFeatureChargingHelper.ChargedInfo;
import static org.joda.time.Days.daysBetween;


/**
 * @author Herman Zamula
 */
public abstract class AbstractStorageLogHelper implements StorageLogHelper {

    private static final int MAX_S3_RETRIES_COUNT = 5;
    public static final int INTERVAL_UNIT = 1;
    private static final int MAX_S3_CONNECTIONS = 1000;
    private static final Function<ChargeableItemUsage, Long> FILE_FROM_USAGE = input -> input.getFile();
    public static final int SUM_LOGS_MAX_PAGE_SIZE = 5000;

    @Inject
    protected LabRepository labRepository;
    @Inject
    protected FileMetaDataRepository fileRepository;
    @Inject
    protected PaymentCalculationsHelper paymentCalculations;
    @Inject
    protected LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    protected StoredObjectPaths storedObjectPaths;
    @Inject
    protected FeaturesHelper featuresHelper;
    @Inject
    protected BillingFileViewRepository fileViewRepository;
    @PersistenceContext(unitName = "mssharing")
    private EntityManager em;
    @Inject
    private PlatformTransactionManager ptm;
    protected TransactionTemplate transactionTemplate;
    @Inject
    protected Transformers transformers;
    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private AmazonS3 s3Client;
    private final Predicate<S3ObjectSummary> filterBySize = input -> input.getSize() > 0;
    protected DateTimeZone timeZone;


    protected int daysSinceEpoch(Date timestamp) {
        return daysBetween(new DateTime(0).withZone(timeZone), new DateTime(timestamp).withZone(timeZone)).getDays();
    }

    protected int daysSinceEpoch(DateTime timestamp) {
        return daysBetween(new DateTime(0).withZone(timeZone), timestamp).getDays();
    }

    private AmazonS3 getS3Client() {
        return s3Client;
    }

    @PostConstruct
    public void initializeAmazonClient() {
        this.transactionTemplate = new TransactionTemplate(ptm);
        this.timeZone = DateTimeZone.forTimeZone(transformers.serverTimezone);
        this.s3Client = cloudStorageClientsProvider.getAmazonS3Client();
    }

    /*---------------------------- Retrieve files from S3 common methods ---------------------------------------------*/

    protected ImmutableMap<String, S3ObjectSummary> readFiles(final String bucket, final String prefix) {
        logger.trace("Start listing files from S3. Bucket { {} }, prefix { {} }", bucket, prefix);

        ObjectListing objectListing = listItemsWithRetries(bucket, prefix, initialListing(bucket, prefix));
        final ImmutableMap.Builder<String, S3ObjectSummary> builder = ImmutableMap.builder();
        addSummariesFilteredBySize(objectListing, builder);

        while (objectListing.isTruncated()) {
            objectListing = listItemsWithRetries(bucket, prefix, nextBatchListing(objectListing));
            addSummariesFilteredBySize(objectListing, builder);
            if (!objectListing.isTruncated()) {
                break;
            }
        }

        logger.trace("End listing files from S3. Bucket { {} }, prefix { {} }", bucket, prefix);
        return builder.build();
    }

    private ObjectListingHandler initialListing(final String bucket, final String prefix) {
        return () -> {
            logger.trace("Initial objects listing. Bucket { {} }, prefix { {} }", bucket, prefix);
            final ObjectListing objectListing = getS3Client().listObjects(bucket, prefix);
            logger.trace("Initial objects listing. Listed {} objects.", objectListing.getObjectSummaries().size());
            return objectListing;
        };
    }

    private ObjectListingHandler nextBatchListing(final ObjectListing objectListing) {
        return () -> {
            logger.trace("Listing next batch of objects. Bucket { {} }, prefix { {} }", objectListing.getBucketName(),
                objectListing.getPrefix());
            final ObjectListing nextListing = getS3Client().listNextBatchOfObjects(objectListing);
            logger.trace("Listing next batch of objects. Listed {} objects.", nextListing.getObjectSummaries().size());
            return nextListing;
        };
    }

    private ObjectListing listItemsWithRetries(String bucket, String prefix, ObjectListingHandler listingHandler) {
        int retries = 0;
        Throwable lastException;
        do {
            try {
                return listingHandler.listObjects();
            } catch (Throwable ex) {
                retries++;
                lastException = ex;
                logger.warn(
                    "*** Cannot list objects from S3: {}. Bucket { {} }, prefix { {} }, retries count: {}",
                    ex.getMessage(), bucket, prefix, retries
                );
            }
        } while (MAX_S3_RETRIES_COUNT > retries);
        throw new RuntimeException(lastException);
    }

    private void addSummariesFilteredBySize(ObjectListing objectListing,
                                            ImmutableMap.Builder<String, S3ObjectSummary> builder) {
        for (S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()) {
            if (filterBySize.apply(s3ObjectSummary)) {
                builder.put(s3ObjectSummary.getKey(), s3ObjectSummary);
            }
        }
    }

    private interface ObjectListingHandler {
        ObjectListing listObjects();
    }

    /*--------------------- File usage processing common methods -----------------------------------------------------*/

    protected void handleMissedLogs(Date now, BillingFileView file, long logInterval,
                                    Function<Integer, ? extends ChargeableItemUsage> onMissedFound) {
        if (fromNullable(file.getLastChargingDate()).transform(forPredicate(isDateUnbound(now, logInterval)))
            .or(false)) {
            logger.trace("*** File {" + file.getId() + "} is not logged in time. Recharging feature usage...");
            final long nowTime = normalizeTime(now, logInterval);
            final long lastChargingTime = normalizeTime(file.getLastChargingDate(), logInterval);
            final int missedLogsCount = (int) (nowTime - lastChargingTime - INTERVAL_UNIT);
            onMissedFound.apply(missedLogsCount);
        }
    }

    private Predicate<Date> isDateUnbound(final Date now, final long logInterval) {
        final long normalizedCurrent = normalizeTime(now, logInterval);
        return input -> {
            final long previousLogged = normalizeTime(input, logInterval);
            return normalizedCurrent - previousLogged > 1;
        };
    }

    protected <T extends ChargeableItemUsage> List<Future<Iterable<T>>> invokeAll(ExecutorService executorService,
                                                                                  Set<Callable<Iterable<T>>> tasks,
                                                                                  int wait) {
        try {
            return executorService.invokeAll(tasks, wait, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.error("*** Exception thrown when executing async task", e);
        }
        return Collections.emptyList();
    }

    protected <T extends ChargeableItemUsage> List<Future<Iterable<T>>> invokeAll(ExecutorService executorService,
                                                                                  Set<Callable<Iterable<T>>> tasks) {
        return invokeAll(executorService, tasks, 1);
    }

    private long normalizeTime(Date now, long logInterval) {
        return now.getTime() / (logInterval);
    }

    /*----------------------- Post process logging of feature usage functions
    -----------------------------------------*/

    protected <T extends ChargeableItemUsage> void saveUsagesAndPostProcessTasks(
        final StorageFeatureUsageRepository<T> repository,
        List<Future<Iterable<T>>> processedTasks,
        final Date filesLoggedTime) {

        logger.debug("*** Processing data for saving...");

        final Function<Future<Iterable<T>>, Iterable<T>> function = this::getChargeableItemSet;
        final FluentIterable<T> fluentUsages = retrieveUsages(processedTasks, function);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                chargeLaboratory(fluentUsages);
                saveUsages(repository, fluentUsages);
                updateFileLastLoggedTime(filesLoggedTime, fluentUsages);
            }
        });
    }

    /*---------------------- Sum logs methods -----------------------------------------------------------------*/

    protected void doSumLogs(StorageFeatureUsageRepository hourlyUsageRepository,
                             StorageFeatureUsageRepository dailyUsageRepository,
                             int dailyMaxThreads,
                             Date dateDayToLog) {

        final ExecutorService executorService = Executors.newFixedThreadPool(dailyMaxThreads);
        final List<Lab> labs = labRepository.findAll();
        logger.debug(size(labs) + " labs found for billing");
        logger.debug("Start sum logs task executing...");

        final int dayToLog = daysSinceEpoch(new DateTime(dateDayToLog)); //Previous day

        labs.stream().forEach(lab -> executorService.execute(()
            -> doSumLogsByLab(lab, hourlyUsageRepository, dailyUsageRepository, dayToLog)));

        executorService.shutdown();
    }

    private void doSumLogsByLab(Lab lab, StorageFeatureUsageRepository hourlyUsageRepository,
                                StorageFeatureUsageRepository dailyUsageRepository, int dayToLog) {

        Pageable pageRequest = getInitialPageRequest();
        Page<ChargeableItemUsage> page;

        logger.debug("Executing sum logs paged result for lab {}...", lab.getId());

        do {

            page = sumLogsFn(dayToLog, pageRequest).apply(lab);

            if (!page.getContent().isEmpty()) {
                logger.debug("Saving sum logs paged result for lab {}, page is {}...", lab.getId(), page);
                //noinspection unchecked
                saveSumLogsPage(hourlyUsageRepository, dailyUsageRepository, page, dayToLog);
            }

        } while (page.hasNext());

    }

    private PageRequest getInitialPageRequest() {
        return new PageRequest(0, SUM_LOGS_MAX_PAGE_SIZE);
    }


    protected <T extends ChargeableItemUsage> void saveSumLogsPage(
        final StorageFeatureUsageRepository<T> hourlyUsageRepository,
        StorageFeatureUsageRepository<T> dailyUsageRepository,
        final Page<T> page,
        final int dayToLog) {

        doSaveSumLogs(hourlyUsageRepository, dailyUsageRepository, dayToLog, page.getContent());

    }

    private <T extends ChargeableItemUsage> void doSaveSumLogs(
        final StorageFeatureUsageRepository<T> hourlyUsageRepository,
        final StorageFeatureUsageRepository<T> dailyUsageRepository,
        final int dayToLog,
        final Iterable<T> content) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                deleteLogs(content, hourlyUsageRepository, dayToLog);
                saveUsages(dailyUsageRepository, content);
            }
        });
    }

    /*----------------------------------- Logs CRUD methods ----------------------------------------------------------*/

    private <T extends ChargeableItemUsage> void deleteLogs(Iterable<T> fluentUsages,
                                                            StorageFeatureUsageRepository<T> usageRepository,
                                                            int dayToLog) {
        logger.debug("Deleting logs...");
        final ImmutableSet<Long> files = from(fluentUsages).transform(FILE_FROM_USAGE).toSet();
        if (files.isEmpty()) {
            logger.debug("Skipping delete empty usages...");
            return;
        }
        usageRepository.deleteLogsForFilesOfDay(files, dayToLog);
    }

    private <T extends ChargeableItemUsage> void updateFileLastLoggedTime(Date filesLoggedTime,
                                                                          FluentIterable<T> fluentUsages) {
        logger.debug("*** Updating files last charging date...");
        final ImmutableList<Long> filesToSave = from(fluentUsages.transform(FILE_FROM_USAGE).toSet()).toList();
        if (filesToSave.isEmpty()) {
            logger.debug("Skipping empty usages...");
            return;
        }
        update(filesLoggedTime, filesToSave);
    }

    private Connection getConnection() {
        try {
            return ((SessionFactoryImplementor) em.unwrap(Session.class).getSessionFactory()).getConnectionProvider()
                .getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void update(Date filesLoggedTime, ImmutableList<Long> fileIds) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            final String idsString = toInClause(fileIds);
            final String query =
                "UPDATE FileMetaDataTemplate set billing_last_charging_date = ? WHERE id in ( " + idsString + ")";
            statement = connection.prepareStatement(query);
            statement.setTimestamp(1, new Timestamp(filesLoggedTime.getTime()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error("Can't close statement", e);
                }
            }

            if (connection != null) {

                try {
                    ((SessionFactoryImplementor) em.unwrap(Session.class).getSessionFactory()).getConnectionProvider()
                        .closeConnection(connection);
                } catch (SQLException e) {
                    logger.error("Can't close connection", e);
                }
            }

        }
    }

    private static String toInClause(ImmutableList<Long> fileIds) {
        final StringBuilder sb = new StringBuilder();
        final Long lastID = getLast(fileIds);
        for (Long fileId : fileIds) {
            sb.append(fileId);
            if (!fileId.equals(lastID)) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private <T extends ChargeableItemUsage> void chargeLaboratory(FluentIterable<T> fluentUsages) {
        logger.debug("*** Saving logged price for labs...");
        final HashMap<Long, ImmutableSet.Builder<T>> usagesPerLab = transformToUsagesPerLab(fluentUsages);
        for (Map.Entry<Long, ImmutableSet.Builder<T>> labUsagesEntry : usagesPerLab.entrySet()) {
            final ImmutableSet<T> labUsages = labUsagesEntry.getValue().build();
            final LabPaymentAccount paymentAccount = labPaymentAccountRepository.findByLab(labUsagesEntry.getKey());
            final ChargedInfo chargedInfo =
                new ChargedInfo(paymentAccount.getStoreBalance(), paymentAccount.getScaledToPayValue());
            setChargedValuesToUsages(labUsages, chargedInfo);
        }
    }

    private static <T extends ChargeableItemUsage> void setChargedValuesToUsages(ImmutableSet<T> labUsages,
                                                                                 ChargedInfo chargedInfo) {
        for (T labUsage : labUsages) {
            labUsage.setBalance(chargedInfo.balance);
            labUsage.setScaledToPayValue(chargedInfo.scaledToPayValue);
        }
    }

    private <T extends ChargeableItemUsage> void saveUsages(StorageFeatureUsageRepository<T> repository,
                                                            Iterable<T> fluentUsages) {
        logger.debug("*** Saving usages... ");
        repository.save(fluentUsages);
    }

    protected abstract <T extends ChargeableItemUsage> Function<T, Long> totalPriceFn();

    /*------------------------ Transforming functions ----------------------------------------------------------------*/

    private <T extends ChargeableItemUsage> FluentIterable<T> retrieveUsages(List<Future<Iterable<T>>> commands,
                                                                             Function<Future<Iterable<T>>,
                                                                                 Iterable<T>> function) {
        return from(commands)
            .transformAndConcat(function)
            .filter(notNull());
    }

    private <T extends ChargeableItemUsage> HashMap<Long, ImmutableSet.Builder<T>> transformToUsagesPerLab(
        FluentIterable<T> fluentUsages) {
        final HashMap<Long, ImmutableSet.Builder<T>> usagesPerLab = newHashMap();

        for (T usage : fluentUsages) {
            ImmutableSet.Builder<T> usageBuilder = usagesPerLab.get(usage.getLab());
            if (usageBuilder == null) {
                usageBuilder = ImmutableSet.builder();
                usagesPerLab.put(usage.getLab(), usageBuilder);
            }
            usageBuilder.add(usage);
        }
        return usagesPerLab;
    }

    private <T extends ChargeableItemUsage> Iterable<T> getChargeableItemSet(Future<Iterable<T>> input) {
        try {
            return input.get();
        } catch (Exception e) {
            logger.error("Unexpected exception occurred while getting task result: {}" + e.getMessage());
            return ImmutableSet.of();
        }
    }

    protected abstract Function<Lab, Page<ChargeableItemUsage>> sumLogsFn(final long dayToLog, final Pageable pageable);

}
