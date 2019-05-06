package com.infoclinika.mssharing.clients.common.transfer.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.*;
import com.infoclinika.mssharing.clients.common.util.PauseSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.amazonaws.event.ProgressEventType.*;
import static com.amazonaws.services.s3.Headers.S3_CANNED_ACL;
import static com.amazonaws.services.s3.Headers.SERVER_SIDE_ENCRYPTION;
import static com.amazonaws.services.s3.model.ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *     date: 24.02.14.
 */
public class MultipartUpload {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartUpload.class);
    private static final Comparator<PartETag> PART_E_TAG_COMPARATOR = Comparator.comparingInt(PartETag::getPartNumber);

    private final UploadItem uploadItem;
    private final UploaderConfiguration configuration;
    private final ExecutorService executor;
    private final List<Future<PartETag>> futures = newArrayList();
    private final ItemStateListener itemStateListener;
    private final PauseSemaphore pauseSemaphore;
    private String uploadId;
    private boolean started = false;

    public MultipartUpload(UploadItem uploadItem,
                           UploaderConfiguration configuration,
                           ExecutorService executor,
                           ItemStateListener itemStateListener,
                           PauseSemaphore pauseSemaphore) {
        this.uploadItem = uploadItem;
        this.configuration = configuration;
        this.executor = executor;
        this.itemStateListener = itemStateListener;
        this.pauseSemaphore = pauseSemaphore;
    }

    public void upload(ProgressListener progressListener) {

        try {
            final S3ClientProvider s3ClientProvider = configuration.getS3ClientProvider();
            final String bucket = configuration.getBucket();
            final String key = uploadItem.getKey();

            uploadId = initialize();
            List<UploadCallable> uploadCallableList = sliceToParts(uploadId, progressListener);

            List<PartETag> partETags = new ArrayList<>();
            try {
                for (UploadCallable uploadPartCallable : uploadCallableList) {
                    futures.add(executor.submit(uploadPartCallable));
                }

                for (Future<PartETag> s3eTagFuture : futures) {
                    partETags.add(s3eTagFuture.get());
                }

                CompleteMultipartUploadRequest compRequest =
                    new CompleteMultipartUploadRequest(bucket, key, uploadId, partETags);
                s3ClientProvider.get().completeMultipartUpload(compRequest);

                uploadItem.setState(UploadItemState.COMPLETED);
                itemStateListener.stateChanged(this, UploadItemState.COMPLETED);
                ProgressEvent event =
                    new ProgressEvent(TRANSFER_COMPLETED_EVENT, 0);
                progressListener.progressChanged(event);
            } catch (Exception e) {
                try {
                    LOGGER.error("Multipart upload failed. Aborting.", e);
                    s3ClientProvider.get().abortMultipartUpload(new AbortMultipartUploadRequest(bucket, key, uploadId));
                } catch (AmazonClientException ex) {
                    LOGGER.error("Abort multipart upload failed.", ex);
                }
                throw e;
            }
        } catch (Exception ex) {

            if (uploadItem.getState() != UploadItemState.CANCELED) {

                uploadItem.setState(UploadItemState.ERROR);
                itemStateListener.stateChanged(this, UploadItemState.ERROR);

                LOGGER.error("Error during multipart upload", ex);

            }
            LOGGER.error("Error during multipart upload, it was canceled", ex);
            throw new RuntimeException("Error during multipart upload, it was canceled");
        }
    }

    public void cancel() {

        LOGGER.info("Canceling multipart upload. Upload item: {}", uploadItem.getKey());

        try {

            if (uploadItem.getState() == UploadItemState.UPLOADING) {
                abortMultipart();
            }

            uploadItem.setState(UploadItemState.CANCELED);
            itemStateListener.stateChanged(this, UploadItemState.CANCELED);

            for (Future<PartETag> future : futures) {
                future.cancel(true);
            }

            LOGGER.info("Multipart upload canceled successfully. Upload item: {}", uploadItem.getKey());

        } catch (Exception e) {
            LOGGER.error("Error during canceling multipart upload for item: {}", uploadItem.getKey(), e);
        }

    }


    private String initialize() {

        LOGGER.info("Initialize multipart upload. Upload item: {}", uploadItem.getKey());

        final S3ClientProvider s3ClientProvider = configuration.getS3ClientProvider();
        final String bucket = configuration.getBucket();
        final String key = uploadItem.getKey();

        final ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setHeader(S3_CANNED_ACL, "private");

        //TODO[Alexander Serebriyan]: try whether it works with roles without these headers. Bring back if not.
        // objectMetadata.setHeader("Authorization", uploadItem.getAuthorization());
        // objectMetadata.setHeader(S3_ALTERNATE_DATE, uploadItem.getDate());

        if (uploadItem.isServerSideEncryption()) {
            objectMetadata.setHeader(SERVER_SIDE_ENCRYPTION, AES_256_SERVER_SIDE_ENCRYPTION);
        }

        final InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(
            bucket,
            key,
            objectMetadata
        );

        try {

            final InitiateMultipartUploadResult response = s3ClientProvider.get().initiateMultipartUpload(request);
            final String uploadId = response.getUploadId();

            LOGGER.info("Multipart upload initialized. Upload id: {}, Upload item: {}", uploadId, uploadItem.getKey());

            return uploadId;

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw new RuntimeException("Cannot initiate multipart upload. Upload item: " + uploadItem.getKey());
        }

    }

    private List<UploadCallable> sliceToParts(String uploadId, ProgressListener externalListener) {

        LOGGER.info("Slice to parts. Upload item: {}", uploadItem.getKey());

        final List<UploadCallable> runnableList = newArrayList();

        final File file = uploadItem.getFile();
        final long fileSize = file.length();
        final String bucket = configuration.getBucket();
        final String key = uploadItem.getKey();

        final long constPartSize = configuration.getPartSize();
        long partSize = constPartSize;
        long filePosition = 0;

        for (int i = 1; filePosition < fileSize; ++i) {

            partSize = Math.min(partSize, (fileSize - filePosition));

            final boolean isLastPart = partSize < constPartSize || (filePosition + partSize + 1) > fileSize;
            final PerPartUploadListener uploadListener =
                new PerPartUploadListener(externalListener, i);

            final UploadPartRequest request = new UploadPartRequest()
                .withBucketName(bucket)
                .withKey(key)
                .withUploadId(uploadId)
                .withFileOffset(filePosition)
                .withFile(file)
                .withPartSize(partSize)
                .withPartNumber(i)
                .withLastPart(isLastPart)
                .withGeneralProgressListener(uploadListener);

            LOGGER.info("Part number: {}, Part size: {}, File offset: {}, Is last part: ", i, partSize, filePosition,
                isLastPart
            );

            filePosition += partSize;

            final UploadCallable uploadCallable = new UploadCallable(
                uploadItem,
                configuration.getS3ClientProvider(),
                request,
                configuration.getPartRetryCount(),
                configuration.getTimeoutBetweenRetries(),
                pauseSemaphore
            );

            runnableList.add(uploadCallable);

        }

        LOGGER.info("Parts size: {}", runnableList.size());

        return runnableList;
    }

    private void abortMultipart() {
        LOGGER.info("Aborting multipart upload. Upload item: {}", uploadItem.getKey());

        try {

            final AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(
                configuration.getBucket(),
                uploadItem.getKey(),
                uploadId
            );
            final S3ClientProvider s3ClientProvider = configuration.getS3ClientProvider();
            s3ClientProvider.get().abortMultipartUpload(request);

            LOGGER.info("Multipart upload aborted successfully. Upload item: {}", uploadItem.getKey());

        } catch (Exception ex) {
            LOGGER.info("Cannot abort multipart upload. Upload item: {}", uploadItem.getKey(), ex);
        }

    }


    private class PerPartUploadListener implements ProgressListener {

        private final ProgressListener externalProgressListener;
        private final int partNumber;
        private long uploaded = 0;

        private PerPartUploadListener(ProgressListener externalProgressListener, int partNumber) {
            this.externalProgressListener = externalProgressListener;
            this.partNumber = partNumber;
        }

        @Override
        public void progressChanged(com.amazonaws.event.ProgressEvent progressEvent) {

            final ProgressEventType progressEventType = progressEvent.getEventType();
            final long bytesTransferred = progressEvent.getBytesTransferred();

            uploaded += bytesTransferred;

            if (uploadItem.getState() == UploadItemState.CANCELED) {
                return;
            }

            switch (progressEventType) {
                case TRANSFER_FAILED_EVENT:
                    LOGGER.info("Part failed. Upload item: {}, Part number: {}", uploadItem.getKey(), partNumber);
                    break;

                case TRANSFER_CANCELED_EVENT:
                    LOGGER.info("Part canceled. Upload item: {}, Part number: {}", uploadItem.getKey(), partNumber);
                    break;

                case HTTP_REQUEST_CONTENT_RESET_EVENT:
                    LOGGER.info("Part reset. Upload item: {}, Part number: {}", uploadItem.getKey(), partNumber);

                    final ProgressEvent resetEvent = new ProgressEvent(TRANSFER_PART_FAILED_EVENT, -uploaded);

                    uploaded = 0;

                    notifyExternalListener(resetEvent);

                    break;

                case TRANSFER_PART_STARTED_EVENT:
                    LOGGER.info("Part started. Upload item: {}, Part number: {}" + uploadItem.getKey(), partNumber);
                    break;

                case TRANSFER_PART_COMPLETED_EVENT:
                    LOGGER.info("Part completed. Upload item: {}, Part number: {}" + uploadItem.getKey(), partNumber);

                    break;

                case TRANSFER_PART_FAILED_EVENT:
                    LOGGER.info("Part failed. Upload item: {}, Part number: {}", uploadItem.getKey(), partNumber);
                    break;

                default:

                    if (!started) {

                        started = true;

                        uploadItem.setState(UploadItemState.UPLOADING);
                        itemStateListener.stateChanged(MultipartUpload.this, UploadItemState.UPLOADING);

                        final ProgressEvent event = new ProgressEvent(TRANSFER_STARTED_EVENT, bytesTransferred);

                        notifyExternalListener(event);

                    }

                    if (!pauseSemaphore.isPaused()) {
                        notifyExternalListener(progressEvent);
                    }

            }

        }

        private void notifyExternalListener(ProgressEvent event) {
            if (externalProgressListener != null) {
                externalProgressListener.progressChanged(event);
            }
        }
    }

}
