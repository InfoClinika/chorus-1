package com.infoclinika.mssharing.clients.common.transfer.impl;

import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.infoclinika.mssharing.clients.common.util.PauseSemaphore;
import com.infoclinika.mssharing.clients.common.util.S3ConnectionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *     date: 25.02.14.
 */
public class UploadCallable implements Callable<PartETag> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadCallable.class);

    private final S3ClientProvider s3ClientProvider;
    private final UploadPartRequest request;
    private final UploadItem uploadItem;
    private volatile int retryCount;
    private volatile int timeoutBetweenRetries;
    private PauseSemaphore pauseSemaphore;

    public UploadCallable(UploadItem uploadItem, S3ClientProvider s3ClientProvider, UploadPartRequest request,
                          int retryCount, int timeoutBetweenRetries, PauseSemaphore pauseSemaphore) {
        this.uploadItem = checkNotNull(uploadItem);
        this.s3ClientProvider = checkNotNull(s3ClientProvider);
        this.request = checkNotNull(request);
        checkArgument(retryCount >= 0);
        this.retryCount = retryCount;
        this.timeoutBetweenRetries = timeoutBetweenRetries;
        this.pauseSemaphore = pauseSemaphore;
    }

    @Override
    public PartETag call() throws Exception {

        while (retryCount > 0) {

            pauseSemaphore.waitIfPaused();

            S3ConnectionChecker.waitForS3Connection();

            try {

                LOGGER.info("Start uploading a part. Upload item: {}, Part number: {}",
                    request.getKey(), request.getPartNumber()
                );

                final UploadPartResult result = s3ClientProvider.get().uploadPart(request);

                LOGGER.info("Part uploaded successfully. Upload item: {}, Part number: {}",
                    request.getKey(), request.getPartNumber()
                );

                return result.getPartETag();

            } catch (Exception ex) {
                if (uploadItem.getState() == UploadItemState.CANCELED) {

                    LOGGER.info("Upload has been canceled. Upload item: {}, Part number: {}", uploadItem.getKey(),
                        request.getPartNumber()
                    );

                    break;

                }

                LOGGER.error("Error occurred while uploading a part. Upload item: {}, Part number: {}," +
                        " Upload item state: {}", request.getKey(), request.getPartNumber(),
                    uploadItem.getState()
                );
                LOGGER.info("Part {}. Retry attempts left: {}", request.getPartNumber(), retryCount, ex);

                --retryCount;

                LOGGER.info("Part {}. sleep before retry. Time to sleep: {}",
                    request.getPartNumber(), timeoutBetweenRetries
                );
                Thread.sleep(timeoutBetweenRetries);
            }

        }

        LOGGER.error("Cannot upload part. Upload item: {}, Part number: {}",
            uploadItem.getKey(), request.getPartNumber()
        );
        throw new RuntimeException("Cannot upload part. Upload item: " + uploadItem.getKey() + ", Part number: "
            + request.getPartNumber());
    }
}
