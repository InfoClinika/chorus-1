package com.infoclinika.mssharing.model.internal.write.ngs.api.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * @author timofei.kasianov 8/3/18
 */
public class NgsExperimentTemplateValidationResult {

    private final List<ValidationError> errors = new LinkedList<>();

    public List<ValidationError> getErrors() {
        return errors;
    }

    public boolean successful() {
        return errors.isEmpty();
    }

    public static class ValidationError {

        private final Integer sampleOrder;
        private final String errorMessage;

        public ValidationError(Integer sampleOrder, String errorMessage) {
            this.sampleOrder = sampleOrder;
            this.errorMessage = errorMessage;
        }

        public ValidationError(String errorMessage) {
            this.sampleOrder = null;
            this.errorMessage = errorMessage;
        }

        public Integer getSampleOrder() {
            return sampleOrder;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

}
