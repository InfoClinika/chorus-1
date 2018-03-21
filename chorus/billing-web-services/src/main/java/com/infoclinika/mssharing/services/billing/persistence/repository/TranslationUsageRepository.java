package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.TranslationUsage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Elena Kurilina
 */
public interface TranslationUsageRepository extends FeatureUsageRepository<TranslationUsage>, CrudRepository<TranslationUsage, Long> {

    /**
     * sum(round(price/100000, 16))
     */
    final String SUM_CHARGE = "sum(round(u.charge/com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage.PRICE_SCALE_VALUE, com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage.PRICE_PRECISION))";

    @Override
    @Query("select s from TranslationUsage s where s.lab = :lab AND s.timestamp>:dateFrom " +
            "AND s.timestamp <= :dateTo" )
    List<TranslationUsage> findByLab(@Param("lab") long lab, @Param("dateFrom") long from, @Param("dateTo") long to);

    @Override
    @Query("select new com.infoclinika.mssharing.services.billing.persistence.enity.TranslationUsage(s.lab, s.user," +
            "s.file, s.bytes, " +
            "s.timestamp, s.usedBy, s.instrument," +
            "sum(s.charge), s.fileName,s.balance)" +
            " from TranslationUsage s where (s.lab = :lab AND s.day=:day) group by s.file" )
    List<ChargeableItemUsage> findGroupedByLab(@Param("lab") long lab, @Param("day") long day);

    @Override
    @Query("select new com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser(" +
            " round(coalesce(" + SUM_CHARGE + ", 0)), " +
            " u.user,  u.usedBy, count(distinct u.file)) from TranslationUsage u WHERE (u.lab = :lab AND u.day=:day) group by u.user")
    List<FeatureUsageByUser> groupUsagesByUser(@Param("lab") long lab, @Param("day") long day);

    @Override
    @Query("select new com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser(" +
            " round(coalesce(" + SUM_CHARGE + ", 0)), " +
            " u.user,  u.usedBy, count(distinct u.file)) from TranslationUsage u WHERE u.lab = :lab AND u.timestamp>:dateFrom AND u.timestamp <= :dateTo group by u.user")
    List<FeatureUsageByUser> groupUsagesByUser(@Param("lab") long lab, @Param("dateFrom") long from, @Param("dateTo") long to);

    @Query("SELECT CAST(COALESCE(round("+ SUM_CHARGE +"),0) AS integer) FROM TranslationUsage u WHERE u.lab=:lab " +
            "AND u.timestamp > :startDate AND u.timestamp <= :endDate ")
    Integer sumAllRawPricesByLabFloor(@Param("lab") long lab, @Param("startDate") long from, @Param("endDate") long to);

    @Override
    @Query("select coalesce(sum(s.charge), 0) from TranslationUsage s " +
            " where s.lab = :lab and timestamp > :startDate AND timestamp <= :endDate ")
    Long sumAllRawPricesByLabUnscaled(@Param("lab") long lab, @Param("startDate") long from, @Param("endDate") long to);

    @Override
    @Query("select coalesce(sum(s.charge), 0) from TranslationUsage s " +
            " where (s.lab = :lab and s.day=:day) ")
    Long sumAllRawPricesByLabUnscaled(@Param("lab")long lab,@Param("day") long day);

    @Query("SELECT count(DISTINCT s.file) FROM TranslationUsage s where s.lab = :lab AND s.timestamp > :dateFrom AND s.timestamp <= :dateTo")
    Long countFiles(@Param("lab") long lab, @Param("dateFrom") long from, @Param("dateTo") long to);

    @Override
    @Query("SELECT count(DISTINCT s.file) FROM TranslationUsage s where (s.lab = :lab AND s.day=:day)")
    Long countFiles(@Param("lab") long lab, @Param("day") long day);
}
