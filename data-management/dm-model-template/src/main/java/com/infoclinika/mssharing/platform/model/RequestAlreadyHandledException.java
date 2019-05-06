package com.infoclinika.mssharing.platform.model;


public class RequestAlreadyHandledException extends RuntimeException {

    public RequestAlreadyHandledException() {
        super();
    }

    public RequestAlreadyHandledException(String message) {
        super(message);
    }
}

