package com.infoclinika.mssharing.model.internal.helper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author timofey.kasyanov
 *         date: 27.02.14.
 */
@Component
public class AbortMultipartHelper {

    private static final Logger LOGGER = Logger.getLogger(AbortMultipartHelper.class);

    @Inject
    private StoredObjectPaths storedObjectPaths;

    public void abortMultipartUploads(Date upToDate){

        LOGGER.info("Abort multipart uploads initiated before " + upToDate);

        final String amazonKey = storedObjectPaths.getAmazonKey();
        final String amazonSecret = storedObjectPaths.getAmazonSecret();
        final String bucket = storedObjectPaths.getRawFilesBucket();
        final BasicAWSCredentials credentials = new BasicAWSCredentials(amazonKey, amazonSecret);
        final TransferManager transferManager = new TransferManager(credentials);

        try {
            transferManager.abortMultipartUploads(bucket, upToDate);
        } catch (AmazonClientException e) {
            LOGGER.error("Cannot abort multipart uploads", e);
        }

        transferManager.shutdownNow();

    }

}
