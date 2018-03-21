package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface FeatureUsageRepository<T extends ChargeableItemUsage> {

    @Transactional(readOnly = true)
    Integer sumAllRawPricesByLabFloor(long lab, long from, long to);

    Long sumAllRawPricesByLabUnscaled(long lab, long from, long to);

    Long sumAllRawPricesByLabUnscaled(long lab, long day);

    List<T> findByLab(long lab, long from, long to);

    /**
     * Returns grouped files by file meta data id with sum of hours and price
     *
     * @param lab lab id
     * @param day date from in mills
     * @return list of ChargeableItemUsage
     */
    List<ChargeableItemUsage> findGroupedByLab(long lab, long day);

    List<FeatureUsageByUser> groupUsagesByUser(long lab, long day);

    List<FeatureUsageByUser> groupUsagesByUser(long lab, long fromInMills, long toInMills);

    Long countFiles(long lab, long from, long to);

    Long countFiles(long lab, long day);

    <S extends T>
    Iterable<S> save(Iterable<S> usages);

}
