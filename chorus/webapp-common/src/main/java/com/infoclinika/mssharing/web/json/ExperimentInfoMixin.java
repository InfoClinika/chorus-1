package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Pavel Kaplin
 */
abstract class ExperimentInfoMixin {
    @JsonCreator
    public ExperimentInfoMixin(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("workflowType") long workflowType,
            @JsonProperty("specie") long specie) {
    }
}
