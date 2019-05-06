package com.infoclinika.mssharing.services.billing.rest.api.model;

/**
 * @author andrii.loboda
 */
public class InvoiceLabLine {

    public long labId;
    public String labName;
    public String labHead;
    public long storeBalance;

    public InvoiceLabLine() {
    }

    public InvoiceLabLine(long labId, String labName, String labHead,
                          long storeBalance) {
        this.labId = labId;
        this.labName = labName;
        this.labHead = labHead;
        this.storeBalance = storeBalance;
    }

}
