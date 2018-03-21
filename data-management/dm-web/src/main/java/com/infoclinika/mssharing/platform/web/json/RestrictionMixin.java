package com.infoclinika.mssharing.platform.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

/**
 * @author Pavel Kaplin
 */
public abstract class RestrictionMixin {
    @JsonCreator
    @SuppressWarnings("unused")
    public RestrictionMixin(
            @JsonProperty("technologyType") long technologyTypeRestriction,
            @JsonProperty("vendor") long vendorRestriction,
            @JsonProperty("instrumentModel") long instrumentModelRestriction,
            @JsonProperty("instrument") Optional<Long> instrumentRestriction) {
    }
}
