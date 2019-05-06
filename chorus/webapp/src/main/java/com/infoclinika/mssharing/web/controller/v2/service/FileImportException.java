package com.infoclinika.mssharing.web.controller.v2.service;

/**
 * @author Vitalii Petkanych
 */
public class FileImportException extends RuntimeException {

    public FileImportException(InterruptedException e) {
        super(e);
    }
}
