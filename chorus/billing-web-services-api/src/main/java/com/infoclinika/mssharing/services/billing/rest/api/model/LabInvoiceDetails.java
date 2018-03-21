package com.infoclinika.mssharing.services.billing.rest.api.model;

import java.util.Date;
import java.util.Set;

/**
 * @author andrii.loboda
 */
public class LabInvoiceDetails {
    public String headEmail;
    public String labName;
    public String url;
    public int members;
    public FeaturesData featuresData;
    public Date lastUpdated;
    public long storeBalance;
    public String accountType;
    public boolean isFree;

    public Set<LabAccountFeatureInfo> labAccountFeatures;


    public LabInvoiceDetails() {
    }

    public Set<LabAccountFeatureInfo> getLabAccountFeatures() {
        return labAccountFeatures;
    }

    public void setLabAccountFeatures(Set<LabAccountFeatureInfo> labAccountFeatures) {
        this.labAccountFeatures = labAccountFeatures;
    }

    public LabInvoiceDetails(String headEmail, String labName, String url, int members, FeaturesData featuresData,
                             Date lastUpdated, long storeBalance, String accountType, boolean isFree) {
        this.headEmail = headEmail;
        this.labName = labName;
        this.url = url;
        this.members = members;
        this.featuresData = featuresData;
        this.lastUpdated = lastUpdated;
        this.storeBalance = storeBalance;
        this.accountType = accountType;
        this.isFree = isFree;
    }
}
