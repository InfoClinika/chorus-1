package com.infoclinika.mssharing.services.billing.rest.api.model;

/**
 * @author timofey 21.04.16.
 */
public class PendingCharge {
    public BillingFeature feature;
    public String serverDateFormatted;
    public long featureAmountUsed;
    public long sizeInBytes;
    public long charge;
    public long timestamp;

    public PendingCharge(BillingFeature feature, String serverDateFormatted, long featureAmountUsed, long sizeInBytes,
                         long charge, long timestamp) {
        this.feature = feature;
        this.serverDateFormatted = serverDateFormatted;
        this.featureAmountUsed = featureAmountUsed;
        this.sizeInBytes = sizeInBytes;
        this.charge = charge;
        this.timestamp = timestamp;
    }

    public PendingCharge() {
    }
}
