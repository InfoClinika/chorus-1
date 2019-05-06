package com.infoclinika.mssharing.platform.fileserver;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * @author Vladislav Kovchug
 */
public class StorageServiceHelper {

    public static AmazonS3 createAmazonS3Client(String amazonKey, String amazonSecret, boolean useRoles) {
        if (useRoles) {
            final InstanceProfileCredentialsProvider credentialsProvider =
                new InstanceProfileCredentialsProvider(false);

            return AmazonS3ClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();
        } else {
            final BasicAWSCredentials credentials = new BasicAWSCredentials(amazonKey, amazonSecret);
            final AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);

            return AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();
        }
    }

}
