package com.infoclinika.mssharing.clients.common.web.api.exception;

import com.infoclinika.mssharing.web.rest.RestExceptionType;

/**
 * @author timofey.kasyanov
 *     date: 07.05.2014
 */
public class RestServiceException extends RuntimeException {

    private final RestExceptionType exceptionType;

    public RestServiceException(String message, RestExceptionType exceptionType) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public RestExceptionType getExceptionType() {
        return exceptionType;
    }
}
