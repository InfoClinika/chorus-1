package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.helper.FileArchivingHelper;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.write.ArchiveSynchronizationManagement;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yevhen.panko
 */

@Service
public class ArchiveSynchronizationManagementImpl
    implements ArchiveSynchronizationManagement<ArchiveSynchronizationManagementImpl.Status> {

    private final FileMetaDataRepository fileMetaDataRepository;
    private final FileArchivingHelper fileArchivingHelper;
    private final CloudStorageClientsProvider cloudStorageClientsProvider;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public ArchiveSynchronizationManagementImpl(FileMetaDataRepository fileMetaDataRepository,
                                                FileArchivingHelper fileArchivingHelper,
                                                CloudStorageClientsProvider cloudStorageClientsProvider,
                                                AmazonPropertiesProvider amazonPropertiesProvider) {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.fileArchivingHelper = fileArchivingHelper;
        this.cloudStorageClientsProvider = cloudStorageClientsProvider;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    private enum State {
        IN_PROGRESS,
        CANCELED,
        ERROR,
        FINISHED
    }

    private Status status;

    private ScheduledExecutorService scheduledExecutorService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveSynchronizationManagementImpl.class);

    @Override
    public void synchronizeS3StateWithDB() {
        synchronizeS3StateWithDB(
            amazonPropertiesProvider.getActiveBucket(),
            amazonPropertiesProvider.getArchiveBucket()
        );
    }

    @Override
    public void synchronizeS3StateWithDB(String activeBucket, String archiveBucket) {
        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();

        status = new Status(State.IN_PROGRESS);

        LOGGER.info("Start looking for files in {} directory", archiveBucket);
        final List<CloudStorageItemReference> archivedFiles = cloudStorageService.list(
            archiveBucket,
            null,
            Optional.of(Calendar.getInstance().getTime())
        );

        final List<CloudStorageItemReference> missedFiles = new ArrayList<>();

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            boolean hasSomeFilesToMove = false;
            status.state = State.IN_PROGRESS;

            try {
                LOGGER.info("Found {} files", archivedFiles.size());
                for (CloudStorageItemReference archivedFile : archivedFiles) {

                    final String archiveId = archivedFile.getKey();

                    final List<ActiveFileMetaData> byContentId = fileMetaDataRepository.findByContentId(archiveId);
                    final ActiveFileMetaData byArchiveId = fileMetaDataRepository.findByArchiveId(archiveId);

                    //File is not archived on production
                    if (byContentId != null && byArchiveId == null) {
                        if (fileArchivingHelper.isArchiveReadyToRestore(archiveId)) {
                            moveFileToActiveBucket(activeBucket, archiveBucket, cloudStorageService, archivedFile);
                        } else {
                            if (!fileArchivingHelper.isOnGoingToRestore(archiveId)) {
                                fileArchivingHelper.requestUnarchive(archiveId, false);
                            }
                            hasSomeFilesToMove = true;
                        }
                    } else if (byContentId == null && byArchiveId == null) {
                        missedFiles.add(archivedFile);
                    }
                }
            } catch (Exception e) {
                final String stackTraceAsString = Throwables.getStackTraceAsString(e);
                LOGGER.error(stackTraceAsString);
                status.state = State.ERROR;
                status.error = stackTraceAsString;

                scheduledExecutorService.shutdown();
                LOGGER.info("Synchronization was stopped due to an error");
            }

            if (!hasSomeFilesToMove) {
                scheduledExecutorService.shutdown();

                if (!missedFiles.isEmpty()) {
                    for (CloudStorageItemReference missedFile : missedFiles) {
                        LOGGER.error("There is no file in DB: {}", missedFile.getKey());
                    }
                }

                status.state = State.FINISHED;
                LOGGER.info("Synchronization was completed successfully");
            }
        }, 0, 3, TimeUnit.HOURS);
    }

    @Override
    public void cancelSynchronization() {
        status.state = State.CANCELED;
        scheduledExecutorService.shutdown();
        LOGGER.info("Synchronization was canceled by user");
    }

    @Override
    public Status checkSynchronizationState() {
        return status;
    }

    private void moveFileToActiveBucket(String activeBucket,
                                        String archiveBucket,
                                        CloudStorageService cloudStorageService,
                                        CloudStorageItemReference rawFile) {
        LOGGER.info("Start moving file {} from {} to {}", rawFile.getKey(), archiveBucket, activeBucket);
        final CloudStorageItemReference copy = new CloudStorageItemReference(
            amazonPropertiesProvider.getActiveBucket(),
            rawFile.getKey()
        );
        cloudStorageService.copy(rawFile, copy);
        LOGGER.info("File was copied successfully");

        if (cloudStorageService.existsAtCloud(copy)) {
            LOGGER.info("Delete file {} from {}", rawFile.getKey(), archiveBucket);
            cloudStorageService.deleteFromCloud(rawFile);
            LOGGER.info("File was deleted successfully");
        }
    }

    public class Status {
        public State state;
        public String error;

        public Status(State state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "Status{" +
                "state=" + state +
                ", error='" + error + '\'' +
                '}';
        }
    }
}
