package com.infoclinika.mssharing.web.controller.v2.service;

import com.amazonaws.services.s3.AmazonS3;

/**
 * @author Vitalii Petkanych
 */
public interface S3FileCopier {

    void copyFile(CopyFileRequest request);

    class CopyFileRequest {
        private final AmazonS3 s3Client;
        private final String srcBucket;
        private final String dstBucket;
        private final String srcKey;
        private final String dstKey;
        private final int numberOfThreads;

        public CopyFileRequest(
            AmazonS3 s3Client,
            String srcBucket,
            String dstBucket,
            String srcKey,
            String dstKey,
            int numberOfThreads
        ) {
            this.s3Client = s3Client;
            this.srcBucket = srcBucket;
            this.dstBucket = dstBucket;
            this.srcKey = srcKey;
            this.dstKey = dstKey;
            this.numberOfThreads = numberOfThreads;
        }

        public AmazonS3 getS3Client() {
            return s3Client;
        }

        public String getSrcBucket() {
            return srcBucket;
        }

        public String getDstBucket() {
            return dstBucket;
        }

        public String getSrcKey() {
            return srcKey;
        }

        public String getDstKey() {
            return dstKey;
        }

        public int getNumberOfThreads() {
            return numberOfThreads;
        }
    }
}
