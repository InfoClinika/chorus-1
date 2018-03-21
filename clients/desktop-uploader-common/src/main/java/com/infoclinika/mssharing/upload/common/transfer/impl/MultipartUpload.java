package com.infoclinika.mssharing.upload.common.transfer.impl;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.amazonaws.event.ProgressEventType.*;
import static com.amazonaws.services.s3.Headers.*;
import static com.amazonaws.services.s3.model.ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *         date: 24.02.14.
 */
public class MultipartUpload {

    private final static Logger LOGGER = Logger.getLogger(MultipartUpload.class);
    private final static Comparator<PartETag> PART_E_TAG_COMPARATOR = new Comparator<PartETag>() {
        @Override
        public int compare(PartETag o1, PartETag o2) {
            return o1.getPartNumber() - o2.getPartNumber();
        }
    };

    private final UploadItem uploadItem;
    private final UploaderConfiguration configuration;
    private final ExecutorService executor;
    private final List<Future<PartETag>> futures = newArrayList();
    private final ItemStateListener itemStateListener;
    private String uploadId;
    private long partsCount;
    private boolean started = false;
    private long completedParts = 0;

    public MultipartUpload(UploadItem uploadItem,
                           UploaderConfiguration configuration,
                           ExecutorService executor,
                           ItemStateListener itemStateListener) {
        this.uploadItem = uploadItem;
        this.configuration = configuration;
        this.executor = executor;
        this.itemStateListener = itemStateListener;
    }

    public void upload(ProgressListener progressListener) {

        try {

            uploadId = initialize();

            final List<UploadCallable> uploadCallableList = sliceToParts(uploadId, progressListener);

            partsCount = uploadCallableList.size();

            final List<PartETag> eTags = doUpload(uploadCallableList);

            complete(eTags);

        } catch (Exception ex) {

            if (uploadItem.getState() != UploadItemState.CANCELED) {

                uploadItem.setState(UploadItemState.ERROR);
                itemStateListener.stateChanged(this, UploadItemState.ERROR);

                LOGGER.error("Error during multipart upload", ex);

            }

            LOGGER.error("Error during multipart upload, it was canceled", ex);
        }
    }

    public void cancel() {

        LOGGER.info("Canceling multipart upload. Upload item: " + uploadItem.getKey());

        try {

            if (uploadItem.getState() == UploadItemState.UPLOADING) {
                abortMultipart();
            }

            uploadItem.setState(UploadItemState.CANCELED);
            itemStateListener.stateChanged(this, UploadItemState.CANCELED);

            for (Future<PartETag> future : futures) {
                future.cancel(true);
            }

            LOGGER.info("Multipart upload canceled successfully. Upload item: " + uploadItem.getKey());

        } catch (Exception e) {
            LOGGER.error("Error during canceling multipart upload for item: " + uploadItem.getKey(), e);
        }

    }


    private String initialize() {

        LOGGER.info("Initialize multipart upload. Upload item: " + uploadItem.getKey());

        final AmazonS3 amazonS3 = configuration.getAmazonS3();
        final String bucket = configuration.getBucket();
        final String key = uploadItem.getKey();

        final ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setHeader("Authorization", uploadItem.getAuthorization());
        objectMetadata.setHeader(S3_CANNED_ACL, "private");
        objectMetadata.setHeader(S3_ALTERNATE_DATE, uploadItem.getDate());
        if (uploadItem.isServerSideEncryption()) {
            objectMetadata.setHeader(SERVER_SIDE_ENCRYPTION, AES_256_SERVER_SIDE_ENCRYPTION);
        }

        final InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(
                bucket,
                key,
                objectMetadata
        );

        try {

            final InitiateMultipartUploadResult response = amazonS3.initiateMultipartUpload(request);
            final String uploadId = response.getUploadId();

            LOGGER.info("Multipart upload initialized. Upload id: " + uploadId
                    + ", Upload item: " + uploadItem.getKey());

            return uploadId;

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw new RuntimeException("Cannot initiate multipart upload. Upload item: " + uploadItem.getKey());
        }

    }

    private List<UploadCallable> sliceToParts(String uploadId, ProgressListener externalListener) {

        LOGGER.info("Slice to parts. Upload item: " + uploadItem.getKey());

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

            LOGGER.info(
                    "Part number: " + i
                            + ", Part size: " + partSize
                            + ", File offset: " + filePosition
                            + ", Is last part: " + isLastPart
            );

            filePosition += partSize;

            final UploadCallable uploadCallable = new UploadCallable(
                    uploadItem,
                    configuration.getAmazonS3(),
                    request,
                    configuration.getPartRetryCount()
            );

            runnableList.add(uploadCallable);

        }

        LOGGER.info("Parts size: " + runnableList.size());

