package com.infoclinika.mssharing.model.extraction.exception;

/**
 * @author Oleksii Tymchenko
 */
public class NoMatchingFilterForPrecursorException extends ChroExtractionException {

    public NoMatchingFilterForPrecursorException(double precursor) {
        super("Cannot find the matching filter for precursor: " + precursor);
    }
}
