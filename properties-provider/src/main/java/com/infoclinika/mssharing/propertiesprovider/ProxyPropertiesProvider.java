package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProxyPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${client.proxy.enabled}")
    private boolean proxyEnabled;

    @Value("${client.proxyHost}")
    private String proxyHost;

    @Value("${client.proxyPort:0}")
    private int proxyPort;

    @Value("${client.proxyUsername}")
    private String proxyUsername;

    @Value("${client.proxyPassword}")
    private String proxyPassword;

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }
}
