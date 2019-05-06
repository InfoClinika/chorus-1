package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${rabbit.hostname}")
    private String rabbitHost;

    @Value("${rabbit.port:5672}")
    private int rabbitPort;

    @Value("${rabbit.timeout:96000000}")
    private int rabbitTimeout;

    @Value("${rabbit.password}")
    private String rabbitPassword;

    @Value("${rabbit.username}")
    private String rabbitUsername;

    @Value("${analysis.platform.queue}")
    private String analysisPlatformQueue;

    @Value("${rabbit.byonic.username}")
    private String byonicRabbitUsername;

    @Value("${rabbit.byonic.password}")
    private String byonicRabbitPassword;

    @Value("${workflow.run.replyQueueSuffix}")
    private String replyQueueSuffix;

    public String getRabbitHost() {
        return rabbitHost;
    }

    public int getRabbitPort() {
        return rabbitPort;
    }

    public int getRabbitTimeout() {
        return rabbitTimeout;
    }

    public String getRabbitPassword() {
        return rabbitPassword;
    }

    public String getRabbitUsername() {
        return rabbitUsername;
    }

    public String getAnalysisPlatformQueue() {
        return analysisPlatformQueue;
    }

    public String getByonicRabbitUsername() {
        return byonicRabbitUsername;
    }

    public String getByonicRabbitPassword() {
        return byonicRabbitPassword;
    }

    public String getReplyQueueSuffix() {
        return replyQueueSuffix;
    }
}
