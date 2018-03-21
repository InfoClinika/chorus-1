package com.infoclinika.mssharing.upload.common.transfer.impl;

import com.amazonaws.services.s3.AmazonS3;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *         date: 24.02.14.
 */
public class UploaderConfiguration {

    private static final long MIN_PART_SIZE = 5 * 1024 * 1024;
    private static final int DEFAULT_POOL_SIZE = 30;
    private static final int DEFAULT_RETRY_COUNT = 25;

    private final AmazonS3 amazonS3;
    private final long partSize;
    private final String bucket;
    private final int poolSize;
    private final int partRetryCount;

    public UploaderConfiguration(AmazonS3 amazonS3, long partSize, String bucket, int poolSize, int partRetryCount) {
        this.amazonS3 = checkNotNull(amazonS3);

        checkArgument(partSize >= MIN_PART_SIZE);
        this.partSize = partSize;

        this.bucket = checkNotNull(bucket);

        checkArgument(poolSize > 0);
        this.poolSize = poolSize;

        checkArgument(partRetryCount >= 0);
        this.partRetryCount = partRetryCount;
    }

    public AmazonS3 getAmazonS3() {
        return amazonS3;
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

    public static UploaderConfiguration getDefaultConfiguration(AmazonS3 amazonS3, String bucket) {
        return new UploaderConfiguration(
                amazonS3,
                MIN_PART_SIZE,
                bucket,
                DEFAULT_POOL_SIZE,
                DEFAULT_RETRY_COUNT
        );
    }
}
