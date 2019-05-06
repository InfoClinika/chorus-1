package com.infoclinika.mssharing.model.extraction.exception;

/**
 * @author Oleksii Tymchenko
 */
public class ChroExtractionException extends RuntimeException {
    private final String message;

    public ChroExtractionException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
