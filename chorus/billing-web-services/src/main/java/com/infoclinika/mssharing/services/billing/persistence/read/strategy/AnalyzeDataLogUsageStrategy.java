package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.AnalyzableStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyAnalyseStorageUsageRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Set;

/**
 * @author Herman Zamula
 */
@Component
public class AnalyzeDataLogUsageStrategy extends AbstractFeatureLogStrategy {

    @Inject
    public AnalyzeDataLogUsageStrategy(DailyAnalyseStorageUsageRepository usageRepository) {
        super(ChargeableItem.Feature.ANALYSE_STORAGE, usageRepository);
    }

    @Override
    protected Function<Set<ChargeableItemUsage>, ChargeableItemUsageReader.UsageLine> usageLineFn() {

        return perFileUsage -> {
            final UsageParams params = new UsageParams();
            for (ChargeableItemUsage usage : perFileUsage) {
                final AnalyzableStorageUsage analyzableUsage = (AnalyzableStorageUsage) usage;
                params.totalPrice = params.totalPrice + analyzableUsage.getCharge();
                params.totalHours = params.totalHours + analyzableUsage.getHours();
            }
            final AnalyzableStorageUsage itemUsage = (AnalyzableStorageUsage) itemUsageOrdering.max(perFileUsage);
            return new ChargeableItemUsageReader.FileUsageLine(itemUsage.getFile(),
                itemUsage.getBytes(), itemUsage.getInstrument(),
                0, 0, itemUsage.getBalance(), (int) params.totalHours,
                itemUsage.getFileName(), itemUsage.getTimestampDate()
            );
        };
    }

}
