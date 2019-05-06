package com.infoclinika.mssharing.model.internal.helper;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author timofei.kasianov 6/25/18
 */
@Component
public class ExportedFilesCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportedFilesCleaner.class);

    private final StoredObjectPaths storedObjectPaths;
    private final CloudStorageClientsProvider cloudStorageClientsProvider;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    private AmazonS3 amazonS3 = null;

    @Inject
    public ExportedFilesCleaner(StoredObjectPaths storedObjectPaths,
                                CloudStorageClientsProvider cloudStorageClientsProvider,
                                AmazonPropertiesProvider amazonPropertiesProvider) {
        this.storedObjectPaths = storedObjectPaths;
        this.cloudStorageClientsProvider = cloudStorageClientsProvider;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    public void removeOldFiles() {

        final Date currentDate = new Date();

        LOGGER.info("Removing old HDF5 files... " + currentDate);

        final AmazonS3 amazonS3 = getAmazonS3();
        final String bucket = storedObjectPaths.getRawFilesBucket();
        final String hdf5FilesPrefix = storedObjectPaths.getHdf5FilesPrefix();

        removeOldFilesWithPrefix(amazonS3, bucket, hdf5FilesPrefix, currentDate);
    }

    private void removeOldFilesWithPrefix(AmazonS3 amazonS3, String bucket, String prefix, Date currentDate) {
        final ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucket)
            .withPrefix(prefix);
        final ObjectListing initialListing = amazonS3.listObjects(listObjectsRequest);

        initialListing.getObjectSummaries().forEach(summary -> removeFileIfOldEnough(amazonS3, currentDate, summary));

        if (initialListing.isTruncated()) {
            ObjectListing nextListing = initialListing;
            while (nextListing.isTruncated()) {
                nextListing = amazonS3.listNextBatchOfObjects(nextListing);
                nextListing.getObjectSummaries()
                    .forEach(summary -> removeFileIfOldEnough(amazonS3, currentDate, summary));
            }
        }
    }

    private void removeFileIfOldEnough(AmazonS3 amazonS3, Date currentDate, S3ObjectSummary summary) {
        try {
            final Date lastModified = summary.getLastModified();
            final long fileAge = currentDate.getTime() - lastModified.getTime();
            if (fileAge > amazonPropertiesProvider.getExportedFileMaxAge()) {
                amazonS3.deleteObject(summary.getBucketName(), summary.getKey());
            }
        } catch (Exception ex) {
            LOGGER.warn("Couldn't remove an old file from " + summary.getBucketName() + " | " + summary.getKey());
        }
    }

    private AmazonS3 getAmazonS3() {
        if (amazonS3 == null) {
            amazonS3 = cloudStorageClientsProvider.getAmazonS3Client();
        }
        return amazonS3;
    }
}
