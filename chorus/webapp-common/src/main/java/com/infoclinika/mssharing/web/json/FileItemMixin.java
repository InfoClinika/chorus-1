package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoclinika.mssharing.model.helper.ExperimentPreparedSampleItem;

/**
 * @author Pavel Kaplin
 */
abstract class FileItemMixin {
    @JsonCreator
    public FileItemMixin(
        @JsonProperty("id") long id,
        @JsonProperty("copy") boolean copy,
        @JsonProperty("fractionNumber") int fractionNumber,
        @JsonProperty("preparedSample") ExperimentPreparedSampleItem preparedSample
    ) {
    }
}
