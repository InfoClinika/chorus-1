package com.infoclinika.mssharing.clients.common.transfer.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.dto.response.UploadConfigDTO;
import com.infoclinika.mssharing.propertiesprovider.DesktopClientsPropertiesProvider;
import com.infoclinika.mssharing.propertiesprovider.ProxyPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Scope("prototype")
public class S3ClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientProvider.class);
    private static final int MAX_RETRY_COUNT = 25;

    private WebService webService;
    private AmazonS3Client amazonS3Client;
    private long clientCreationTimestamp;

    @Inject
    private ProxyPropertiesProvider proxyPropertiesProvider;

    @Inject
    private DesktopClientsPropertiesProvider desktopClientsPropertiesProvider;

    S3ClientProvider() {
    }

    @Inject
    public S3ClientProvider(final WebService webService) {
        this.webService = webService;
    }

    private AmazonS3Client createClient() {

        final UploadConfigDTO uploadConfig = webService.getUploadConfig();

        AWSCredentials credentials;
        if (uploadConfig.isUseRoles()) {
            credentials = new BasicSessionCredentials(uploadConfig.getAmazonKey(), uploadConfig.getAmazonSecret(),
                                                      uploadConfig.getSessionToken()
            );
        } else {
            credentials = new BasicAWSCredentials(uploadConfig.getAmazonKey(), uploadConfig.getAmazonSecret());
        }

        final ClientConfiguration clientConfiguration =
            proxyPropertiesProvider.isProxyEnabled() ? getS3ClientConfigWithProxy() : getS3ClientConfig();
        this.clientCreationTimestamp = getCurrentTimestamp();
        return new AmazonS3Client(credentials, clientConfiguration);
    }

    private boolean isCredentialsExpired() {
        final long expirationDuration = desktopClientsPropertiesProvider.getCredentialsExpirationDuration();
        return getCurrentTimestamp() - this.clientCreationTimestamp > expirationDuration;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private ClientConfiguration getS3ClientConfigWithProxy() {

        final ClientConfiguration config = getS3ClientConfig();

        LOGGER.info("# Using next proxy info to configure S3 client:");
        LOGGER.info("# Host: {}", proxyPropertiesProvider.getProxyHost());
        LOGGER.info("# Port: {}", proxyPropertiesProvider.getProxyPort());
        LOGGER.info("# Username: {}", proxyPropertiesProvider.getProxyUsername());
        LOGGER.info("# Password: {}", proxyPropertiesProvider.getProxyPassword());

        config.setProxyHost(proxyPropertiesProvider.getProxyHost());
        config.setProxyPort(proxyPropertiesProvider.getProxyPort());
        config.setProxyUsername(proxyPropertiesProvider.getProxyUsername());
        config.setProxyPassword(proxyPropertiesProvider.getProxyPassword());

        return config;
    }

    private ClientConfiguration getS3ClientConfig() {
        return new ClientConfiguration()
            .withMaxErrorRetry(MAX_RETRY_COUNT)
            .withProtocol(Protocol.HTTPS);
    }

    public AmazonS3 get() {
        if (this.amazonS3Client == null || isCredentialsExpired()) {
            this.amazonS3Client = createClient();
        }

        return this.amazonS3Client;
    }
}
