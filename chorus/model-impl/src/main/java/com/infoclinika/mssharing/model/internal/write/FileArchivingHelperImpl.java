package com.infoclinika.mssharing.model.internal.write;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.google.common.base.Predicate;
import com.infoclinika.mssharing.model.helper.FileArchivingHelper;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.internal.features.FeaturesReader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

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

    private static final int MAX_HTTP_CONNECTIONS = 100;

    private final Predicate<S3ObjectSummary> isGlacierStorageClass = input ->
            input.getSize() > 0 && input.getStorageClass().equals(StorageClass.Glacier.toString());

    @Value("${billing.storage.archive.restore.expiration}")
    private int unarchiveExpirationInDays;
    @Value("${billing.storage.archive.download.restore.expiration}")
    private int downloadExpirationInDays;
    @Inject
    private StoredObjectPaths paths;
    @Inject
    private FeaturesReader featuresReader;
    private static AmazonS3Client client;

    private static final Logger LOG = Logger.getLogger(FileArchivingHelperImpl.class);
    private String analysableStorageBucket;
    private String archiveStorageBucket;

    @PostConstruct
    public void initializeAmazonClient() {
        final BasicAWSCredentials credentials = new BasicAWSCredentials(paths.getAmazonKey(), paths.getAmazonSecret());
        final ClientConfiguration modifiedConf = new ClientConfiguration();
        modifiedConf.setMaxConnections(MAX_HTTP_CONNECTIONS);
        modifiedConf.setMaxErrorRetry(10);
        client = new AmazonS3Client(credentials, modifiedConf);

        analysableStorageBucket = paths.getRawFilesBucket();
        archiveStorageBucket = paths.getArchiveBucket();
    }

    @Override
    public String moveToArchiveStorage(String filePath) {
        LOG.info("** Moving file '" + filePath + "' to archive storage...");
        try {
            return move(analysableStorageBucket, archiveStorageBucket, filePath);
        } catch (AmazonS3Exception ex) {
            LOG.error("*** There are an error occurred when trying to move file to archive storage.", ex);
            //throw new IllegalStateException("*** There are an error occurred when trying to move file to archive storage. File: " + filePath, ex);
            return null;
        }
    }

    @Override
    public boolean isOnGoingToRestore(String archiveId) {
        LOG.info("** Check if file '" + archiveId + "' is already going to restore...");
        final ObjectMetadata metadata = client.getObjectMetadata(archiveStorageBucket, archiveId);

        return metadata != null && metadata.getOngoingRestore() != null && metadata.getOngoingRestore();
    }

    @Override
    public boolean requestUnarchive(String archiveId, boolean forDownloadOnly) {
        checkState(featuresReader.isFeatureEnabled(ApplicationFeature.GLACIER), "Unarchive operation is not possible because GLACIER feature is not enabled.");
        checkNotNull(archiveId, "Archive id is not specified.");
        LOG.info("** Requesting unarchive file '" + archiveId + "'...");
        try {
            if (!archiveHasGlacierStorageClass(archiveId)) {
                LOG.info("*** Skipping object restoration from archive. Probably, object has been already restored or newly uploaded. Archive id: " + archiveId);
                return true;
            }
            final int expirationInDays = forDownloadOnly ? downloadExpirationInDays : unarchiveExpirationInDays;
            client.restoreObject(archiveStorageBucket, archiveId, expirationInDays);
        } catch (AmazonS3Exception ex) {
            LOG.error("*** There are an error occurred when request file restoration.", ex);
            return false;
        }
        return true;
    }

    private boolean archiveHasGlacierStorageClass(String archiveId) {
        List<S3ObjectSummary> objects = client.listObjects(archiveStorageBucket, archiveId).getObjectSummaries();
        if (isEmpty(objects)) {
            //throw new IllegalStateException("Element with key '" + archiveId + "' not found in archive");
            LOG.error("Element with key '" + archiveId + "' not found in archive");
        }
        return from(objects).firstMatch(isGlacierStorageClass).isPresent();
    }

    @Override
    public boolean isArchiveReadyToRestore(final String archiveId) {
        LOG.debug("** Checking file ready to restore: " + archiveId);
        try {
            final ObjectMetadata metadata = client.getObjectMetadata(archiveStorageBucket, archiveId);
            return !fromNullable(metadata.getOngoingRestore()).or(() -> archiveHasGlacierStorageClass(archiveId));
        } catch (AmazonS3Exception ex) {
            LOG.error("*** There are an error occurred when trying to check file restoration readiness:\n " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String moveToAnalyzableStorage(String archiveId) {
        LOG.info("** Moving to analyzable storage: " + archiveId);
        try {
            return move(archiveStorageBucket, analysableStorageBucket, archiveId);
        } catch (AmazonS3Exception ex) {
            LOG.error("*** Moving file to analysable storage failed.", ex);
            throw new IllegalStateException("*** There are an error occurred when trying to move file to analysable storage. File: " + archiveId, ex);
        }
    }

    @Override
    public String moveArchivedFileToTempStorage(String archiveId, String destination) {
        checkNotNull(archiveId);
        LOG.info("** Moving archive to temp storage: " + archiveId);
        try {
            return copyToTemp(archiveStorageBucket, archiveId, destination);
        } catch (AmazonS3Exception ex) {
            LOG.error("*** Moving file to temp storage failed.", ex);
            throw new IllegalStateException("*** There are an error occurred when trying to move archive to temp storage. File: " + archiveId, ex);
        }
    }

    private String copyToTemp(String fileBucket, String filePath, String destinationKey) {
        client.copyObject(fileBucket, filePath, analysableStorageBucket, destinationKey);
        return destinationKey;
    }

    @Override
    public String moveNotArchivedFileToTempStorage(String filePath, String destination) {
        checkNotNull(filePath);
        LOG.info("** Moving file to temp storage: " + filePath);
        try {
            return copyToTemp(analysableStorageBucket, filePath, destination);
        } catch (AmazonS3Exception ex) {
            LOG.error("*** Moving file to temp storage failed.", ex);
            throw new IllegalStateException("*** There are an error occurred when trying to move archive to temp storage. File: " + filePath, ex);
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
