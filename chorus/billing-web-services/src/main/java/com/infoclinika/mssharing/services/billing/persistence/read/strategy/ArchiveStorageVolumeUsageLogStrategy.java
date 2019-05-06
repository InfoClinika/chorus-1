package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import com.infoclinika.mssharing.services.billing.persistence.enity.ArchiveStorageVolumeUsage;
import com.infoclinika.mssharing.services.billing.persistence.helper.PaymentCalculationsHelper;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.repository.ArchiveStorageVolumeUsageRepository;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType.PER_VOLUME;

/**
 * @author timofey 12.04.16.
 */
@Component
public class ArchiveStorageVolumeUsageLogStrategy implements FeatureLogStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveStorageVolumeUsageLogStrategy.class);

    @Inject
    private ArchiveStorageVolumeUsageRepository archiveStorageVolumeUsageRepository;
    @Inject
    private PaymentCalculationsHelper paymentCalculationsHelper;
    @Inject
    private ChargeableItemRepository chargeableItemRepository;

    @Override
    public ChargeableItemUsageReader.ChargeableItemBill readBill(long lab, Date dateFrom, Date dateTo) {
        LOGGER.debug("Reading bill for lab {}. From {} to {}", lab, dateFrom, dateTo);

        final long fromInMills = dateFrom.getTime();
        final long toInMills = dateTo.getTime();

        final Long unscaled =
            archiveStorageVolumeUsageRepository.sumAllRawPricesByLabUnscaled(lab, fromInMills, toInMills);
        LOGGER.debug("Price was read...");

        final List<ArchiveStorageVolumeUsage> itemUsages =
            archiveStorageVolumeUsageRepository.findByLab(lab, fromInMills, toInMills);
        final int totalVolumes = itemUsages.stream().mapToInt(ArchiveStorageVolumeUsage::getVolumesCount).sum();
        LOGGER.debug("Usages was read...");
        LOGGER.debug("Invoice data loaded. Total price {}, usages size: {}, volumes count: {}", unscaled,
            itemUsages.size(), totalVolumes
        );

        final ChargeableItem.Feature feature = Transformers.transformFeature(BillingFeature.ARCHIVE_STORAGE_VOLUMES);
        final ChargeableItem chargeableItem = chargeableItemRepository.findByFeature(feature);
        final int totalUsers =
            itemUsages.stream().collect(Collectors.groupingBy(ArchiveStorageVolumeUsage::getUser)).size();

        return new ChargeableItemUsageReader.ChargeableItemBill(
            BillingFeature.ARCHIVE_STORAGE_VOLUMES.getValue(),
            paymentCalculationsHelper.unscalePrice(unscaled),
            BillingFeature.ARCHIVE_STORAGE_VOLUMES,
            totalVolumes,
            PER_VOLUME,
            new ArrayList<>(),
            Optional.<Long>absent(),
            totalUsers,
            chargeableItem.getPrice(),
            unscaled
        );
    }

    @Override
    public ChargeableItemUsageReader.ChargeableItemBill readShortBill(long lab, Date day) {
        LOGGER.debug("Reading feature bill for lab {}. Day {}", lab, day);

        final int daySinceEpoch = paymentCalculationsHelper.calculationDaySinceEpoch(day);
        final Long unscaled = archiveStorageVolumeUsageRepository.sumAllRawPricesByLabUnscaled(lab, daySinceEpoch);
        LOGGER.debug("Price was read...");

        final List<ArchiveStorageVolumeUsage> itemUsages =
            archiveStorageVolumeUsageRepository.findByLab(lab, daySinceEpoch);
        final int totalVolumes = itemUsages.stream().mapToInt(ArchiveStorageVolumeUsage::getVolumesCount).sum();
        LOGGER.debug("Usages was read...");
        LOGGER.debug("Invoice data loaded. Total price {}, usages size: {}, volumes count: {}", unscaled,
            itemUsages.size(), totalVolumes
        );

        final ChargeableItem.Feature feature = Transformers.transformFeature(BillingFeature.ARCHIVE_STORAGE_VOLUMES);
        final ChargeableItem chargeableItem = chargeableItemRepository.findByFeature(feature);
        final int totalUsers =
            itemUsages.stream().collect(Collectors.groupingBy(ArchiveStorageVolumeUsage::getUser)).size();

        return new ChargeableItemUsageReader.ChargeableItemBill(
            BillingFeature.ARCHIVE_STORAGE_VOLUMES.getValue(),
            paymentCalculationsHelper.unscalePrice(unscaled),
            BillingFeature.ARCHIVE_STORAGE_VOLUMES,
            totalVolumes,
            PER_VOLUME,
            new ArrayList<>(),
            Optional.<Long>absent(),
            totalUsers,
            chargeableItem.getPrice(),
            unscaled
        );
    }

    @Override
    public boolean accept(ChargeableItem.Feature billingFeature) {
        return ChargeableItem.Feature.ARCHIVE_STORAGE_VOLUMES == billingFeature;
    }
}
