package com.infoclinika.mssharing.web.controller.response;

public class SuccessErrorResponse {
    public final String errorMessage;
    public final String successMessage;


    public SuccessErrorResponse(String errorMessage, String successMessage) {
        this.errorMessage = errorMessage;
        this.successMessage = successMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }
}