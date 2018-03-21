package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.google.common.base.Function;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.ProteinIDSearchUsage;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.repository.ProteinIDSearchUsageRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Set;

import static com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.Feature.ANALYSIS;

/**
 * @author Herman Zamula
 */
@Component
public class ProteinIDSearchStrategy extends AbstractFeatureLogStrategy {

    @Inject
    protected ProteinIDSearchStrategy(ProteinIDSearchUsageRepository usageRepository) {
        super(ANALYSIS, usageRepository);
    }

    @Override
    protected Function<Set<ChargeableItemUsage>, ChargeableItemUsageReader.UsageLine> usageLineFn() {
        return new Function<Set<ChargeableItemUsage>, ChargeableItemUsageReader.UsageLine>() {
            @Override
            public ChargeableItemUsageReader.UsageLine apply(Set<ChargeableItemUsage> usages) {
                final UsageParams params = new UsageParams();
                for (ChargeableItemUsage usage : usages) {
                    final ProteinIDSearchUsage analyzableUsage = (ProteinIDSearchUsage) usage;
                    params.totalPrice = params.totalPrice + analyzableUsage.getCharge();
                }
                final ProteinIDSearchUsage itemUsage = (ProteinIDSearchUsage) itemUsageOrdering.max(usages);
                return new ChargeableItemUsageReader.ExperimentUsageLine(itemUsage.getBytes(),
                        paymentCalculations.unscalePrice(params.totalPrice), params.totalPrice, itemUsage.getBalance(),
                        itemUsage.getExperiment(), itemUsage.getExperimentName(), itemUsage.getTimestampDate());
            }
        };
    }

}
