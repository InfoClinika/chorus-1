package com.infoclinika.mssharing.model.internal.helper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author timofey.kasyanov
 *     date: 27.02.14.
 */
@Component
public class AbortMultipartHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbortMultipartHelper.class);

    @Inject
    private StoredObjectPaths storedObjectPaths;
    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    public void abortMultipartUploads(Date upToDate) {

        LOGGER.info("Abort multipart uploads initiated before {}", upToDate);

        final AmazonS3 amazonS3Client = cloudStorageClientsProvider.getAmazonS3Client();
        final String bucket = storedObjectPaths.getRawFilesBucket();
        final TransferManager transferManager = TransferManagerBuilder.standard()
            .withS3Client(amazonS3Client)
            .build();

        try {
            transferManager.abortMultipartUploads(bucket, upToDate);
        } catch (AmazonClientException e) {
            LOGGER.error("Cannot abort multipart uploads", e);
        }

        transferManager.shutdownNow();

    }

}
