package com.infoclinika.mssharing.services.billing.rest.api.model;

import java.util.Set;

/**
 * @author andrii.loboda
 */
public class FeaturesData {
    public Set<String> features;

    public FeaturesData() {
    }

    public FeaturesData(Set<String> features) {
        this.features = features;
    }
}
