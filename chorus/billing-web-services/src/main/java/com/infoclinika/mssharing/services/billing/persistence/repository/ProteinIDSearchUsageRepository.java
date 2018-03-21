package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.ProteinIDSearchUsage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface ProteinIDSearchUsageRepository extends FeatureUsageRepository<ProteinIDSearchUsage>, CrudRepository<ProteinIDSearchUsage, Long> {

    /**
     * sum(round(price/100000, 16))
     */
    final String SUM_PRICE = "sum(round(u.charge/com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage.PRICE_SCALE_VALUE, com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage.PRICE_PRECISION))";

    @Query("select s from ProteinIDSearchUsage s where s.lab = :lab ")
    List<ProteinIDSearchUsage> findByLab(@Param("lab") long lab);

    @Query("select s from ProteinIDSearchUsage s where s.lab = :lab AND s.timestamp>:dateFrom " +
            "AND s.timestamp <= :dateTo")
    List<ProteinIDSearchUsage> findByLab(@Param("lab") long lab, @Param("dateFrom") long from, @Param("dateTo") long to);

    @Override
    @Query("select new com.infoclinika.mssharing.services.billing.persistence.enity.ProteinIDSearchUsage(s.lab, s.user," +
            "  s.bytes, " +
            "s.timestamp, s.usedBy," +
            "s.charge, s.experimentName, s.experiment, s.balance)" +
            " from ProteinIDSearchUsage s where s.lab = :lab AND s.day=:day" )
    List<ChargeableItemUsage> findGroupedByLab(@Param("lab") long lab, @Param("day") long day);

    @Override
    @Query("select new com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser(" +
            " round(coalesce(" + SUM_PRICE  + ", 0)), " +
            " u.user,  u.usedBy, sum(u.filesCount)) from ProteinIDSearchUsage u WHERE (u.lab = :lab AND u.day=:day) group by u.user")
    List<FeatureUsageByUser> groupUsagesByUser(@Param("lab") long lab, @Param("day") long day);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser(" +
            " round(coalesce(" + SUM_PRICE  + ", 0)), " +
            " u.user,  u.usedBy, sum(u.filesCount)) from ProteinIDSearchUsage u WHERE u.lab = :lab AND u.timestamp>:start AND u.timestamp <= :end group by u.user")
    List<FeatureUsageByUser> groupUsagesByUser(@Param("lab") long lab, @Param("start") long from, @Param("end") long to);

    @Query("select cast(round(coalesce(" + SUM_PRICE + ",0)) as integer) from ProteinIDSearchUsage u where u.lab=:lab " +
            "and u.timestamp >= :lastPayedTimestamp group by u.user, u.file")
    List<Integer> groupNotPayedUsersFilesPricesByLab(@Param("lab") long lab,@Param("lastPayedTimestamp") long lastPayedTimestamp);

    @Query("SELECT CAST(COALESCE(round(" + SUM_PRICE + "),0) AS integer) FROM ProteinIDSearchUsage u WHERE u.lab=:lab " +
            "AND u.timestamp > :startDate AND u.timestamp <= :endDate ")
    Integer sumAllRawPricesByLabFloor(@Param("lab") long lab, @Param("startDate") long from, @Param("endDate") long to);

    @Override
    @Query("select coalesce(sum(s.charge), 0) from ProteinIDSearchUsage s " +
            " where s.lab = :lab and timestamp > :startDate AND timestamp <= :endDate ")
    Long sumAllRawPricesByLabUnscaled(@Param("lab") long lab, @Param("startDate") long from, @Param("endDate") long to);

    @Override
    @Query("select coalesce(sum(s.charge), 0) from ProteinIDSearchUsage s " +
            " where (s.lab = :lab and s.day=:day) ")
    Long sumAllRawPricesByLabUnscaled(@Param("lab")long lab,@Param("day") long day);

    @Query("SELECT sum(s.filesCount) FROM ProteinIDSearchUsage s where s.lab = :lab AND s.timestamp > :dateFrom AND s.timestamp <= :dateTo")
    Long countFiles(@Param("lab") long lab, @Param("dateFrom") long from, @Param("dateTo") long to);

    @Override
    @Query("select sum(s.filesCount) from ProteinIDSearchUsage s where s.lab = :lab AND s.day=:day")
    Long countFiles(@Param("lab") long lab, @Param("day") long day);
}
