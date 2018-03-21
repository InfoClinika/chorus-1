package com.infoclinika.mssharing.dto.request;

/**
 * author: Ruslan Duboveckij
 */
public class ConfirmMultipartUploadDTO {

    private long fileId;
    private String remoteDestination;

    public ConfirmMultipartUploadDTO(){}

    public ConfirmMultipartUploadDTO(long fileId, String remoteDestination) {
        this.fileId = fileId;
        this.remoteDestination = remoteDestination;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getRemoteDestination() {
        return remoteDestination;
    }

    public void setRemoteDestination(String remoteDestination) {
        this.remoteDestination = remoteDestination;
    }
}
