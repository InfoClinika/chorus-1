package com.infoclinika.mssharing.upload.common.web.api.exception;

/**
 * @author timofey.kasyanov
 *         date: 07.05.2014
 */
public class UploadLimitExceededException extends RuntimeException {
    public UploadLimitExceededException(String message) {
        super(message);
    }
}
