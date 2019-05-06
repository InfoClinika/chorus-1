package com.infoclinika.sso.model.exception;

/**
 * @author andrii.loboda
 */
public class ApplicationAuthenticationServerErrorException extends RuntimeException {
    public ApplicationAuthenticationServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
