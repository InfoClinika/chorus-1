package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;

import java.util.Set;

/**
 * @author andrii.loboda
 */
public abstract class ExperimentPreparedSampleItemMixin {
    @JsonCreator
    public ExperimentPreparedSampleItemMixin(
            @JsonProperty("name") String name,
            @JsonProperty("samples") Set<ExperimentSampleItem> samples
    ) {
    }
}
