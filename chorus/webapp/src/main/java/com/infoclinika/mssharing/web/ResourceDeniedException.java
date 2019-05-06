package com.infoclinika.mssharing.web;


public class ResourceDeniedException extends RuntimeException {
    public ResourceDeniedException(String message) {
        super(message);
    }
}
