package com.infoclinika.mssharing.upload.common.transfer.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *         date: 25.02.14.
 */
public class UploadCallable implements Callable<PartETag> {
    private static final Logger LOGGER = Logger.getLogger(UploadCallable.class);
    private static final String PART_NUMBER = ", Part number: ";

    private final AmazonS3 amazonS3;
    private final UploadPartRequest request;
    private final UploadItem uploadItem;
    private volatile int retryCount;

    public UploadCallable(UploadItem uploadItem, AmazonS3 amazonS3, UploadPartRequest request, int retryCount) {
        this.uploadItem = checkNotNull(uploadItem);
        this.amazonS3 = checkNotNull(amazonS3);
        this.request = checkNotNull(request);
        checkArgument(retryCount >= 0);
        this.retryCount = retryCount;
    }

    @Override
    public PartETag call() throws Exception {

        while (retryCount > 0) {

            try {

                LOGGER.info("Start uploading a part. Upload item: " + request.getKey()
                        + PART_NUMBER + request.getPartNumber());

                final UploadPartResult result = amazonS3.uploadPart(request);

                LOGGER.info("Part uploaded successfully. Upload item: " + request.getKey()
                        + PART_NUMBER + request.getPartNumber());

                return result.getPartETag();

            } catch (Exception ex) {

                if (uploadItem.getState() == UploadItemState.CANCELED) {

                    LOGGER.info("Upload has been canceled. Upload item: " + uploadItem.getKey()
                            + PART_NUMBER + request.getPartNumber());

                    break;

                }

                LOGGER.error("Error occurred while upload a part. Upload item: " + request.getKey()
                        + PART_NUMBER + request.getPartNumber()
                        + ", Upload item state: " + uploadItem.getState());
                LOGGER.info("Retry attempts left: " + retryCount, ex);

                --retryCount;
            }

        }

        LOGGER.error("Cannot upload part. Upload item: " + uploadItem.getKey()
                + PART_NUMBER + request.getPartNumber());
        throw new RuntimeException("Cannot upload part. Upload item: " + uploadItem.getKey()
                + PART_NUMBER + request.getPartNumber());
    }
}
