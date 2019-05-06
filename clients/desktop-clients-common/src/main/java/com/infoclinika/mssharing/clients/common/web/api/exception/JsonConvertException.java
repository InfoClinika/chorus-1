package com.infoclinika.mssharing.clients.common.web.api.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author Ruslan Duboveckij
 */
public class JsonConvertException extends RuntimeException {
    private static final Logger LOG = LoggerFactory.getLogger(JsonConvertException.class);

    public JsonConvertException(Throwable cause) {
        super(cause);
        LOG.error(cause.getMessage());
    }
}
