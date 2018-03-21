package com.infoclinika.mssharing.web.controller.request;

import java.util.Collection;

/**
 * @author Herman Zamula
 */
public class BulkFileUpdateSpeciesRequest {
    private Collection<? extends Long> fileIds;
    private long newValue;

    public Collection<? extends Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(Collection<? extends Long> fileIds) {
        this.fileIds = fileIds;
    }

    public long getNewValue() {
        return newValue;
    }

    public void setNewValue(long newValue) {
        this.newValue = newValue;
    }
}
