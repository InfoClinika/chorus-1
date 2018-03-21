package com.infoclinika.mssharing.upload.common.web.model;

import com.infoclinika.mssharing.web.rest.RestExceptionType;

/**
 * @author timofey 25.02.16.
 */
public class WebExceptionResponse {
    private RestExceptionType type;
    private String message;

    public RestExceptionType getType() {
        return type;
    }

    public void setType(RestExceptionType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
