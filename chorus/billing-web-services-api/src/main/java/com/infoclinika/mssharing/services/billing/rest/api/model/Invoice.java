package com.infoclinika.mssharing.services.billing.rest.api.model;

import java.util.Date;

/**
 * @author andrii.loboda
 */
public class Invoice {
    public long total;
    public long storeBalance;
    public long labId;
    public String labName;
    public Date date;
    public InvoiceFeatureItem featureItem;

    public Invoice(String labName, long labId, long storeBalance, long total,
                   Date timestamp, InvoiceFeatureItem featureItem) {
        this.labName = labName;
        this.total = total;
        this.storeBalance = storeBalance;
        this.labId = labId;
        this.date = timestamp;
        this.featureItem = featureItem;
    }

    public Invoice() {
    }
}
