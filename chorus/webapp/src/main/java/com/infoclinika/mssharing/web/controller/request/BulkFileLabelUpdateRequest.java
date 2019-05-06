package com.infoclinika.mssharing.web.controller.request;

import java.util.List;

/**
 * @author Oleksii Tymchenko
 */
public class BulkFileLabelUpdateRequest {
    private List<Long> fileIds;
    private boolean appendLabels;
    private String newValue;

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

    public boolean isAppendLabels() {
        return appendLabels;
    }

    public void setAppendLabels(boolean appendLabels) {
        this.appendLabels = appendLabels;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}
