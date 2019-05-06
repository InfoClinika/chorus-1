package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AmazonPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${amazon.accessKey}")
    private String accessKey;

    @Value("${amazon.secretKey}")
    private String secretKey;

    @Value("${amazon.active.bucket}")
    private String activeBucket;

    @Value("${amazon.archive.bucket}")
    private String archiveBucket;

    @Value("${amazon.archiving.expiration.hours:24000}")
    private int archivingExpirationHours; //1000 Days

    @Value("${amazon.templates.bucket}")
    private String templatesBucket;

    @Value("${amazon.iam.useRoles}")
    private boolean useRoles;

    @Value("${amazon.iam.role}")
    private String iamRole;

    @Value("${amazon.serverside.encryption}")
    private boolean serversideEncryptionEnabled;

    @Value("${amazon.iam.tempKeyDuration:3600}")
    private int tempKeyDuration; // in seconds

    @Value("${amazon.iam.presignedURLDuration:2}")
    private int presignedURLDuration; // in months

    /**
     * Time in milliseconds after the file, prepared for export (CSV export, HDF5 export, etc.),
     * becomes an old enough to be deleted.
     */
    @Value("${amazon.exported.files.max.age:86400000}")
    private long exportedFileMaxAge;

    @Value("${amazon.s3copy.parallelFiles:10}")
    private int s3CopyParallelFilesCount;

    @Value("${amazon.s3copy.partSize:5242880}")
    private long s3CopyPartSize;

    @Value("${amazon.s3copy.threadsPerFile:50}")
    private int s3copyThreadsPerFile;

    @Value("${amazon.sqs.failed.emails.url}")
    private String sqsFailedEmailsUrl;

    @Value("${amazon.workflow.arn}")
    private String workflowArn;

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getActiveBucket() {
        return activeBucket;
    }

    public String getArchiveBucket() {
        return archiveBucket;
    }

    public int getArchivingExpirationHours() {
        return archivingExpirationHours;
    }

    public String getTemplatesBucket() {
        return templatesBucket;
    }

    public boolean isUseRoles() {
        return useRoles;
    }

    public String getIamRole() {
        return iamRole;
    }

    public boolean isServersideEncryptionEnabled() {
        return serversideEncryptionEnabled;
    }

    public int getTempKeyDuration() {
        return tempKeyDuration;
    }

    public int getPresignedURLDuration() {
        return presignedURLDuration;
    }

    public long getExportedFileMaxAge() {
        return exportedFileMaxAge;
    }

    public int getS3CopyParallelFilesCount() {
        return s3CopyParallelFilesCount;
    }

    public long getS3CopyPartSize() {
        return s3CopyPartSize;
    }

    public int getS3copyThreadsPerFile() {
        return s3copyThreadsPerFile;
    }

    public String getSqsFailedEmailsUrl() {
        return sqsFailedEmailsUrl;
    }

    public String getWorkflowArn() {
        return workflowArn;
    }
}
