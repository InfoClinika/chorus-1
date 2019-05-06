package com.infoclinika.mssharing.autoimporter.service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author Ruslan Duboveckij
 */
public class MonitorException extends RuntimeException {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorException.class);

    public MonitorException(String message, Throwable cause) {
        super(message, cause);
        LOG.error(message);
    }
}
