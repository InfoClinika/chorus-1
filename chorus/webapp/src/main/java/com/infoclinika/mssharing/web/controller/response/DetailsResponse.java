package com.infoclinika.mssharing.web.controller.response;

public class DetailsResponse<T> extends SuccessErrorResponse {

    public static final String DEFAULT_SUCCESS_MESSAGE = "Success";

    public final T details;

    public DetailsResponse(T details, String errorMessage, String successMessage) {
        super(errorMessage, successMessage);
        this.details = details;
    }

    public static <T> DetailsResponse<T> ok(T details) {
        return new DetailsResponse<T>(details, null, DEFAULT_SUCCESS_MESSAGE);
    }
}
