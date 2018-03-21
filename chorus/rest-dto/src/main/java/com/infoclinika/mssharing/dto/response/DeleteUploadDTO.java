package com.infoclinika.mssharing.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * author: Ruslan Duboveckij
 */
public class DeleteUploadDTO {

    private boolean deleted;

    public DeleteUploadDTO(){}

    public DeleteUploadDTO(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
