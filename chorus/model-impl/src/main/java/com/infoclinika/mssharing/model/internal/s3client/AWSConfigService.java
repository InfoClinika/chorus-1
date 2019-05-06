package com.infoclinika.mssharing.model.internal.s3client;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.ClientConfigurationFactory;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Component
public class AWSConfigService {
    public static final String DELIMITER = "/";
    private static final Logger LOGGER = LoggerFactory.getLogger(AWSConfigService.class);

    private ClientConfiguration clientConfiguration;
    private AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public AWSConfigService(AmazonPropertiesProvider amazonPropertiesProvider) {
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    @PostConstruct
    private void createClientConfiguration() {
        LOGGER.info("Using S3 bucket {}", getActiveBucket());

        final int s3CopyParallelFilesCount = amazonPropertiesProvider.getS3CopyParallelFilesCount();
        final int threads = amazonPropertiesProvider.getS3copyThreadsPerFile();
        clientConfiguration = new ClientConfigurationFactory()
            .getConfig()
            .withMaxErrorRetry(3)
            .withMaxConnections(s3CopyParallelFilesCount * threads);
    }

    public String getAccessKeyId() {
        return amazonPropertiesProvider.getAccessKey();
    }

    public String getSecretAccessKey() {
        return amazonPropertiesProvider.getSecretKey();
    }

    public String getActiveBucket() {
        return amazonPropertiesProvider.getActiveBucket();
    }

    public boolean isActiveBucketEncrypted() {
        return amazonPropertiesProvider.isServersideEncryptionEnabled();
    }

    public int getParallelFiles() {
        return amazonPropertiesProvider.getS3CopyParallelFilesCount();
    }

    public int getParallelThreads() {
        return amazonPropertiesProvider.getS3copyThreadsPerFile();
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    public AmazonS3 s3Client() {
        return s3Client(getActiveBucket());
    }

    public AmazonS3 s3Client(String bucket) {
        return s3Client(awsCredentialsProvider(), bucket);
    }

    public AmazonS3 s3Client(AWSCredentialsProvider credentialsProvider, String bucket) {
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                                                                   .withCredentials(credentialsProvider)
                                                                   .withClientConfiguration(clientConfiguration)
                                                                   .withRegion(Regions.DEFAULT_REGION);
        try {
            builder.build().getBucketPolicy(bucket);
        } catch (AmazonS3Exception e) {
            if ("PermanentRedirect".equals(e.getErrorCode())) {
                builder.withRegion(Regions.fromName(e.getHttpHeaders().get("x-amz-bucket-region")));
            } else {
                throw e;
            }
        } catch (SdkClientException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return builder.build();
    }

    public String getWorkflowArn() {
        return amazonPropertiesProvider.getWorkflowArn();
    }

    public AWSCredentialsProvider awsCredentialsProvider() {
        if (amazonPropertiesProvider.isUseRoles()) {
            return new InstanceProfileCredentialsProvider(false);
        }

        return awsCredentialsProvider(getAccessKeyId(), getSecretAccessKey());
    }

    public AWSCredentialsProvider awsCredentialsProvider(String accessKeyId, String secretAccessKey) {
        final BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        return new AWSStaticCredentialsProvider(credentials);
    }

    public String getUserArn() {
        return getArn(awsCredentialsProvider());
    }

    private String getArn(AWSCredentialsProvider credentialsProvider) {
        AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard()
                                                                          .withCredentials(credentialsProvider)
                                                                          .withRegion(Regions.DEFAULT_REGION)
                                                                          .build();
        GetCallerIdentityResult identity = sts.getCallerIdentity(new GetCallerIdentityRequest());
        return identity.getArn();
    }
}
