package com.infoclinika.mssharing.dto.response;

/**
 * author: Ruslan Duboveckij
 */
public class CompleteUploadDTO {

    private boolean confirmed;

    public CompleteUploadDTO() {
    }

    public CompleteUploadDTO(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
