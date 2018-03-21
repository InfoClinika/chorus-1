package com.infoclinika.mssharing.platform.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Herman Zamula
 */
@SuppressWarnings("unused")
public abstract class AnnotationMixin {

    @JsonCreator
    public AnnotationMixin(
            @JsonProperty("name") String name,
            @JsonProperty("value") String value,
            @JsonProperty("units") String units,
            @JsonProperty("isNumeric") boolean isNumeric
    ) {
    }
}
