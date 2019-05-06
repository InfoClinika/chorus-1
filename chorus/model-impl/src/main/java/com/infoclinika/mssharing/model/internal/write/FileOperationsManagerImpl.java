package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.Util;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.StorageData;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.write.FileAccessLogService;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Calendar.MINUTE;

/**
 * @author Herman Zamula
 */
@Service
@Transactional
public class FileOperationsManagerImpl implements FileOperationsManager {
    private static final int PAGE_SIZE = 1000;
    private static final int THREADS_COUNT = 5;
    private static final int THREAD_TIMEOUT = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileOperationsManagerImpl.class);
    private static final int MAXIMUM_UPLOAD_DURATION_IN_MINUTES = 60;
    private final FileMetaDataRepository fileMetaDataRepository;
    private final FeaturesHelper featuresHelper;
    private final RuleValidator ruleValidator;
    private final FileMovingManager fileMovingManager;
    private final ExperimentRepository experimentRepository;
    private final FileAccessLogService fileAccessLogService;
    private final CloudStorageClientsProvider cloudStorageClientsProvider;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    private TransactionTemplate transactionTemplate;

    @Inject
    public FileOperationsManagerImpl(PlatformTransactionManager platformTransactionManager,
                                     FileMetaDataRepository fileMetaDataRepository,
                                     FeaturesHelper featuresHelper,
                                     RuleValidator ruleValidator,
                                     FileMovingManager fileMovingManager,
                                     ExperimentRepository experimentRepository,
                                     FileAccessLogService fileAccessLogService,
                                     CloudStorageClientsProvider cloudStorageClientsProvider,
                                     AmazonPropertiesProvider amazonPropertiesProvider) {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.featuresHelper = featuresHelper;
        this.ruleValidator = ruleValidator;
        this.fileMovingManager = fileMovingManager;
        this.experimentRepository = experimentRepository;
        this.fileAccessLogService = fileAccessLogService;
        this.cloudStorageClientsProvider = cloudStorageClientsProvider;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    }

    @Override
    public void markFilesToArchive(final long actor, Set<Long> files) {

        if (!featuresHelper.isEnabled(ApplicationFeature.GLACIER)) {
            LOGGER.warn("Mark files to archive method called with disabled GLACIER feature");
            return;
        }

        final FluentIterable<Long> availableToArchiving = from(files)
            .filter(input -> ruleValidator.canArchiveFile(actor, input));

        for (long file : availableToArchiving) {
            prepareFileToArchive(actor, Optional.<Long>absent(), file);
        }

    }

    private void prepareFileToArchive(final long actor, Optional<Long> experiment, long file) {
        final ActiveFileMetaData metaData = fileMetaDataRepository.findOne(file);
        metaData.getStorageData().setToArchive(true);
        metaData.getStorageData().setStorageStatus(StorageData.Status.ARCHIVING_REQUESTED);

        fileMetaDataRepository.save(metaData);
        fileAccessLogService.logFileArchiveStart(actor, file);
    }

    private Lab getBillLab(ActiveExperiment activeExperiment) {
        return activeExperiment.getLab() == null ? activeExperiment.getBillLaboratory() : activeExperiment.getLab();
    }

    @Override
    public void markFilesToUnarchive(final long actor, Set<Long> files) {
        if (!featuresHelper.isEnabled(ApplicationFeature.GLACIER)) {
            LOGGER.warn("Mark files to unarchive method called with disabled GLACIER feature");
            return;
        }

        final FluentIterable<Long> filesToUnarchive = from(files).filter(canUnarchiveFile(actor));

        for (Long file : filesToUnarchive) {
            prepareFileToUnarchive(file, false);
        }

        fileMovingManager.requestFilesUnarchiving(filesToUnarchive.toSet(), actor);

    }

    private Predicate<Long> canUnarchiveFile(final long actor) {
        return file -> ruleValidator.canUnarchiveFile(actor, file);
    }

    private void prepareFileToUnarchive(Long file, boolean downloadOnly) {
        final ActiveFileMetaData metaData = fileMetaDataRepository.findOne(file);
        metaData.getStorageData().setToArchive(false);
        metaData.getStorageData().setArchivedDownloadOnly(downloadOnly);
        metaData.getStorageData().setArchivedDownloadCharged(downloadOnly ? false : null);
        fileMetaDataRepository.save(metaData);
    }

    @Override
    public void markExperimentFilesToArchive(long actor, long experiment) {
        if (!featuresHelper.isEnabled(ApplicationFeature.GLACIER)) {
            LOGGER.warn("Mark experiment files to archive method called with disabled GLACIER feature");
            return;
        }

        final ActiveExperiment activeExperiment = checkNotNull(experimentRepository.findOne(experiment));

        if (!ruleValidator.canArchiveExperiment(actor, activeExperiment)) {
            throw new AccessDenied("Can't archive experiment. Actor: " + actor + " experiment: " + experiment);
        }

        final FluentIterable<Long> files = from(activeExperiment.getRawFiles().getData())
            .transform(Transformers.RAW_FILES_META_ID_TRANSFORMER)
            .filter(input -> ruleValidator.canArchiveFile(actor, input));

        for (Long file : files) {
            prepareFileToArchive(actor, Optional.of(experiment), file);
        }
    }

    @Override
    public void markExperimentFilesToUnarchive(long actor, long experiment) {
        if (!featuresHelper.isEnabled(ApplicationFeature.GLACIER)) {
            LOGGER.warn("Mark experiment files to unarchive method called with disabled GLACIER feature");
            return;
        }

        final ActiveExperiment activeExperiment = checkNotNull(experimentRepository.findOne(experiment));
        if (!ruleValidator.canUnarchiveExperiment(actor, activeExperiment)) {
            throw new AccessDenied("Can't unarchive experiment. Actor: " + actor + " experiment: " + experiment);
        }

        final FluentIterable<Long> rawFiles = from(activeExperiment.getRawFiles().getData())
            .transform(Transformers.RAW_FILES_META_ID_TRANSFORMER)
            .filter(canUnarchiveFile(actor));

        for (Long file : rawFiles) {
            prepareFileToUnarchive(file, false);
        }

        fileMovingManager.requestFilesUnarchiving(rawFiles.toSet(), actor);
    }

    @Override
    public void archiveMarkedFiles() {
        if (!featuresHelper.isEnabled(ApplicationFeature.GLACIER)) {
            LOGGER.warn("Archive marked files method called with disabled GLACIER feature");
            return;
        }

        LOGGER.info("** Archive marked files method called. **");
        final List<Long> forArchiving = fileMetaDataRepository.findIdsMarkedForArchiving();
        for (final Long file : forArchiving) {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    final ActiveFileMetaData metaData = fileMetaDataRepository.findOne(file);
                    metaData.getStorageData().setToArchive(false);
                    fileMovingManager.moveToArchiveStorage(file);
                    fileMetaDataRepository.save(metaData);
                }
            });
        }
    }

    @Override
    public void unarchiveMarkedFiles() {
        if (!featuresHelper.isEnabled(ApplicationFeature.GLACIER)) {
            LOGGER.warn("Unarchive marked files method called with disabled GLACIER feature");
            return;
        }
        LOGGER.info("** Unarchive marked files method called. **");
        fileMovingManager.moveReadyToUnarchiveToAnalysableStorage();
        LOGGER.info("** Unarchive marked files method completed. **");
    }

    @Override
    public void makeExperimentFilesAvailableForDownload(long actor, long experiment) {
        final ActiveExperiment activeExperiment = experimentRepository.findOne(experiment);

        if (!ruleValidator.canUnarchiveExperiment(actor, activeExperiment)) {
            throw new AccessDenied(
                "User with ID: " + actor + " is not allowed to unarchive experiment with ID: " + experiment
            );
        }

        for (ExperimentFileTemplate rawFile : activeExperiment.getRawFiles().getData()) {
            prepareFileToUnarchive(rawFile.getFileMetaData().getId(), true);
        }

        fileMovingManager.requestExperimentFilesUnarchiving(experiment, newArrayList(actor));
    }


    @Override
    public void makeFilesAvailableForDownload(long actor, Set<Long> files) {
        final boolean canUnarchiveAll = files.stream().allMatch(fId -> ruleValidator.canUnarchiveFile(actor, fId));

        if (!canUnarchiveAll) {
            throw new AccessDenied("User with ID: " + actor + " is not allowed to unarchive some of the " +
                "files with IDs " + Joiner.on(", ").join(files)
            );
        }

        for (Long file : files) {
            prepareFileToUnarchive(file, true);
        }

        fileMovingManager.requestFilesUnarchiving(files, actor);
    }

    @Override
    public void checkIsFilesConsistent(long actor) {
        if (!ruleValidator.hasAdminRights(actor)) {
            throw new AccessDenied("Non-admin user is now allowed to check file size consistent.");
        }

        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();

        int pageNum = 0;
        final ExecutorService executorService = Executors.newFixedThreadPool(THREADS_COUNT);

        while (true) {
            final Page<ActiveFileMetaData> files =
                fileMetaDataRepository.findBySizeIsConsistent(false, new PageRequest(pageNum, PAGE_SIZE));
            final List<Callable<Object>> tasks = new ArrayList<>(PAGE_SIZE);

            for (ActiveFileMetaData file : files) {
                if (!file.isSizeConsistent() && (file.getContentId() != null || file.getArchiveId() != null)) {
                    tasks.add(createCheckFileSizeIsConsistentTask(file, cloudStorageService));
                }
            }

            LOGGER.info("Run {} threads with {} tasks to check file size consistent.", THREADS_COUNT, tasks.size());
            try {
                final List<Future<Object>> futures = executorService.invokeAll(tasks);
                for (Future<Object> future : futures) {
                    future.get();
                }
            } catch (Exception e) {
                LOGGER.warn("Error while executing tasks for check file size consistent.", e);
                throw Throwables.propagate(e);
            }

            pageNum++;
            if (files.isLast()) {
                break;
            }
        }

        executorService.shutdownNow();
    }

    @Override
    public void checkIsFileConsistent(long actor, long id) {
        final ActiveFileMetaData file = fileMetaDataRepository.findOne(id);
        if (!file.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner is able to check file consistent");
        }

        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();

        try {
            final String bucket = amazonPropertiesProvider.getActiveBucket();
            final String objectPath = file.getContentId();
            LOGGER.info("Try to read size of file {} | {}", bucket, objectPath);
            final long contentLength =
                cloudStorageService.readContentLength(new CloudStorageItemReference(bucket, objectPath));
            file.setSizeIsConsistent(contentLength == file.getSizeInBytes());
            fileMetaDataRepository.save(file);
        } catch (Exception e) {
            LOGGER.warn("Error read size of file from S3", e);
        }
    }

    @Override
    public void markUnsuccessfullyUploadedFilesToReplaceAndCorrupted() {
        final Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        instance.add(MINUTE, -MAXIMUM_UPLOAD_DURATION_IN_MINUTES);
        final Date fromDate = instance.getTime();

        fileMetaDataRepository.markAllUnsuccessfullyUploadedFilesToReplaceAndCorrupted(fromDate);
    }

    private Callable<Object> createCheckFileSizeIsConsistentTask(ActiveFileMetaData fileMetaData,
                                                                 CloudStorageService cloudStorageService) {
        return () -> {
            if (fileMetaData == null) {
                return null;
            }

            final String bucket = fileMetaData.getContentId() != null
                ? amazonPropertiesProvider.getActiveBucket()
                : amazonPropertiesProvider.getArchiveBucket();

            final String contentId = fileMetaData.getContentId() != null
                ? fileMetaData.getContentId()
                : fileMetaData.getArchiveId();
            long contentLength;
            try {
                LOGGER.info("Try to read size of file {} | {}", bucket, contentId);
                contentLength =
                    cloudStorageService.readContentLength(new CloudStorageItemReference(bucket, contentId));
            } catch (Exception e) {
                LOGGER.warn("Error read size of file from S3", e);
                return null;
            }

            final boolean sizeIsConsistent = fileMetaData.getSizeInBytes() == contentLength;
            LOGGER.info(
                "File {} | {}. Consistent: {}. Size in database: {}. Size in storage: {}",
                bucket,
                contentId,
                sizeIsConsistent,
                fileMetaData.getSizeInBytes(),
                contentLength
            );

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    fileMetaData.setSizeIsConsistent(sizeIsConsistent);
                    fileMetaDataRepository.save(fileMetaData);
                }
            });

            Thread.sleep(THREAD_TIMEOUT);
            return null;
        };
    }
}
