package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;

import java.util.Date;

/**
 * @author Herman Zamula
 */
public interface FeatureLogStrategy {

    ChargeableItemUsageReader.ChargeableItemBill readBill(long lab, Date dateFrom, Date dateTo);

    ChargeableItemUsageReader.ChargeableItemBill readShortBill(long lab, Date day);

    boolean accept(ChargeableItem.Feature billingFeature);
}
