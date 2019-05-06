package com.infoclinika.mssharing.desktop.upload.model;

import com.infoclinika.mssharing.propertiesprovider.DesktopClientsPropertiesProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * @author timofey.kasyanov
 *     date:   28.01.14
 */
@Component
public class ConfigurationInfo {

    @Inject
    private DesktopClientsPropertiesProvider desktopClientsPropertiesProvider;

    private String configFolderPath;
    private String zipFolderPath;

    @PostConstruct
    private void init() throws IOException {

        configFolderPath = System.getProperty("user.home") + File.separator + desktopClientsPropertiesProvider
            .getConfigFolder();
        final File configFolder = new File(configFolderPath);

        if (!configFolder.exists() && !configFolder.mkdirs()) {
            throw new IOException("The problem of creating an application configuration folder");
        }

        zipFolderPath = configFolderPath + File.separator + "zips";
        final File zipFolder = new File(zipFolderPath);

        if (!zipFolder.exists() && !zipFolder.mkdirs()) {

            throw new IOException("The problem of creating an application zip folder");
        }
    }

    public String getConfigFolderPath() {
        return configFolderPath;
    }

    public String getZipFolderPath() {
        return zipFolderPath;
    }

    public String getLogFileName() {
        return desktopClientsPropertiesProvider.getLogFile();
    }

    public Integer getUploadMaxRetryCount() {
        return desktopClientsPropertiesProvider.getUploadMaxRetryCount();
    }

    public boolean isClientTokenEnabled() {
        return desktopClientsPropertiesProvider.isClientTokenEnabled();
    }
}
