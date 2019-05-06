package com.infoclinika.mssharing.web.controller.v2;

/**
 * @author Vitalii Petkanych
 */
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(Exception e) {
        super(e.getMessage(), e);
    }
}
