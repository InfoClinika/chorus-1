package com.infoclinika.mssharing.model.internal.write;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.helper.FileArchivingHelper;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;

/**
 * @author Herman Zamula
 */
@Component
public class FileArchivingHelperImpl implements FileArchivingHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileArchivingHelperImpl.class);
    private static final Set<String> ARCHIVE_STORAGE_CLASSES = Sets.newHashSet(StorageClass.Glacier.toString());
    private final Predicate<S3ObjectSummary> isGlacierStorageClass = input ->
        input.getSize() > 0 && ARCHIVE_STORAGE_CLASSES.contains(input.getStorageClass());

    @Inject
    private StoredObjectPaths paths;

    @Inject
    private FeaturesHelper featuresHelper;

    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;

    private static AmazonS3 client;
    private String analysableStorageBucket;
    private String archiveStorageBucket;

    @PostConstruct
    public void initializeAmazonClient() {
        client = cloudStorageClientsProvider.getAmazonS3Client();

        analysableStorageBucket = paths.getRawFilesBucket();
        archiveStorageBucket = paths.getArchiveBucket();
    }

    @Override
    public String moveToArchiveStorage(String filePath) {
        LOGGER.info("** Moving file '{}' to archive storage...", filePath);
        try {
            return move(analysableStorageBucket, archiveStorageBucket, filePath);
        } catch (AmazonS3Exception ex) {
            LOGGER.error("*** There are an error occurred when trying to move file to archive storage.", ex);
            // throw new IllegalStateException("*** There are an error occurred when trying to move file to archive
            // storage. File: " + filePath, ex);
            return null;
        }
    }

    @Override
    public boolean isOnGoingToRestore(String archiveId) {
        LOGGER.info("** Check if file '{}' is already going to restore...", archiveId);
        final ObjectMetadata metadata = client.getObjectMetadata(archiveStorageBucket, archiveId);

        return metadata != null && metadata.getOngoingRestore() != null && metadata.getOngoingRestore();
    }

    @Override
    public boolean requestUnarchive(String archiveId, boolean forDownloadOnly) {
        checkState(
            featuresHelper.isEnabled(ApplicationFeature.GLACIER),
            "Unarchive operation is not possible because GLACIER feature is not enabled."
        );
        checkNotNull(archiveId, "Archive id is not specified.");
        LOGGER.info("** Requesting unarchive file '{}'...", archiveId);
        try {
            if (!archiveHasGlacierStorageClass(archiveId)) {
                LOGGER.info(
                    "*** Skipping object restoration from archive. Probably, object has been already restored or " +
                        "newly uploaded. Archive id: {}",
                    archiveId
                );

                return true;
            }

            final int expirationInDays = forDownloadOnly
                ? billingPropertiesProvider.getUnarchivedForDownloadMaxDays()
                : billingPropertiesProvider.getUnarchiveExpirationInDays();
            client.restoreObject(archiveStorageBucket, archiveId, expirationInDays);
        } catch (AmazonS3Exception ex) {
            LOGGER.error("*** There are an error occurred when request file restoration.", ex);

            return false;
        }

        return true;
    }

    private boolean archiveHasGlacierStorageClass(String archiveId) {
        List<S3ObjectSummary> objects = client.listObjects(archiveStorageBucket, archiveId).getObjectSummaries();
        if (isEmpty(objects)) {
            //throw new IllegalStateException("Element with key '" + archiveId + "' not found in archive");
            LOGGER.error("Element with key '{}' not found in archive", archiveId);
        }

        return from(objects).firstMatch(isGlacierStorageClass).isPresent();
    }

    @Override
    public boolean isArchiveReadyToRestore(final String archiveId) {
        LOGGER.debug("** Checking file ready to restore: {}", archiveId);
        try {
            final ObjectMetadata metadata = client.getObjectMetadata(archiveStorageBucket, archiveId);

            return !fromNullable(metadata.getOngoingRestore()).or(() -> archiveHasGlacierStorageClass(archiveId));
        } catch (AmazonS3Exception ex) {
            LOGGER.error(
                "*** There are an error occurred when trying to check file restoration readiness:\n {}",
                ex.getMessage()
            );

            return false;
        }
    }

    @Override
    public String moveToAnalyzableStorage(String archiveId) {
        LOGGER.info("** Moving to analyzable storage: {}", archiveId);
        try {
            return move(archiveStorageBucket, analysableStorageBucket, archiveId);
        } catch (AmazonS3Exception ex) {
            LOGGER.error("*** Moving file to analysable storage failed.", ex);
            throw new IllegalStateException(
                "*** There are an error occurred when trying to move file to analysable storage. File: " + archiveId,
                ex
            );
        }
    }

    @Override
    public String moveArchivedFileToTempStorage(String archiveId, String destination) {
        checkNotNull(archiveId);
        LOGGER.info("** Moving archive to temp storage: {}", archiveId);
        try {
            return copyToTemp(archiveStorageBucket, archiveId, destination);
        } catch (AmazonS3Exception ex) {
            LOGGER.error("*** Moving file to temp storage failed.", ex);
            throw new IllegalStateException(
                "*** There are an error occurred when trying to move archive to temp storage. File: " + archiveId,
                ex
            );
        }
    }

    private String copyToTemp(String fileBucket, String filePath, String destinationKey) {
        client.copyObject(fileBucket, filePath, analysableStorageBucket, destinationKey);
        return destinationKey;
    }

    @Override
    public String moveNotArchivedFileToTempStorage(String filePath, String destination) {
        checkNotNull(filePath);
        LOGGER.info("** Moving file to temp storage: {}", filePath);
        try {
            return copyToTemp(analysableStorageBucket, filePath, destination);
        } catch (AmazonS3Exception ex) {
            LOGGER.error("*** Moving file to temp storage failed.", ex);
            throw new IllegalStateException(
                "*** There are an error occurred when trying to move archive to temp storage. File: " + filePath,
                ex
            );
        }
    }

    @Override
    public boolean isArchived(String archiveId) {
        checkNotNull(archiveId);
        return archiveHasGlacierStorageClass(archiveId);
    }

    private String move(String sourceBucket, String destinationBucket, String path) {
        client.copyObject(sourceBucket, path, destinationBucket, path);
        client.deleteObject(sourceBucket, path);
        return path;
    }
}
