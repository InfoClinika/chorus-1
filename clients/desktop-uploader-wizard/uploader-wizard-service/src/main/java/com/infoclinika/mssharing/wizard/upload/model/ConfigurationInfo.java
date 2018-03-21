package com.infoclinika.mssharing.wizard.upload.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
@Component
public class ConfigurationInfo {

    @Value("${config.folder}")
    private String configFolder;

    @Value("${log.file}")
    private String logFileName;

    @Value("${upload.max.retry.count}")
    private Integer uploadMaxRetryCount;

    @Value("${client.token.enabled}")
    private boolean clientTokenEnabled;

    private String configFolderPath;
    private String zipFolderPath;

    @PostConstruct
    private void init() throws IOException {

        configFolderPath = System.getProperty("user.home") + File.separator + configFolder;
        final File configFolder = new File(configFolderPath);

        if (!configFolder.exists() && !configFolder.mkdirs()) {
            throw new IOException("The problem of creating " +
                    "an application configuration folder");
        }

        zipFolderPath = configFolderPath + File.separator + "zips";
        final File zipFolder = new File(zipFolderPath);

        if (!zipFolder.exists() && !zipFolder.mkdirs()) {

            throw new IOException("The problem of creating " +
                    "an application zip folder");

        }

    }

    public String getConfigFolderPath() {
        return configFolderPath;
    }

    public String getZipFolderPath() {
        return zipFolderPath;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public Integer getUploadMaxRetryCount() {
        return uploadMaxRetryCount;
    }

    public boolean isClientTokenEnabled() {
        return clientTokenEnabled;
    }
}
