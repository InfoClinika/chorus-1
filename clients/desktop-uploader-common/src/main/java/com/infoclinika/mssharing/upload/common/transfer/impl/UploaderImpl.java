package com.infoclinika.mssharing.upload.common.transfer.impl;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.infoclinika.mssharing.upload.common.transfer.api.Uploader;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *         date: 24.02.14.
 */
public class UploaderImpl implements Uploader, ItemStateListener {
    private static final Logger LOGGER = Logger.getLogger(UploaderImpl.class);

    private final UploaderConfiguration configuration;
    private final ExecutorService executor;
    private final ConcurrentLinkedQueue<MultipartUpload> multipartItems
            = new ConcurrentLinkedQueue<MultipartUpload>();
    private volatile boolean canceled = false;

    public UploaderImpl(UploaderConfiguration configuration) {
        this.configuration = checkNotNull(configuration);
        checkArgument(configuration.getPoolSize() > 0);
        executor = createExecutor(configuration.getPoolSize());
    }

    public AmazonS3 getAmazonS3() {
        return configuration.getAmazonS3();
    }

    @Override
    public boolean upload(UploadItem item, ProgressListener listener) {

        try {

            LOGGER.info("Start upload. Upload item: " + item.getKey());

            final MultipartUpload multipartItem = new MultipartUpload(
                    item,
                    configuration,
                    executor,
                    this
            );

            multipartItems.add(multipartItem);
            multipartItem.upload(listener);

        } catch (Exception e) {
            LOGGER.error("Cannot start upload item: " + item.getKey(), e);
        }

        return true;
    }

    @Override
    public boolean cancel() {

        canceled = true;
        LOGGER.info("Canceling upload");

        try {

            for (MultipartUpload item : multipartItems) {
                item.cancel();
            }

            while (multipartItems.size() > 0) {
                removeMultipartItem(multipartItems.poll());
            }

            executor.shutdownNow();

            AmazonS3Client amazonS3 = (AmazonS3Client) configuration.getAmazonS3();
            amazonS3.shutdown();

        } catch (Exception e) {
            LOGGER.error("Error during canceling upload", e);
        }

        return true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void stateChanged(MultipartUpload multipartUpload, UploadItemState state) {
        LOGGER.info("Upload item state changed: " + state);
        if (state == UploadItemState.COMPLETED
                || state == UploadItemState.CANCELED
                || state == UploadItemState.ERROR) {
            removeMultipartItem(multipartUpload);
        }
    }

    private synchronized void removeMultipartItem(MultipartUpload multipartUpload) {
        multipartItems.remove(multipartUpload);
    }

    private ExecutorService createExecutor(int nThreads) {

        return Executors.newFixedThreadPool(nThreads);

    }

}
