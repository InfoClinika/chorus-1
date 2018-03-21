package com.infoclinika.mssharing.model;

/**
 * @author timofey.kasyanov
 *         date: 07.05.2014
 */
public class UploadLimitException extends RuntimeException {
    public UploadLimitException(String message) {
        super(message);
    }
}
