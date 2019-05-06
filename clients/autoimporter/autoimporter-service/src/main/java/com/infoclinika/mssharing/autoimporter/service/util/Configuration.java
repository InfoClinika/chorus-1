package com.infoclinika.mssharing.autoimporter.service.util;

import com.infoclinika.mssharing.autoimporter.service.exception.ApplicationConfigException;
import com.infoclinika.mssharing.propertiesprovider.DesktopClientsPropertiesProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * author Ruslan Duboveckij
 */
@Component
public class Configuration {

    @Inject
    private DesktopClientsPropertiesProvider desktopClientsPropertiesProvider;

    private String logFilePath;
    private File configFile;

    @PostConstruct
    private void init() {
        configFile = new File(getConfigFolderPath() + File.separator + "config.json");
        final File configFolder = new File(getConfigFolderPath());
        if (!configFolder.exists()) {
            if (!configFolder.mkdirs()) {
                throw new ApplicationConfigException(
                    "The problem of creating an application configuration folder",
                    new IOException()
                );
            }
        }

        logFilePath = getConfigFolderPath() + File.separator + desktopClientsPropertiesProvider.getLogFile();

    }

    public String getConfigFolderPath() {
        return System.getProperty("user.home") + File.separator + desktopClientsPropertiesProvider.getConfigFolder();
    }

    public String getZipFolderPath() {
        String zipFolderPath = getConfigFolderPath() + File.separator + "zips";
        File zipFolder = new File(zipFolderPath);
        if (!zipFolder.exists()) {
            if (!zipFolder.mkdirs()) {
                throw new ApplicationConfigException(
                    "The problem of creating an application configuration zip folder",
                    new IOException()
                );
            }
        }
        return zipFolderPath;
    }

    public void initConfigFile() {
        if (!configFile.exists() && desktopClientsPropertiesProvider.isSaveConfig()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new ApplicationConfigException("The problem of creating an application configuration file", e);
            }
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    public boolean isConfigSave() {
        return desktopClientsPropertiesProvider.isSaveConfig();
    }

    public long getMonitorInterval() {
        return desktopClientsPropertiesProvider.getMonitorInterval();
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public boolean isClientTokenEnabled() {
        return desktopClientsPropertiesProvider.isClientTokenEnabled();
    }

    public int getNumberOfTriesToUpload() {
        return desktopClientsPropertiesProvider.getNumberOfTriesToUpload();
    }

    public long getTimeToWaitBeforeRetry() {
        return desktopClientsPropertiesProvider.getTimeToWaitBeforeRetry();
    }

    public long getUploadingInProgressFilesLimit() {
        return desktopClientsPropertiesProvider.getUploadingInProgressFilesLimit();
    }

    @Override
    public String toString() {
        return "Configuration{" +
            ", monitorInterval=" + desktopClientsPropertiesProvider.getMonitorInterval() +
            ", configFolder='" + desktopClientsPropertiesProvider.getConfigFolder() + '\'' +
            ", configFile=" + configFile +
            '}';
    }
}
