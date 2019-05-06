package com.infoclinika.mssharing.model.internal.write.ngs;

/**
 * @author timofei.kasianov 8/3/18
 */
public class NgsExperimentTemplateParseException extends RuntimeException {

    public NgsExperimentTemplateParseException(String message) {
        super(message);
    }

    public NgsExperimentTemplateParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
