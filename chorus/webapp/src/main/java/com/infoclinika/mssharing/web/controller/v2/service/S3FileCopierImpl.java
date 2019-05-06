package com.infoclinika.mssharing.web.controller.v2.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.infoclinika.mssharing.web.controller.v2.service.TrackingUploadService.UploadFileTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Vitalii Petkanych
 */
@Service
public class S3FileCopierImpl implements S3FileCopier {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3FileCopierImpl.class);

    private final ActiveBucketService activeBucketService;
    private final TrackingUploadService trackingService;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public S3FileCopierImpl(ActiveBucketService activeBucketService,
                            TrackingUploadService trackingService,
                            AmazonPropertiesProvider amazonPropertiesProvider) {
        this.activeBucketService = activeBucketService;
        this.trackingService = trackingService;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    @Override
    public void copyFile(CopyFileRequest request) {
        final long partSize = amazonPropertiesProvider.getS3CopyPartSize();
        final ExecutorService executorService = Executors.newFixedThreadPool(request.getNumberOfThreads());
        final AmazonS3 s3Client = request.getS3Client();
        final String srcBucket = request.getSrcBucket();
        final String srcKey = request.getSrcKey();
        final String dstBucket = request.getDstBucket();
        final String dstKey = request.getDstKey();

        final String trackingId = trackingService.trackingId(srcBucket, srcKey);
        final UploadFileTracker tracker = trackingService.startFileTracking(trackingId);
        if (tracker.isFileCanceled()) {
            LOGGER.info("Copy {} skipped", srcKey);
            return;
        }

        String uploadId = null;
        List<CompletableFuture<PartETag>> partETagFutures = emptyList();
        try {
            final GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(srcBucket, srcKey);

            final ObjectMetadata srcMetadata = s3Client.getObjectMetadata(metadataRequest);
            final long objectSize = srcMetadata.getContentLength();

            tracker.updateFileStatus(UploadFileStatus.STARTED);
            LOGGER.info("Copy {} started", srcKey);

            if (objectSize < partSize) {
                final CopyObjectRequest copyRequest = new CopyObjectRequest(srcBucket, srcKey, dstBucket, dstKey);
                copyRequest.withNewObjectMetadata(activeBucketService.createCommonMetadata());
                s3Client.copyObject(copyRequest);
            } else {
                List<PartETag> existedParts = emptyList();
                if (tracker.isFileInterrupted()) {
                    final String recoveryId = tracker.getFileRecoveryId();
                    if (existsMultipartUpload(recoveryId, dstBucket, dstKey, s3Client)) {
                        existedParts = getExistedParts(recoveryId, dstBucket, dstKey, s3Client);
                        tracker.updateFileProgress(partSize * existedParts.size());
                        uploadId = recoveryId;
                    }
                }

                if (uploadId == null) {
                    final InitiateMultipartUploadRequest initiateRequest =
                        new InitiateMultipartUploadRequest(dstBucket, dstKey);
                    initiateRequest.setObjectMetadata(activeBucketService.createCommonMetadata());
                    final InitiateMultipartUploadResult initResult = s3Client.initiateMultipartUpload(initiateRequest);

                    uploadId = initResult.getUploadId();
                    tracker.setFileRecoveryId(uploadId);
                }

                final AtomicLong bytesCopied = new AtomicLong(partSize * existedParts.size());

                final Set<Integer> existedPartsNumbers = existedParts.stream()
                    .map(PartETag::getPartNumber)
                    .collect(toSet());
                final int partsCount = calcPartsCount(objectSize);
                partETagFutures = IntStream.range(0, partsCount)
                    .filter(partId -> !existedPartsNumbers.contains(partId + 1))
                    .mapToObj(part -> createCopyPartRequest(
                        srcBucket,
                        srcKey,
                        dstBucket,
                        dstKey,
                        objectSize,
                        tracker.getFileRecoveryId(),
                        part
                    ))
                    .map(rq -> CompletableFuture
                        .supplyAsync(() -> {
                            if (tracker.isFileCanceled()) {
                                LOGGER.trace("Copy {} part {} canceled", srcKey, rq.getPartNumber());
                                throw new UploadFileCancelException();
                            } else {
                                LOGGER.trace(
                                    "Copy {} part {} bytes {} - {}",
                                    new Object[] {srcKey, rq.getPartNumber(), rq.getFirstByte(), rq.getLastByte()}
                                );
                                return s3Client.copyPart(rq);
                            }
                        }, executorService)
                        .thenApply(rp -> {
                            tracker.updateFileProgress(bytesCopied.addAndGet(rq.getLastByte() - rq.getFirstByte() + 1));
                            LOGGER.trace("Copy {} progress {} of {}", new Object[] {srcKey, bytesCopied, objectSize});
                            return rp.getPartETag();
                        }))
                    .collect(toList());

                waitAndCompleteCopy(dstBucket, s3Client, dstKey, tracker, uploadId, partETagFutures, existedParts);
            }

            tracker.updateFileStatus(UploadFileStatus.UPLOADED);
            LOGGER.info("Copy {} completed", srcKey);

        } catch (Exception e) {
            try {
                if (e instanceof CompletionException && e.getCause() instanceof UploadFileCancelException) {
                    LOGGER.info("Copy {} canceled", srcKey);
                    tracker.updateFileStatus(UploadFileStatus.CANCELED);
                    cancelQueuedUpTasks(partETagFutures);
                } else {
                    LOGGER.warn(e.getMessage());
                    tracker.updateFileStatus(UploadFileStatus.FAILED);
                }

                if (uploadId != null) {
                    LOGGER.debug("Copy {} abort multipart upload", srcKey);
                    s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(dstBucket, dstKey, uploadId));
                }
            } catch (Exception e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
        } finally {
            try {
                executorService.shutdownNow();
            } catch (Exception ignore) {
                //ignore
            }
        }
    }

    private void cancelQueuedUpTasks(List<CompletableFuture<PartETag>> partETagFutures) {
        try {
            CompletableFuture
                .allOf((CompletableFuture<?>[]) partETagFutures.toArray(new CompletableFuture<?>[0]))
                .cancel(true);
        } catch (CompletionException | CancellationException e1) {
            LOGGER.error(e1.getLocalizedMessage(), e1);
        }
    }

    private void waitAndCompleteCopy(
        String dstBucket,
        AmazonS3 s3Client,
        String dstKey,
        UploadFileTracker tracker,
        String uploadId,
        List<CompletableFuture<PartETag>> partETagFutures,
        List<PartETag> existedParts
    ) {
        final List<PartETag> partETags = partETagFutures.stream()
            .map(CompletableFuture::join)
            .collect(toList());

        if (!tracker.isFileCanceled()) {
            partETags.addAll(existedParts);
            partETags.sort(Comparator.comparingInt(PartETag::getPartNumber));
            s3Client.completeMultipartUpload(new CompleteMultipartUploadRequest(
                dstBucket,
                dstKey,
                uploadId,
                partETags
            ));
        }
    }

    private int calcPartsCount(long objectSize) {
        final long partSize = amazonPropertiesProvider.getS3CopyPartSize();
        return (int) ((objectSize / partSize) + (objectSize % partSize == 0 ? 0 : 1));
    }

    private CopyPartRequest createCopyPartRequest(String srcBucket,
                                                  String srcKey,
                                                  String dstBucket,
                                                  String dstKey,
                                                  long size,
                                                  String uploadId,
                                                  int part) {
        final long partSize = amazonPropertiesProvider.getS3CopyPartSize();
        final int partNumber = part + 1;

        return new CopyPartRequest()
            .withDestinationBucketName(dstBucket)
            .withDestinationKey(dstKey)
            .withSourceBucketName(srcBucket)
            .withSourceKey(srcKey)
            .withUploadId(uploadId)
            .withFirstByte(part * partSize)
            .withLastByte(Math.min(partNumber * partSize - 1, size - 1))
            .withPartNumber(Math.toIntExact(partNumber));
    }

    private boolean existsMultipartUpload(String uploadId, String bucket, String key, AmazonS3 s3Client) {
        final ListMultipartUploadsRequest request = new ListMultipartUploadsRequest(bucket)
            .withPrefix(key);
        MultipartUploadListing uploadListing;
        boolean exists;
        do {
            uploadListing = s3Client.listMultipartUploads(request);
            exists = uploadListing.getMultipartUploads()
                .stream()
                .anyMatch(multipartUpload -> uploadId.equals(multipartUpload.getUploadId()));
        } while (uploadListing.isTruncated() && !exists);

        return exists;
    }

    private List<PartETag> getExistedParts(String uploadId, String bucket, String key, AmazonS3 s3Client) {
        final List<PartETag> partETags = new ArrayList<>();
        final ListPartsRequest request = new ListPartsRequest(bucket, key, uploadId);
        PartListing parts;
        do {
            parts = s3Client.listParts(request);
            partETags.addAll(extractPartETags(parts.getParts()));
            request.setPartNumberMarker(parts.getNextPartNumberMarker());
        } while (parts.isTruncated());

        return partETags;
    }

    private List<PartETag> extractPartETags(List<PartSummary> list) {
        return list.stream()
            .map(partSummary -> new PartETag(partSummary.getPartNumber(), partSummary.getETag()))
            .collect(toList());
    }

    private class UploadFileCancelException extends RuntimeException {
    }
}
