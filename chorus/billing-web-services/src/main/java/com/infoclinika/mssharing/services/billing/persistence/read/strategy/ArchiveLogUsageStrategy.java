package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.ArchiveStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyArchiveStorageUsageRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Set;

/**
 * @author Herman Zamula
 */
@Component
public class ArchiveLogUsageStrategy extends AbstractFeatureLogStrategy {

    @Inject
    protected ArchiveLogUsageStrategy(DailyArchiveStorageUsageRepository usageRepository) {
        super(ChargeableItem.Feature.ARCHIVE_STORAGE, usageRepository);
    }

    @Override
    protected Function<Set<ChargeableItemUsage>, ChargeableItemUsageReader.UsageLine> usageLineFn() {
        return perFileUsage -> {
            final UsageParams params = new UsageParams();
            for (ChargeableItemUsage usage : perFileUsage) {
                final ArchiveStorageUsage archiveStorageUsage = (ArchiveStorageUsage) usage;
                params.totalPrice = params.totalPrice + archiveStorageUsage.getCharge();
                params.totalHours = params.totalHours + archiveStorageUsage.hours;
            }
            final ChargeableItemUsage itemUsage = itemUsageOrdering.max(perFileUsage);
            return new ChargeableItemUsageReader.FileUsageLine(itemUsage.getFile(), itemUsage.getBytes(), itemUsage.getInstrument(),
                    0, 0, itemUsage.getBalance(), (int) params.totalHours,
                    itemUsage.getFileName(), itemUsage.getTimestampDate());
        };
    }

}
