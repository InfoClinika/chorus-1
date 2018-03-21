package com.infoclinika.mssharing.model;

/**
 * @author timofey.kasyanov
 *         date: 28.02.14.
 */
public class UploadUnavailable extends RuntimeException {

    public UploadUnavailable(String message) {
        super(message);
    }
}
