package com.infoclinika.mssharing.platform.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Pavel Kaplin
 */
public abstract class InstrumentDetailsTemplateMixin {
    @JsonCreator
    @SuppressWarnings("unused")
    public InstrumentDetailsTemplateMixin(
            @JsonProperty("name") String name,
            @JsonProperty("serialNumber") String serialNumber,
            @JsonProperty("peripherals") String peripherals
    ) {
    }
}
