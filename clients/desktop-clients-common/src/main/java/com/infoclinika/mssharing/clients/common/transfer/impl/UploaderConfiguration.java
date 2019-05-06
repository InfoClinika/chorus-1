package com.infoclinika.mssharing.clients.common.transfer.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *     date: 24.02.14.
 */
public class UploaderConfiguration {

    private static final long MIN_PART_SIZE = 5 * 1024 * 1024;
    private static final int DEFAULT_POOL_SIZE = 30;
    private static final int DEFAULT_RETRY_COUNT = 25;
    private static final int TIMEOUT_BETWEEN_RETRIES = 3000;

    private final S3ClientProvider s3ClientProvider;
    private final long partSize;
    private final String bucket;
    private final int poolSize;
    private final int partRetryCount;
    private final int timeoutBetweenRetries;

    public UploaderConfiguration(S3ClientProvider s3ClientProvider, long partSize, String bucket, int poolSize,
                                 int partRetryCount, int timeoutBetweenRetries) {
        this.s3ClientProvider = checkNotNull(s3ClientProvider);

        checkArgument(partSize >= MIN_PART_SIZE);
        this.partSize = partSize;

        this.bucket = checkNotNull(bucket);

        checkArgument(poolSize > 0);
        this.poolSize = poolSize;

        checkArgument(partRetryCount >= 0);
        this.partRetryCount = partRetryCount;

        checkArgument(timeoutBetweenRetries >= 0);
        this.timeoutBetweenRetries = timeoutBetweenRetries;
    }

    public static UploaderConfiguration getDefaultConfiguration(S3ClientProvider s3ClientProvider, String bucket) {
        return new UploaderConfiguration(
            s3ClientProvider,
            MIN_PART_SIZE,
            bucket,
            DEFAULT_POOL_SIZE,
            DEFAULT_RETRY_COUNT,
            TIMEOUT_BETWEEN_RETRIES
        );
    }

    public S3ClientProvider getS3ClientProvider() {
        return s3ClientProvider;
    }

    public long getPartSize() {
        return partSize;
    }

    public String getBucket() {
        return bucket;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public int getPartRetryCount() {
        return partRetryCount;
    }

    public int getTimeoutBetweenRetries() {
        return timeoutBetweenRetries;
    }
}