        return runnableList;

    }

    private List<PartETag> doUpload(List<UploadCallable> uploadCallableList) {

        LOGGER.info("Do multipart upload. Upload item: " + uploadItem.getKey());

        final List<PartETag> eTags = newArrayList();

        for (UploadCallable runnable : uploadCallableList) {

            final Future<PartETag> future = executor.submit(runnable);
            futures.add(future);

            try {
                Thread.sleep(50);
            } catch (Exception ex) {
                LOGGER.info("Thread interrupted while sleeping");
            }

        }

        for (Future<PartETag> future : futures) {

            try {

                final PartETag partETag = future.get();
                eTags.add(partETag);

                LOGGER.info("Future is done. Upload item: " + uploadItem.getKey()
                        + ", Part number: " + partETag.getPartNumber());

            } catch (Exception ex) {
                LOGGER.error("Getting result from future", ex);
                throw new RuntimeException("Cannot get result future");
            }

        }

        LOGGER.info("All tasks are done."
                + ". ETags size: " + eTags.size()
                + ". Upload item: " + uploadItem.getKey());

        return eTags;
    }

    private void complete(List<PartETag> eTags) {

        LOGGER.info("Complete multipart upload. Upload item: " + uploadItem.getKey()
                + "PartETags: " + eTags);

        Collections.sort(eTags, PART_E_TAG_COMPARATOR);

        final AmazonS3 amazonS3 = configuration.getAmazonS3();
        final String bucket = configuration.getBucket();
        final String key = uploadItem.getKey();

        try {

            final CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(
                    bucket,
                    key,
                    uploadId,
                    eTags
            );

            amazonS3.completeMultipartUpload(request);

            LOGGER.info("Multipart upload finished successfully. Upload item: " + uploadItem.getKey());

        } catch (Exception ex) {
            LOGGER.error("Cannot complete multipart upload for item: " + uploadItem.getKey(), ex);
            throw new RuntimeException("Cannot complete multipart upload");
        }

    }

    private void abortMultipart() {

        LOGGER.info("Aborting multipart upload. Upload item: " + uploadItem.getKey());

        try {

            final AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(
                    configuration.getBucket(),
                    uploadItem.getKey(),
                    uploadId
            );

            configuration.getAmazonS3().abortMultipartUpload(request);

            LOGGER.info("Multipart upload aborted successfully. Upload item: " + uploadItem.getKey());

        } catch (Exception ex) {
            LOGGER.info("Cannot abort multipart upload. Upload item: " + uploadItem.getKey(), ex);
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
                    LOGGER.info("Part failed. Upload item: " + uploadItem.getKey()
                            + ", Part number: " + partNumber);
                    break;

                case TRANSFER_CANCELED_EVENT:
                    LOGGER.info("Part canceled. Upload item: " + uploadItem.getKey()
                            + ", Part number: " + partNumber);
                    break;

                case HTTP_REQUEST_CONTENT_RESET_EVENT:
                    LOGGER.info("Part reset. Upload item: " + uploadItem.getKey()
                            + ", Part number: " + partNumber);

                    final ProgressEvent resetEvent = new ProgressEvent(TRANSFER_PART_FAILED_EVENT, -uploaded);

                    uploaded = 0;

                    notifyExternalListener(resetEvent);

                    break;

                case TRANSFER_PART_STARTED_EVENT:
                    LOGGER.info("Part started. Upload item: " + uploadItem.getKey()
                            + ", Part number: " + partNumber);
                    break;

                case TRANSFER_PART_COMPLETED_EVENT:
                    LOGGER.info("Part completed. Upload item: " + uploadItem.getKey()
                            + ", Part number: " + partNumber);

                    ++completedParts;

                    if (completedParts == partsCount) {

                        uploadItem.setState(UploadItemState.COMPLETED);
                        itemStateListener.stateChanged(MultipartUpload.this, UploadItemState.COMPLETED);

                        ProgressEvent event =
                                new ProgressEvent(TRANSFER_COMPLETED_EVENT, bytesTransferred);

                        notifyExternalListener(event);
                    }

                    break;

                case TRANSFER_PART_FAILED_EVENT:
                    LOGGER.info("Part failed. Upload item: " + uploadItem.getKey()
                            + ", Part number: " + partNumber);
                    break;

                default:

                    if (!started) {

                        started = true;

                        uploadItem.setState(UploadItemState.UPLOADING);
                        itemStateListener.stateChanged(MultipartUpload.this, UploadItemState.UPLOADING);

                        final ProgressEvent event =
                                new ProgressEvent(TRANSFER_STARTED_EVENT, bytesTransferred);

                        notifyExternalListener(event);

                    }

                    notifyExternalListener(progressEvent);

            }

        }

        private void notifyExternalListener(ProgressEvent event) {
            if (externalProgressListener != null) {
                externalProgressListener.progressChanged(event);
            }
        }
    }

}
