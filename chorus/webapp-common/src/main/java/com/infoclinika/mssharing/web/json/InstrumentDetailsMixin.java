package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoclinika.mssharing.model.helper.LockMzItem;

import java.util.List;

/**
 * @author Pavel Kaplin
 */
abstract class InstrumentDetailsMixin {
    @JsonCreator
    public InstrumentDetailsMixin(
            @JsonProperty("name") String name,
            @JsonProperty("serialNumber") String serialNumber,
            @JsonProperty("hplc") String hplc,
            @JsonProperty("peripherals") String peripherals,
            @JsonProperty("lockMasses") List<LockMzItem> lockMasses
    ) {
    }
}
