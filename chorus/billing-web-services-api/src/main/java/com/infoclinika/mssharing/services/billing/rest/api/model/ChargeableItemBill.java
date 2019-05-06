package com.infoclinika.mssharing.services.billing.rest.api.model;


import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author andrii.loboda
 */
public class ChargeableItemBill {
    public String name;
    public long total;
    public BillingFeature type;
    public BillingChargeType loggedChargeValueType;
    public long totalLoggedChargeValue;
    public Collection<UsageByUser> usageByUsers;
    public Long totalFiles;
    public long totalUsers;
    public long price;
    public long unscaledTotal;

    public ChargeableItemBill() {
    }

    public ChargeableItemBill(String name, long total, BillingFeature type, long totalChargeValue,
                              BillingChargeType loggedChargeValueType, Collection<UsageByUser> usageByUsers,
                              @Nullable Long totalFiles, long totalUsers, long price, long unscaledTotal) {
        this.name = name;
        this.total = total;
        this.type = type;
        this.totalLoggedChargeValue = totalChargeValue;
        this.loggedChargeValueType = loggedChargeValueType;
        this.usageByUsers = usageByUsers;
        this.totalFiles = totalFiles;
        this.totalUsers = totalUsers;
        this.price = price;
        this.unscaledTotal = unscaledTotal;
    }
}
