package com.infoclinika.mssharing.upload.common.web.api.exception;


import org.apache.log4j.Logger;

/**
 * author Ruslan Duboveckij
 */
public class JsonConvertException extends RuntimeException {
    private static final Logger LOG = Logger.getLogger(JsonConvertException.class);

    public JsonConvertException(Throwable cause) {
        super(cause);
        LOG.error(cause.getMessage());
    }
}
