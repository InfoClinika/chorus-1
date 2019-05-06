package com.infoclinika.mssharing.platform.model.impl.helper.adapters;

import com.amazonaws.auth.*;
import com.infoclinika.mssharing.platform.model.impl.helper.AmazonCorsRequestSignerTemplate;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
public class AmazonCorsRequestSignerAdapter extends AmazonCorsRequestSignerTemplate {
    private AmazonPropertiesProvider amazonPropertiesProvider;
    private AWSCredentialsProvider credentialsProvider;

    @Inject
    public AmazonCorsRequestSignerAdapter(AmazonPropertiesProvider amazonPropertiesProvider) {
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    @PostConstruct
    private void init() {
        credentialsProvider = getCredentialsProvider();
    }

    @Override
    protected String getBucket(long userId, String objectName) {
        return amazonPropertiesProvider.getActiveBucket();
    }

    @Override
    public String getAmazonKey() {
        if (credentialsProvider != null) {
            return credentialsProvider.getCredentials().getAWSAccessKeyId();
        }
        return "";
    }

    @Override
    public String getAmazonSecret() {
        if (credentialsProvider != null) {
            return credentialsProvider.getCredentials().getAWSSecretKey();
        }
        return "";
    }

    @Override
    public String getAmazonToken() {
        if (credentialsProvider != null && credentialsProvider instanceof InstanceProfileCredentialsProvider) {
            return ((BasicSessionCredentials) credentialsProvider.getCredentials()).getSessionToken();
        }
        return "";
    }

    @Override
    public boolean useSessionToken() {
        return (credentialsProvider != null && credentialsProvider instanceof InstanceProfileCredentialsProvider);
    }

    private AWSCredentialsProvider getCredentialsProvider() {
        if (amazonPropertiesProvider.isUseRoles()) {
            return InstanceProfileCredentialsProvider.getInstance();
        } else {
            final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(
                amazonPropertiesProvider.getAccessKey(),
                amazonPropertiesProvider.getSecretKey()
            );

            return new AWSStaticCredentialsProvider(basicAWSCredentials);
        }
    }
}
