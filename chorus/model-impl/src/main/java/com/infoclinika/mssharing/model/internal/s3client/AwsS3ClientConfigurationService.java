package com.infoclinika.mssharing.model.internal.s3client;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.google.common.base.Joiner;
import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Configuration
@Service
public class AwsS3ClientConfigurationService {

    private static final Logger LOGGER = Logger.getLogger(AwsS3ClientConfigurationService.class);

    private static final String DELIMETER = "/";

    private static final CloudStorageService CLOUD_STORAGE_SERVICE = CloudStorageFactory.service();

    @Value("${processed.files.target.folder}")
    private String processedTargetFolder;

    @Value("${raw.files.target.folder}")
    private String rawFileTargetFolder;

    @Value("${amazon.key}")
    private String accessKeyId;

    @Value("${amazon.secret}")
    private String secretAccessKey;

    @Value("${amazon.active.bucket}")
    private String activeBucket;


    public String getActiveBucket() {
        return activeBucket;
    }

    public String getProcessedTargetFolder() {
        return processedTargetFolder;
    }

    public String getRawFileTargetFolder() {
        return rawFileTargetFolder;
    }

    public BasicAWSCredentials awsCredentialsProvider(){
        return credentials(accessKeyId, secretAccessKey);
    }

    public BasicAWSCredentials credentials(String accessKeyId, String secretAccessKey){
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        return basicAWSCredentials;
    }

    @Bean
    @Primary
    public AmazonS3 s3Client(){
        return amazonS3Client(awsCredentialsProvider(), activeBucket);
    }

    public AmazonS3 amazonS3Client(BasicAWSCredentials credentialsProvider, String bucket) {
        final AmazonS3Client amazonS3Client = new AmazonS3Client(credentialsProvider);
        amazonS3Client.getBucketPolicy(bucket);
        return amazonS3Client;
    }

    public CloudStorageItemReference storageItemReference(String contentId){
        return new CloudStorageItemReference(activeBucket, contentId);
    }


    public NodePath returnProcessingStorageTargetFolder(long experiment, String fileName){
        NodePath nodePath = new NodePath(Joiner.on(DELIMETER).join(processedTargetFolder, experiment, fileName));
        return nodePath;
    }

    public NodePath returnExperimentStorageTargetFolder(long user, long instrumentId, String fileName){
        NodePath nodePath = new NodePath(Joiner.on(DELIMETER).join(rawFileTargetFolder, user, instrumentId, fileName));
        return nodePath;
    }

    public String generateTemporaryLinkToS3(String key){

        CloudStorageItemReference cloudStorageItemReference = storageItemReference(key);

        try {

            if(CLOUD_STORAGE_SERVICE.existsAtCloud(cloudStorageItemReference)){

                Date expiration = new Date();
                long milliSeconds = expiration.getTime();
                milliSeconds += 1000 * 60 * 60;
                expiration.setTime(milliSeconds);

                GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(getActiveBucket(), key);
                generatePresignedUrlRequest.setMethod(HttpMethod.GET);
                generatePresignedUrlRequest.setExpiration(expiration);

                URL url = s3Client().generatePresignedUrl(generatePresignedUrlRequest);

                LOGGER.info("Link on the S3 bucket was successfully created !");
                return url.toString();
            }

        } catch (AmazonServiceException exception) {
            LOGGER.warn("Caught an AmazonServiceException, " + "which means your request made it " + "to Amazon S3, but was rejected with an error response " + "for some reason.");
            LOGGER.warn("Error Message: " + exception.getMessage());
            LOGGER.warn("HTTP  Code: "    + exception.getStatusCode());
            LOGGER.warn("AWS Error Code:" + exception.getErrorCode());
            LOGGER.warn("Error Type:    " + exception.getErrorType());
            LOGGER.warn("Request ID:    " + exception.getRequestId());
        } catch (AmazonClientException ace) {
            LOGGER.warn("Caught an AmazonClientException, " + "which means the client encountered " + "an internal error while trying to communicate" + " with S3, " +
                    "such as not being able to access the network.");
            LOGGER.warn("Error Message: " + ace.getMessage());
        }

        LOGGER.warn("File path does not exists");

        return "File path does not exists";
    }
}
