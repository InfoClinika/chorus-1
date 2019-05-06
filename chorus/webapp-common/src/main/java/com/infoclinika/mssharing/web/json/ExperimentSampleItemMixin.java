package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem;

import java.util.List;

/**
 * @author andrii.loboda
 */
public abstract class ExperimentSampleItemMixin {
    @JsonCreator
    public ExperimentSampleItemMixin(
        @JsonProperty("name") String name,
        @JsonProperty("type") ExperimentSampleTypeItem type,
        @JsonProperty("factorValues") List<String> factorValues
    ) {
    }
}
