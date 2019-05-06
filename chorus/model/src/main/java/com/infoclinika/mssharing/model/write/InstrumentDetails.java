package com.infoclinika.mssharing.model.write;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;

import java.util.List;

/**
 * @author Herman Zamula
 */
public class InstrumentDetails extends InstrumentManagementTemplate.InstrumentDetailsTemplate {
    public final String hplc;
    public final List<LockMzItem> lockMasses;

    @JsonCreator
    public InstrumentDetails(@JsonProperty("name") String name,
                             @JsonProperty("serialNumber") String serialNumber,
                             @JsonProperty("peripherals") String peripherals,
                             @JsonProperty("hplc") String hplc,
                             @JsonProperty("lockMasses") List<LockMzItem> lockMasses) {
        super(name, serialNumber, peripherals);
        this.hplc = hplc;
        this.lockMasses = lockMasses;
    }
}
