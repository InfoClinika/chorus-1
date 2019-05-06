package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Herman Zamula
 */
public class LockMzItemMixin {

    public LockMzItemMixin(
        @JsonProperty("lockMass") double lockMass,
        @JsonProperty("charge") int charge) {
    }
}
