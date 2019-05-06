package com.infoclinika.mssharing.model.internal.helper;

import com.amazonaws.services.s3.AmazonS3;
import com.infoclinika.mssharing.model.helper.CloudFileHelper;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author timofey.kasyanov 25/11/2015.
 */
@Component
public class CloudFileHelperImpl implements CloudFileHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudFileHelperImpl.class);

    @Inject
    private StoredObjectPaths storedObjectPaths;
    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    @Override
    public long getFileSize(String fileKey) {

        final AmazonS3 amazonS3 = cloudStorageClientsProvider.getAmazonS3Client();

        try {
            return amazonS3.getObjectMetadata(
                storedObjectPaths.getRawFilesBucket(),
                fileKey
            ).getContentLength();
        } catch (Exception e) {
            LOGGER.warn(
                "Couldn't get file from active bucket. Will try archive bucket. File path: {}",
                fileKey,
                e
            );
        }

        try {
            return amazonS3.getObjectMetadata(
                storedObjectPaths.getArchiveBucket(),
                fileKey
            ).getContentLength();
        } catch (Exception e) {
            LOGGER.warn("Couldn't get file from archive bucket either. File path: {}", fileKey, e);
        }

        throw new RuntimeException("Couldn't get file size. File path: " + fileKey);
    }
}
