package com.infoclinika.mssharing.services.billing.persistence.enity.dto;


import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;

import javax.validation.constraints.NotNull;

/**
 * @author Herman Zamula
 */
public class GroupedStorageUsage<T extends ChargeableItemUsage> {

    public final T itemUsage;
    @NotNull
    public final Long hours;
    public final long summedCharge;
    public final long summedTranslatedCharge;

    public GroupedStorageUsage(T itemUsage, Long hours, long summedCharge, long summedTranslatedCharge) {
        this.itemUsage = itemUsage;
        this.hours = hours;
        this.summedCharge = summedCharge;
        this.summedTranslatedCharge = summedTranslatedCharge;
    }
}
