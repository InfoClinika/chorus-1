package com.infoclinika.mssharing.services.billing.rest.api.model;

/**
 * @author andrii.loboda
 */
public class BillingFeatureItem {
    public int price;
    public BillingFeature feature;
    public String name;
    public BillingChargeType chargeType;
    public int chargeValue;

    public BillingFeatureItem(int price, BillingFeature feature, String name, BillingChargeType chargeType,
                              int chargeValue) {
        this.price = price;
        this.feature = feature;
        this.name = name;
        this.chargeType = chargeType;
        this.chargeValue = chargeValue;
    }

    public BillingFeatureItem() {
    }


}
