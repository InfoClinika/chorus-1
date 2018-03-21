package com.infoclinika.mssharing.model.internal.helper;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.infoclinika.mssharing.model.helper.CloudFileHelper;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author timofey.kasyanov 25/11/2015.
 */
@Component
public class CloudFileHelperImpl implements CloudFileHelper {

    private final static Logger LOGGER = Logger.getLogger(CloudFileHelperImpl.class);

    @Inject
    private StoredObjectPaths storedObjectPaths;

    @Override
    public long getFileSize(String fileKey) {

        final String amazonKey = storedObjectPaths.getAmazonKey();
        final String amazonSecret = storedObjectPaths.getAmazonSecret();
        final BasicAWSCredentials credentials = new BasicAWSCredentials(amazonKey, amazonSecret);
        final AmazonS3 amazonS3 = new AmazonS3Client(credentials);

        try {
            return amazonS3.getObjectMetadata(
                    storedObjectPaths.getRawFilesBucket(),
                    fileKey
            ).getContentLength();
        } catch (Exception ignore) {
            LOGGER.warn("Couldn't get file from active bucket. Will try archive bucket. File path: " + fileKey, ignore);
        }

        try {
            return amazonS3.getObjectMetadata(
                    storedObjectPaths.getArchiveBucket(),
                    fileKey
            ).getContentLength();
        } catch (Exception ignore) {
            LOGGER.warn("Couldn't get file from archive bucket either. File path: " + fileKey, ignore);
        }

        throw new RuntimeException("Couldn't get file size. File path: " + fileKey);
    }
}
