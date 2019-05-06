package com.infoclinika.mssharing.model.write.ngs;

/**
 * @author timofei.kasianov 8/6/18
 */
public class NgsExperimentImportException extends RuntimeException {

    public NgsExperimentImportException(String message) {
        super(message);
    }

    public NgsExperimentImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
