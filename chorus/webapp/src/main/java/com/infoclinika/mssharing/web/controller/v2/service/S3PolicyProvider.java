package com.infoclinika.mssharing.web.controller.v2.service;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.s3.AmazonS3URI;
import com.infoclinika.mssharing.model.internal.s3client.AWSConfigService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Vladislav Kovchug
 */

@Service
public class S3PolicyProvider {
    private static final String AWS_PROVIDER = "AWS";

    private final AWSConfigService awsConfigService;

    @Inject
    public S3PolicyProvider(AWSConfigService awsConfigService) {
        this.awsConfigService = awsConfigService;
    }

    public String getBucketPolicy(String url) {
        final String bucket = new AmazonS3URI(url).getBucket();

        return new Policy()
            .withStatements(
                getStatement(bucket, awsConfigService.getUserArn()),
                getStatement(bucket + "/*", awsConfigService.getUserArn()),
                getStatement(bucket, awsConfigService.getWorkflowArn()),
                getStatement(bucket + "/*", awsConfigService.getWorkflowArn())
            ).toJson();
    }

    private Statement getStatement(String bucket, String arn) {
        return new Statement(Statement.Effect.Allow)
            .withPrincipals(new Principal(AWS_PROVIDER, arn, false))
            .withActions(S3Actions.AllS3Actions)
            .withResources(new Resource("arn:aws:s3:::" + bucket));
    }
}
