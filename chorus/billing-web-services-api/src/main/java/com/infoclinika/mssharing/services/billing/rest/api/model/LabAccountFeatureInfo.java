package com.infoclinika.mssharing.services.billing.rest.api.model;

/**
 * @author : Alexander Serebriyan
 */
public class LabAccountFeatureInfo {
    public String name;
    public boolean active;
    public long account;
    public boolean autoProlongate;
    public int quantity;

    public LabAccountFeatureInfo(String name, boolean active, long account, boolean autoProlongate, int quantity) {
        this.name = name;
        this.active = active;
        this.account = account;
        this.autoProlongate = autoProlongate;
        this.quantity = quantity;
    }

    public LabAccountFeatureInfo() {
    }
}
