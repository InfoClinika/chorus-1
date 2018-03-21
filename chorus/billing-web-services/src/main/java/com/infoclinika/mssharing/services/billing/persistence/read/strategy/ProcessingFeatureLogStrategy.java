package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import com.infoclinika.mssharing.services.billing.persistence.enity.ProcessingUsage;
import com.infoclinika.mssharing.services.billing.persistence.helper.PaymentCalculationsHelper;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.repository.ProcessingUsageRepository;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author : timofei.kasianov@gmail.com
 */
@Component
public class ProcessingFeatureLogStrategy implements FeatureLogStrategy {

    private static final Logger LOGGER = Logger.getLogger(ProcessingFeatureLogStrategy.class);

    @Inject
    private ProcessingUsageRepository processingUsageRepository;
    @Inject
    private PaymentCalculationsHelper paymentCalculationsHelper;
    @Inject
    private ChargeableItemRepository chargeableItemRepository;

    @Override
    public ChargeableItemUsageReader.ChargeableItemBill readBill(long lab, Date dateFrom, Date dateTo) {

        LOGGER.debug(format("Reading bill for lab {%d}. From {%s} to {%s}", lab, dateFrom, dateTo));

        final long fromInMills = dateFrom.getTime();
        final long toInMills = dateTo.getTime();

        final Long unscaled = processingUsageRepository.sumAllRawPricesByLabUnscaled(lab, fromInMills, toInMills);
        LOGGER.debug("Price was read...");

        final List<ProcessingUsage> itemUsages = processingUsageRepository.findByLab(lab, fromInMills, toInMills);
        LOGGER.debug("Usages was read...");
        LOGGER.debug(
                format(
                        "Invoice data loaded. Total price {%d}, usages size: {%d}, files count: {%d}",
                        unscaled,
                        itemUsages.size(),
                        0
                )
        );

        final ChargeableItem.Feature feature = Transformers.transformFeature(BillingFeature.PROCESSING);
        final ChargeableItem chargeableItem = chargeableItemRepository.findByFeature(feature);
        final int totalUsers = itemUsages.stream().collect(Collectors.groupingBy(ProcessingUsage::getUser)).size();

        return new ChargeableItemUsageReader.ChargeableItemBill(
                BillingFeature.PROCESSING.getValue(),
                paymentCalculationsHelper.unscalePrice(unscaled),
                BillingFeature.PROCESSING,
                0,
                BillingChargeType.PER_USAGE,
                new ArrayList<>(),
                Optional.<Long>absent(),
                totalUsers,
                chargeableItem.getPrice(),
                unscaled
        );
    }

    @Override
    public ChargeableItemUsageReader.ChargeableItemBill readShortBill(long lab, Date day) {
        LOGGER.debug(format("Reading feature bill for lab {%d}. Day {%s}", lab, day));

        final int daySinceEpoch = paymentCalculationsHelper.calculationDaySinceEpoch(day);
        final Long unscaled = processingUsageRepository.sumAllRawPricesByLabUnscaled(lab, daySinceEpoch);
        LOGGER.debug("Price was read...");

        final List<ProcessingUsage> itemUsages = processingUsageRepository.findByLab(lab, daySinceEpoch);
        LOGGER.debug("Usages was read...");
        LOGGER.debug(
                format(
                        "Invoice data loaded. Total price {%d}, usages size: {%d}, files count: {%d}",
                        unscaled,
                        itemUsages.size(),
                        0
                )
        );

        final ChargeableItem.Feature feature = Transformers.transformFeature(BillingFeature.PROCESSING);
        final ChargeableItem chargeableItem = chargeableItemRepository.findByFeature(feature);
        final int totalUsers = itemUsages.stream().collect(Collectors.groupingBy(ProcessingUsage::getUser)).size();

        return new ChargeableItemUsageReader.ChargeableItemBill(
                BillingFeature.PROCESSING.getValue(),
                paymentCalculationsHelper.unscalePrice(unscaled),
                BillingFeature.PROCESSING,
                0,
                BillingChargeType.PER_USAGE,
                new ArrayList<>(),
                Optional.<Long>absent(),
                totalUsers,
                chargeableItem.getPrice(),
                unscaled
        );
    }

    @Override
    public boolean accept(ChargeableItem.Feature billingFeature) {
        return ChargeableItem.Feature.PROCESSING.equals(billingFeature);
    }
}
