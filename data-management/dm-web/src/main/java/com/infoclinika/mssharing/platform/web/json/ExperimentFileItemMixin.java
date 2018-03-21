package com.infoclinika.mssharing.platform.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;

import java.util.List;

/**
 * @author Herman Zamula
 */
@SuppressWarnings("unused")
public abstract class ExperimentFileItemMixin {
    @JsonCreator
    public ExperimentFileItemMixin(
            @JsonProperty("id") long id,
            @JsonProperty("factorValues") List<String> factorValues,
            @JsonProperty("annotations") List<ExperimentManagementTemplate.AnnotationTemplate> annotations,
            @JsonProperty("copy") boolean copy) {
    }
}