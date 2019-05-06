package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DesktopClientsPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${client.token.enabled}")
    private boolean clientTokenEnabled;

    @Value("${log.file}")
    private String logFile;

    /**
     * Shows how often autoimporter checks target folder on new files existence
     */
    @Value("${monitor.interval:180000}")
    private long monitorInterval;

    /**
     * Shows how many times autoimporter will try to upload files before throwing an error
     */
    @Value("${number.of.tries.to.upload:3}")
    private int numberOfTriesToUpload;

    /**
     * Allows to scan folders with an .RAW extension
     */
    @Value("${thermoRecursiveScan:false}")
    private boolean thermoRecursiveScan;

    /**
     * Delay in milliseconds before retries
     */
    @Value("${time.to.wait.before.retry:300000}")
    private long timeToWaitBeforeRetry;

    /**
     * Shows how many times desktop uploader will try to upload files before throwing an error
     */
    @Value("${upload.max.retry.count:25}")
    private int uploadMaxRetryCount;

    /**
     * Chorus uploader api url
     */
    @Value("${uploader.api.url}")
    private String uploaderApiUrl;

    /**
     * Shows the number of files, autoimporter uploads at the same time
     */
    @Value("${uploading.in.progress.files.limit:100}")
    private long uploadingInProgressFilesLimit;

    /**
     * Path to folder where configurations must be saved
     */
    @Value("${config.folder}")
    private String configFolder;

    @Value("${config.save:false}")
    private boolean saveConfig;

    @Value("${client.credentialsExpirationDuration:1800000}")
    private long credentialsExpirationDuration;

    public String getLogFile() {
        return logFile;
    }

    public boolean isClientTokenEnabled() {
        return clientTokenEnabled;
    }

    public long getMonitorInterval() {
        return monitorInterval;
    }

    public int getNumberOfTriesToUpload() {
        return numberOfTriesToUpload;
    }

    public boolean isThermoRecursiveScan() {
        return thermoRecursiveScan;
    }

    public long getTimeToWaitBeforeRetry() {
        return timeToWaitBeforeRetry;
    }

    public int getUploadMaxRetryCount() {
        return uploadMaxRetryCount;
    }

    public String getUploaderApiUrl() {
        return uploaderApiUrl;
    }

    public long getUploadingInProgressFilesLimit() {
        return uploadingInProgressFilesLimit;
    }

    public String getConfigFolder() {
        return configFolder;
    }

    public boolean isSaveConfig() {
        return saveConfig;
    }

    public long getCredentialsExpirationDuration() {
        return credentialsExpirationDuration;
    }
}
