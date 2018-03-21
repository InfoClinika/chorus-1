package com.infoclinika.mssharing.model.helper;

import com.google.common.base.Function;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;

/**
* @author Herman Zamula
*/
public class BillingFeatureItem {

    public final int price;
    public final BillingFeature feature;
    public final String name;
    public final BillingChargeType chargeType;
    public final int chargeValue;

    public BillingFeatureItem(int price, BillingFeature feature, String name, BillingChargeType chargeType, int chargeValue) {
        this.price = price;
        this.feature = feature;
        this.name = name;
        this.chargeType = chargeType;
        this.chargeValue = chargeValue;
    }

    public static Function<BillingFeatureItem, com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeatureItem> WS_TRANSFORM = new Function<BillingFeatureItem, com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeatureItem>() {
        @Override
        public com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeatureItem apply(BillingFeatureItem bfi) {
            return new com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeatureItem(bfi.price, bfi.feature, bfi.name, bfi.chargeType, bfi.chargeValue);
        }
    };
}
