package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Herman Zamula
 */
public interface PaymentCalculationsHelper {

    long unscalePrice(long price);

    long scalePrice(long realPrice);

    long unscalePriceNotRound(long price);

    long calculateRoundedPriceByUnscaled(long actualBalance, long unscaledValue);

    int calculationDaySinceEpoch(Date timestamp);

    long calculateTotalToPayForLab(long lab, Date from, Date to);

    int calculateStorageVolumes(long bytes);

    int calculateArchiveStorageVolumes(long bytes);

    long calculateStorageCost(int volumes);

    long calculateArchiveStorageCost(int volumes);

    long calculateMaximumStorageUsage(long lab, Date from, Date to);

    long calculateMaximumArchiveStorageUsage(long lab, Date from, Date to);

    /**
     * @param lab laboratory for calculation
     * @param day day to calculate charged amount
     * @return total charged amount of used features for day
     */
    long calculateTotalToPayForLabForDay(long lab, Date day);

    long sumPrices(Iterable<? extends Number> prices);

    /**
     * @param lab  laboratory for calculation
     * @param from start date
     * @param to   end date
     * @return Optional.absent() if no logged usages for this lab found
     */
    Optional<Long> calculateStoreBalance(long lab, Date from, Date to);

    /**
     * Calculates total cost of the given feature by the period in cents
     */
    long caclulateTotalPrice(long lab, BillingFeature feature, Date from, Date to);

    /**
     * @param lab laboratory for calculation
     * @param day day to calculate store balance
     * @return Optional.absent() if no logged usages for this lab found
     */
    Optional<Long> calculateStoreBalanceForDay(long lab, Date day);

    /**
     * For internal use
     */
    /*package*/ long countLoggedUsagesInRangePerFeature(long lab, Date from, Date to);

    /*package*/ long countLoggedUsagesInRangePerFeature(long lab, Date day);

    BigDecimal calculateScaledFeaturePrice(long bytes, BillingFeature feature);

    BigDecimal calculateScaledFeaturePriceForEachLab(long bytes, BillingFeature feature, int accounts);

    BigDecimal scalePriceBetweenLabs(BigDecimal price, int labsCount);
}
