package com.infoclinika.mssharing.services.billing.rest.api.model;

import java.util.Set;

/**
 * @author andrii.loboda
 */
public class InvoiceFeatureItem {
    public Set<ChargeableItemBill> features;

    public InvoiceFeatureItem(Set<ChargeableItemBill> features) {
        this.features = features;
    }

    public InvoiceFeatureItem() {
    }
}
