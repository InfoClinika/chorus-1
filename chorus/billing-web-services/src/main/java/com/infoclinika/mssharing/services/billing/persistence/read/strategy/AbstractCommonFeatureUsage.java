package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.repository.FeatureUsageRepository;

import java.util.Set;

/**
 * @author Herman Zamula
 */
public abstract class AbstractCommonFeatureUsage extends AbstractFeatureLogStrategy {

    protected AbstractCommonFeatureUsage(ChargeableItem.Feature feature,
                                         FeatureUsageRepository<?> featureUsageRepository) {
        super(feature, featureUsageRepository);
    }

    @Override
    protected Function<Set<ChargeableItemUsage>, ChargeableItemUsageReader.UsageLine> usageLineFn() {
        return perFileUsage -> {
            final UsageParams params = new UsageParams();
            for (ChargeableItemUsage usage : perFileUsage) {
                params.totalPrice = params.totalPrice + usage.getCharge();
            }
            ChargeableItemUsage itemUsage = itemUsageOrdering.max(perFileUsage);
            return new ChargeableItemUsageReader.FileUsageLine(itemUsage.getFile(), itemUsage.getBytes(),
                itemUsage.getInstrument(), paymentCalculations.unscalePrice(params.totalPrice),
                params.totalPrice, itemUsage.getBalance(),
                (int) params.totalHours,
                itemUsage.getFileName(), itemUsage.getTimestampDate()
            );
        };
    }
}
