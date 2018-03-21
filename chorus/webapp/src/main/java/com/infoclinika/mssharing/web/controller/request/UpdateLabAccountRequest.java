package com.infoclinika.mssharing.web.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author timofey 14.03.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateLabAccountRequest {
    private long labId;
    private int storageVolumesCount;
    private boolean processingEnabled;
    private boolean autoprolongateProcessing;
    private String accountType;

    public long getLabId() {
        return labId;
    }

    public void setLabId(long labId) {
        this.labId = labId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String type) {
        this.accountType = type;
    }

    public int getStorageVolumesCount() {
        return storageVolumesCount;
    }

    public void setStorageVolumesCount(int storageVolumesCount) {
        this.storageVolumesCount = storageVolumesCount;
    }

    public boolean isProcessingEnabled() {
        return processingEnabled;
    }

    public void setProcessingEnabled(boolean processingEnabled) {
        this.processingEnabled = processingEnabled;
    }

    public boolean isAutoprolongateProcessing() {
        return autoprolongateProcessing;
    }

    public void setAutoprolongateProcessing(boolean autoprolongateProcessing) {
        this.autoprolongateProcessing = autoprolongateProcessing;
    }
}
