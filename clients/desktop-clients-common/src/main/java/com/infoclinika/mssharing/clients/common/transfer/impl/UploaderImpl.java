package com.infoclinika.mssharing.clients.common.transfer.impl;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.infoclinika.mssharing.clients.common.transfer.api.Uploader;
import com.infoclinika.mssharing.clients.common.util.PauseSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *     date: 24.02.14.
 */
public class UploaderImpl implements Uploader, ItemStateListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploaderImpl.class);

    private final UploaderConfiguration configuration;
    private final ExecutorService executor;
    private final ConcurrentLinkedQueue<MultipartUpload> multipartItems = new ConcurrentLinkedQueue<>();
    private volatile boolean canceled = false;
    private final PauseSemaphore pauseSemaphore;

    public UploaderImpl(UploaderConfiguration configuration, PauseSemaphore pauseSemaphore) {
        this.configuration = checkNotNull(configuration);
        this.pauseSemaphore = pauseSemaphore;
        checkArgument(configuration.getPoolSize() > 0);
        executor = createExecutor(configuration.getPoolSize());
    }

    public AmazonS3 getAmazonS3() {
        return configuration.getS3ClientProvider().get();
    }

    @Override
    public boolean upload(UploadItem item, ProgressListener listener) {
        try {

            LOGGER.info("Start upload. Upload item: {}", item.getKey());

            final MultipartUpload multipartItem = new MultipartUpload(
                item,
                configuration,
                executor,
                this,
                pauseSemaphore
            );

            multipartItems.add(multipartItem);
            multipartItem.upload(listener);

        } catch (Exception e) {
            LOGGER.error("Cannot start upload item: {}", item.getKey(), e);
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public boolean cancel() {
        try {
            canceled = true;
            LOGGER.info("Canceling upload");

            for (MultipartUpload item : multipartItems) {
                item.cancel();
            }

            while (multipartItems.size() > 0) {
                removeMultipartItem(multipartItems.poll());
            }

            executor.shutdownNow();

            AmazonS3 amazonS3 = getAmazonS3();
            amazonS3.shutdown();

        } catch (Exception e) {
            LOGGER.error("Error during canceling upload", e);
            throw e;
        }

        return true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void stateChanged(MultipartUpload multipartUpload, UploadItemState state) {
        LOGGER.info("Upload item state changed: {}", state);
        if (state == UploadItemState.COMPLETED
            || state == UploadItemState.CANCELED
            || state == UploadItemState.ERROR) {
            removeMultipartItem(multipartUpload);
        }
    }

    private synchronized void removeMultipartItem(MultipartUpload multipartUpload) {
        multipartItems.remove(multipartUpload);
    }

    private ExecutorService createExecutor(int numberOfThreads) {

        return Executors.newFixedThreadPool(numberOfThreads);

    }

}
