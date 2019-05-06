package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.GlacierDownloadListeners;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.helper.FileArchivingHelper;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.FileDownloadJob;
import com.infoclinika.mssharing.model.internal.entity.FilesDownloadGroup;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.Util;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.StorageData;
import com.infoclinika.mssharing.model.internal.repository.FileDownloadGroupRepository;
import com.infoclinika.mssharing.model.internal.repository.FileDownloadJobRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.FileAccessLogService;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.model.InboxNotifierTemplate;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.model.internal.entity.restorable.StorageData.Status.ARCHIVED;
import static com.infoclinika.mssharing.model.internal.entity.restorable.StorageData.Status.UNARCHIVED;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Elena Kurilina
 */
@Service
public class FileMovingManagerImpl implements FileMovingManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileMovingManagerImpl.class);

    private final BillingFeaturesHelper billingFeaturesHelper;
    private final FileMetaDataRepository fileMetaDataRepository;
    private final FileDownloadJobRepository fileDownloadJobRepository;
    private final Notifier notifier;
    private final FileDownloadGroupRepository downloadGroupRepository;
    private final UserRepository userRepository;
    private final InboxNotifierTemplate inboxNotifier;
    private final GlacierDownloadListeners<ActiveFileMetaData> listeners;
    private final FileArchivingHelper fileArchivingHelper;
    private final AmazonPropertiesProvider amazonPropertiesProvider;
    private final FileAccessLogService fileAccessLogService;
    private final CloudStorageClientsProvider cloudStorageClientsProvider;

    @SuppressWarnings("all")
    private boolean testMode = false;

    @Inject
    public FileMovingManagerImpl(PlatformTransactionManager platformTransactionManager,
                                 GlacierDownloadListeners<ActiveFileMetaData> listeners,
                                 BillingFeaturesHelper billingFeaturesHelper,
                                 FileMetaDataRepository fileMetaDataRepository,
                                 CloudStorageClientsProvider cloudStorageClientsProvider,
                                 FileDownloadJobRepository fileDownloadJobRepository,
                                 Notifier notifier,
                                 FileDownloadGroupRepository downloadGroupRepository,
                                 UserRepository userRepository,
                                 InboxNotifierTemplate inboxNotifier,
                                 FileArchivingHelper fileArchivingHelper,
                                 AmazonPropertiesProvider amazonPropertiesProvider,
                                 FileAccessLogService fileAccessLogService) {
        this.listeners = listeners;
        this.billingFeaturesHelper = billingFeaturesHelper;
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.cloudStorageClientsProvider = cloudStorageClientsProvider;
        this.fileDownloadJobRepository = fileDownloadJobRepository;
        this.notifier = notifier;
        this.downloadGroupRepository = downloadGroupRepository;
        this.userRepository = userRepository;
        this.inboxNotifier = inboxNotifier;
        this.fileArchivingHelper = fileArchivingHelper;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
        this.fileAccessLogService = fileAccessLogService;
    }

    @Override
    public void requestExperimentFilesUnarchiving(long experimentId, Collection<Long> actors) {
        final Iterable<ActiveFileMetaData> filteredFiles =
            filter(
                fileMetaDataRepository.findByExperiment(experimentId),
                not(input -> input.getStorageData().getStorageStatus().equals(UNARCHIVED))
            );

        FilesDownloadGroup downloadGroup = downloadGroupRepository.findByExperiment(experimentId);
        if (downloadGroup == null) {
            downloadGroup = new FilesDownloadGroup(experimentId);
            requestFilesUnarchiving(Iterables.transform(filteredFiles, EntityUtil.ENTITY_TO_ID), downloadGroup);
        }

        downloadGroup.getNotifiers().addAll(Collections2.transform(actors, Util.USER_FROM_ID));
        downloadGroupRepository.save(downloadGroup);
    }

    private void requestFilesUnarchiving(Iterable<Long> files, FilesDownloadGroup downloadGroup) {
        for (Long file : files) {
            requestUnarchiving(file, downloadGroup);
        }
    }

    @Override
    public void requestFilesUnarchiving(Collection<Long> files, Long actor) {
        if (files.size() == 0) {
            LOGGER.warn("*** Skipping Move to Storage operation with empty input files...");
            return;
        }
        final FilesDownloadGroup downloadGroup = new FilesDownloadGroup();
        requestFilesUnarchiving(files, downloadGroup);
        downloadGroup.getNotifiers().add(userRepository.findOne(actor));
        downloadGroupRepository.save(downloadGroup);
    }

    @Override
    public void requestFileUnarchiving(long file) {
        processUnarchivingRequest(file);
    }

    @Override
    public void moveFilesToStorageAndListen(Collection<Long> files, String listenerId) {
        for (Long file : files) {
            requestUnarchiving(file, listenerId);
        }
    }

    private void requestUnarchiving(Long file, String listenerId) {
        final FileDownloadJob fileDownloadJob = fileDownloadJobRepository.findByFileMetaDataId(file);
        final FileDownloadJob job = fromNullable(fileDownloadJob).or(requestNewDownloadJob(file));
        job.listener = listenerId;
        fileDownloadJobRepository.save(job);
    }

    private void requestUnarchiving(Long file, FilesDownloadGroup downloadGroup) {
        final FileDownloadJob fileDownloadJob = fileDownloadJobRepository.findByFileMetaDataId(file);
        final FileDownloadJob job = fromNullable(fileDownloadJob).or(requestNewDownloadJob(file));
        prepareToUnarchiving(job.fileMetaData);
        fileDownloadJobRepository.save(job);
        downloadGroup.getJobs().add(job);
    }

    @Override
    public void moveToArchiveStorage(long file) {
        LOGGER.info("*** Move file from analysable storage to archive storage: {}", file);
        moveFileToArchiveStorage(file);
    }

    @Override
    public void moveToArchiveStorage(Collection<Long> files) {
        for (Long file : files) {
            moveFileToArchiveStorage(file);
        }
    }

    @Override
    public void moveToAnalysableStorage(Collection<Long> files) {
        final FilesDownloadGroup filesDownloadGroup = new FilesDownloadGroup();
        requestFilesUnarchiving(files, filesDownloadGroup);
        downloadGroupRepository.save(filesDownloadGroup);
    }

    private Supplier<FileDownloadJob> requestNewDownloadJob(final long file) {
        return () -> {
            final ActiveFileMetaData metaData = processUnarchivingRequest(file);
            return new FileDownloadJob(metaData);
        };
    }

    private ActiveFileMetaData processUnarchivingRequest(long file) {

        final ActiveFileMetaData metaData = fileMetaDataRepository.findOne(file);

        if (metaData.getArchiveId() == null && metaData.getContentId() != null) {
            LOGGER.warn(
                "*** Requesting to unarchive file that is not in archive yet. Content id: {}",
                metaData.getContentId()
            );
        } else {
            checkState(fileArchivingHelper.requestUnarchive(
                metaData.getArchiveId(),
                metaData.getStorageData().isArchivedDownloadOnly()
            ), "Unarchive request failed");
        }

        prepareToUnarchiving(metaData);
        return metaData;
    }

    private void prepareToUnarchiving(ActiveFileMetaData file) {
        file.getStorageData().setStorageStatus(StorageData.Status.UNARCHIVING_REQUESTED);
        file.setLastAccess(new Date());
        fileMetaDataRepository.save(file);
    }

    @Override
    public void downloadToAnalysableStorageRetrievedFile(long file) {
        final FileDownloadJob job = fileDownloadJobRepository.findByFileMetaDataId(file);
        if (job != null && !job.isCompleted()) {
            String archiveId = job.fileMetaData.getArchiveId();
            if (fileArchivingHelper.isArchiveReadyToRestore(archiveId)) {
                String contentId = fileArchivingHelper.moveToAnalyzableStorage(archiveId);
                final Map<Long, Set<Long>> usersToNotify = processJobAndSaveMovementToStorage(job, contentId);
                usersToNotify.forEach(this::sendDownloadReadyNotifications);
            }
        }
    }

    private void callListener(FileDownloadJob job) {
        if (job.listener != null && listeners.getListener(job.listener) != null) {
            listeners.getListener(job.listener).onFileDownloaded(job.fileMetaData);
        }
    }

    @Override
    public void updateAccessForExperiment(long experiment) {
        Collection<ActiveFileMetaData> files = fileMetaDataRepository.findByExperiment(experiment);
        updateLastAccess(files);
    }

    @Override
    public void updateAccessForFile(Collection<Long> filesIds) {
        Collection<ActiveFileMetaData> files = fileMetaDataRepository.findAllByIds(filesIds);
        updateLastAccess(files);
    }

    private void updateLastAccess(Collection<ActiveFileMetaData> files) {
        for (ActiveFileMetaData file : files) {
            file.setLastAccess(new Date());
        }
        fileMetaDataRepository.save(files);
    }

    @Override
    public void deleteFromStorage(String key) {
        deleteFromS3(amazonPropertiesProvider.getActiveBucket(), key);
    }

    @Override
    public void deleteFromArchiveStorage(String key) {
        deleteFromS3(amazonPropertiesProvider.getArchiveBucket(), key);
    }

    private void deleteFromS3(String bucket, String contentId) {
        if (!isEmpty(contentId)) {
            final CloudStorageItemReference reference = new CloudStorageItemReference(bucket, contentId);
            LOGGER.warn("DELETING file from S3: {}", reference);
            final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();
            if (cloudStorageService.existsAtCloud(reference)) {
                cloudStorageService.deleteFromCloud(reference);
            } else {
                LOGGER.debug("File is not found on S3 by key: {}", reference);
            }
        }
    }

    @Override
    public void moveReadyToUnarchiveToAnalysableStorage() {
        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();
        final Iterable<FileDownloadJob> jobs = fileDownloadJobRepository.findNotCompleted();

        final Map<Long, Set<Long>> usersToNotify = newHashMap();

        for (FileDownloadJob job : jobs) {

            final ActiveFileMetaData file = job.fileMetaData;
            final String archiveId = file.getArchiveId();


            if (archiveId == null && file.getContentId() != null) {
                LOGGER.warn(
                    "*** Skipping direct checking for file restoration. File already in analysable storage {}",
                    file.getContentId()
                );

                final Map<Long, Set<Long>> usersFilesToNotify = handleUnarchivedJob(job);
                usersFilesToNotify.forEach((key, value) -> usersToNotify.merge(key, value, Sets::union));
            } else if (fileArchivingHelper.isArchiveReadyToRestore(archiveId)) {
                LOGGER.info("Restore file. ID= {}. path: ", file.getId(), archiveId);

                final Map<Long, Set<Long>> usersFilesToNotify = handleUnarchivedJob(job);
                usersFilesToNotify.forEach((key, value) -> usersToNotify.merge(key, value, Sets::union));
            } else if (cloudStorageService.existsAtCloud(new CloudStorageItemReference(
                amazonPropertiesProvider.getActiveBucket(),
                file.getArchiveId()
            ))) {
                //For test data (real file already moved to storage)
                LOGGER.warn("*** File already present in analyzable storage. File: {}", file.getArchiveId());

                final Map<Long, Set<Long>> usersFilesToNotify =
                    processJobAndSaveMovementToStorage(job, file.getArchiveId());
                usersFilesToNotify.forEach((key, value) -> usersToNotify.merge(key, value, Sets::union));
            }
        }

        usersToNotify.forEach(this::sendDownloadReadyNotifications);
    }

    @Override
    public void moveToArchiveExpiredUnarchivedFiles() {
        final List<ActiveFileMetaData> files = fileMetaDataRepository.findUnarchivedWIthExpirationTime();

        for (ActiveFileMetaData file : files) {
            if (file.getArchiveId() != null && fileArchivingHelper.isArchived(file.getArchiveId())) {
                LOGGER.info(
                    "*** Save expired unarchived file movement. File id: {}, archiveId: {}",
                    file.getId(),
                    file.getArchiveId()
                );
                saveMovementToArchive(file, checkNotNull(file.getArchiveId()));
            }
        }
    }

    private Map<Long, Set<Long>> handleUnarchivedJob(FileDownloadJob job) {
        final ActiveFileMetaData metaData = job.fileMetaData;
        final Long lab = metaData.getInstrument().getLab().getId();
        if (billingFeaturesHelper.isFeatureEnabled(lab, BillingFeature.ANALYSE_STORAGE)) {
            if (metaData.getArchiveId() == null && metaData.getContentId() != null) {
                LOGGER.warn(
                    "** Skipping move file to analyzable storage. Content id is already exists. File: {}",
                    metaData.getContentId()
                );
                if (metaData.getStorageData().isArchivedDownloadOnly()) {
                    return processUnarchivedFileInArchiveStorage(job);
                } else {
                    return processJobAndSaveMovementToStorage(job, metaData.getContentId());
                }
            } else {
                if (metaData.getStorageData().isArchivedDownloadOnly()) {
                    return processUnarchivedFileInArchiveStorage(job);
                } else {
                    return processAnalysableStorageUnarchivedFile(job);
                }
            }
        } else {
            return processUnarchivedFileInArchiveStorage(job);
        }
    }

    private Map<Long, Set<Long>> processAnalysableStorageUnarchivedFile(FileDownloadJob job) {
        final String contentId = fileArchivingHelper.moveToAnalyzableStorage(job.fileMetaData.getArchiveId());
        return processJobAndSaveMovementToStorage(job, contentId);
    }

    private Map<Long, Set<Long>> processUnarchivedFileInArchiveStorage(FileDownloadJob job) {
        final ActiveFileMetaData fileMetaData = job.fileMetaData;
        fileMetaData.getStorageData().setStorageStatus(UNARCHIVED);
        fileMetaData.getStorageData().setLastUnarchiveTimestamp(new Date());
        fileMetaDataRepository.save(fileMetaData);
        final Map<Long, Set<Long>> usersToNotify = processJobForNotify(job);
        callListener(job);
        return usersToNotify;
    }

    private Map<Long, Set<Long>> processJobAndSaveMovementToStorage(FileDownloadJob job, String contentId) {
        saveMovementToStorage(job.fileMetaData, contentId);
        final Map<Long, Set<Long>> usersToNotify = processJobForNotify(job);
        callListener(job);
        return usersToNotify;
    }

    private void moveFileToArchiveStorage(Long file) {
        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();
        final ActiveFileMetaData metaData = fileMetaDataRepository.findOne(file);
        final Optional<String> contentId = Optional.fromNullable(metaData.getContentId());
        if (!contentId.isPresent()) {
            LOGGER.error("**** Trying move the file to archive without content id specified. File: {}", file);
            return;
        }
        if (metaData.isReadOnly()) {
            LOGGER.warn("**** Trying move the readonly file to archive. File: {}", file);
            return;
        }

        final Optional<String> archiveId =
            Optional.fromNullable(fileArchivingHelper.moveToArchiveStorage(contentId.get()));

        if (archiveId.isPresent()) {

            saveMovementToArchive(metaData, archiveId.get());

        } else if (cloudStorageService.existsAtCloud(new CloudStorageItemReference(
            amazonPropertiesProvider.getArchiveBucket(),
            contentId.get()
        ))) { //For test data

            LOGGER.info(
                "*** Skip move file to archive storage. File already present at archive content id: {}",
                contentId.get()
            );
            saveMovementToArchive(metaData, contentId.get());

        } else if (cloudStorageService.existsAtCloud(new CloudStorageItemReference(
            amazonPropertiesProvider.getActiveBucket(),
            contentId.get()
        ))) { //For test data

            LOGGER.info("*** File already present in analysable storage content id: {}", contentId.get());
            metaData.getStorageData().setStorageStatus(UNARCHIVED);
            fileMetaDataRepository.save(metaData);
        } else {
            LOGGER.error(
                "*** File is not present nether in archive nor analysable storage. File: {}, Content Id: {}",
                file,
                contentId.get()
            );
        }
    }

    private void saveMovementToArchive(ActiveFileMetaData metaData, String archiveId) {
        metaData.setContentId(null);
        metaData.setArchiveId(archiveId);
        metaData.getStorageData().setStorageStatus(ARCHIVED);
        metaData.getStorageData().setLastUnarchiveTimestamp(null);
        metaData.getStorageData().setArchivedDownloadOnly(false);
        metaData.getStorageData().setArchivedDownloadCharged(null);
        fileMetaDataRepository.save(metaData);
        fileAccessLogService.logFileArchiveConfirm(metaData.getId());
    }

    private void saveMovementToStorage(ActiveFileMetaData metaData, String contentId) {
        metaData.setContentId(contentId);
        metaData.setArchiveId(null);
        metaData.setLastAccess(new Date());
        metaData.getStorageData().setStorageStatus(UNARCHIVED);
        fileMetaDataRepository.save(metaData);
    }

    private Map<Long, Set<Long>> processJobForNotify(FileDownloadJob job) {
        final Collection<FilesDownloadGroup> groups = downloadGroupRepository.findByJob(Sets.newHashSet(job));
        final Map<Long, Set<Long>> userFilesMapToNotify = new HashMap<>();

        for (FilesDownloadGroup group : groups) {
            if (isGroupCompleted(group)) {
                for (User user : group.getNotifiers()) {
                    final Set<Long> files = group.getJobs()
                        .stream()
                        .map(input -> input.fileMetaData.getId())
                        .collect(toSet());
                    userFilesMapToNotify.merge(user.getId(), files, Sets::union);
                }

                downloadGroupRepository.delete(group);
                deleteJobsForGroup(group);
            }
        }

        downloadGroupRepository.flush();
        fileDownloadJobRepository.flush();

        return userFilesMapToNotify;
    }

    private void deleteJobsForGroup(FilesDownloadGroup group) {
        final Set<FileDownloadJob> jobs = group.getJobs();
        final ImmutableList.Builder<FileDownloadJob> toDeleteBuilder = ImmutableList.builder();

        for (FileDownloadJob job : jobs) {
            final List<FilesDownloadGroup> byJob = downloadGroupRepository.findByJob(job);
            byJob.remove(group);
            if (byJob.isEmpty()) {
                toDeleteBuilder.add(job);
            }
        }

        final ImmutableList<FileDownloadJob> toDeleteList = toDeleteBuilder.build();
        fileDownloadJobRepository.delete(toDeleteList);
        group.getJobs().removeAll(toDeleteList);
    }


    private void sendDownloadReadyNotifications(long actor, Collection<Long> files) {
        notifier.sendFileReadyToDownloadNotification(actor, files);
        inboxNotifier.notify(actor, actor, "Files are ready for downloading.");
    }

    private boolean isGroupCompleted(FilesDownloadGroup group) {
        boolean completed = true;
        for (FileDownloadJob job : group.getJobs()) {
            if (!job.isCompleted()) {
                completed = false;
            }
        }
        return completed;
    }
}
