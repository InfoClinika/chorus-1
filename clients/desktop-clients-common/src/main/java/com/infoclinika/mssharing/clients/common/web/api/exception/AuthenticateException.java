package com.infoclinika.mssharing.clients.common.web.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author Ruslan Duboveckij
 */
public class AuthenticateException extends RuntimeException {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateException.class);
    private static final String message = "Bad credentials";

    public AuthenticateException() {
        super(message);
        LOG.error(message);
    }
}
