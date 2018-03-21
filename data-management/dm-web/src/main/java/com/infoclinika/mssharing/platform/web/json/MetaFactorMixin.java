package com.infoclinika.mssharing.platform.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Pavel Kaplin
 */
@JsonIgnoreProperties("id")
public abstract class MetaFactorMixin {
    @JsonCreator
    @SuppressWarnings("unused")
    public MetaFactorMixin(
            @JsonProperty("name") String name,
            @JsonProperty("units") String units,
            @JsonProperty("numeric") boolean numeric,
            @JsonProperty("experimentId") long experimentId) {
    }
}
