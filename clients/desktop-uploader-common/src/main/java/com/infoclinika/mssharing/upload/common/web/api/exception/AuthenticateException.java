package com.infoclinika.mssharing.upload.common.web.api.exception;

import org.apache.log4j.Logger;

/**
 * author Ruslan Duboveckij
 */
public class AuthenticateException extends RuntimeException {
    private static final Logger LOG = Logger.getLogger(AuthenticateException.class);
    private static final String message = "Bad credentials";

    public AuthenticateException() {
        super(message);
        LOG.error(message);
    }
}
