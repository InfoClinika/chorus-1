package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.AnalyzableStorageUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * @author Elena Kurilina
 */
public interface AnalyzeStorageUsageRepository<T extends AnalyzableStorageUsage>
    extends StorageFeatureUsageRepository<T> {

    /**
     * sum(round(price/100000, 16))
     */
    String SUM_PRICE = "sum(s.charge/com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage" +
            ".PRICE_SCALE_VALUE)";

    @Override
    @Query(
        value = "select new com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage(ss, " +
            " (select coalesce(sum(s.hours),0) from #{#entityName} s " +
            " where s.day=ss.day AND  s.file = ss.file and compressed = false)," +
            " (select coalesce(SUM(s.charge),0) from #{#entityName} s " +
            " where s.day=ss.day AND  s.file = ss.file AND compressed = false)" +
            ")  " +
            " from #{#entityName} ss where ((ss.day=:day AND ss.lab = :lab) AND compressed=false " +
            " AND ss.timestamp = (SELECT max(s.timestamp) FROM #{#entityName} s " +
            "WHERE s.day=ss.day AND s.file = ss.file))",

        countQuery = "select count(ss.id) " +
            " from #{#entityName} ss where ((ss.day=:day AND ss.lab = :lab) AND compressed=false " +
            " AND ss.timestamp = (SELECT max(s.timestamp) FROM #{#entityName} s " +
            "WHERE s.day=ss.day AND s.file = ss.file))"
    )
    Page<GroupedStorageUsage<T>> groupedNotCompressedUsagesByFile(@Param("lab") long lab, @Param("day") long day,
                                                                  Pageable pageable);

    @Override
    @Query("select s from #{#entityName} s where s.lab = :lab AND s.timestamp>:dateFrom AND s.timestamp <= :dateTo")
    List<T> findByLab(@Param("lab") long lab, @Param("dateFrom") long from, @Param("dateTo") long to);

    @Override
    @Query(
        "select new com.infoclinika.mssharing.services.billing.persistence.enity.storage.AnalyzableStorageUsage(" +
            "s.lab, s.user,s.file, s.bytes, s.timestamp, s.usedBy, s.instrument," +
            "sum(s.charge), s.fileName, cast(sum(s.hours) as integer ), s.balance)" +
            " from #{#entityName} s where (s.lab = :lab AND s.day=:day) group by s.file")
    List<ChargeableItemUsage> findGroupedByLab(@Param("lab") long lab, @Param("day") long day);

    @Override
    @Query("select cast(coalesce(round(" + SUM_PRICE + "), 0) as integer ) " +
        "from #{#entityName} s where s.lab = :lab and timestamp > :startDate AND timestamp<=:endDate ")
    Integer sumAllRawPricesByLabFloor(@Param("lab") long lab, @Param("startDate") long from, @Param("endDate") long to);

    @Override
    @Query("select coalesce(sum(s.charge), 0) from #{#entityName} s " +
        " where s.lab = :lab and timestamp > :startDate AND timestamp<=:endDate ")
    Long sumAllRawPricesByLabUnscaled(@Param("lab") long lab, @Param("startDate") long from, @Param("endDate") long to);

    @Override
    @Query("select coalesce(sum(s.charge), 0) from #{#entityName} s " +
        " where (s.lab = :lab and s.day=:day) ")
    Long sumAllRawPricesByLabUnscaled(@Param("lab") long lab, @Param("day") long day);

    @Override
    @Query("select new com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser(" +
        " cast(round(coalesce(" + SUM_PRICE + ", 0)) as integer ), " +
        " s.user,  s.usedBy, count(distinct s.file)) from #{#entityName} s WHERE (s.lab = :lab AND s.day=:day) " +
        "group by s.user")
    List<FeatureUsageByUser> groupUsagesByUser(@Param("lab") long lab, @Param("day") long day);

    @Override
    @Query("select new com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser(" +
        " cast(round(coalesce(" + SUM_PRICE + ", 0)) as integer ), " +
        " s.user,  s.usedBy, count(distinct s.file)) from #{#entityName} s WHERE s.lab = :lab AND s.timestamp > " +
        ":start AND s.timestamp<=:end group by s.user")
    List<FeatureUsageByUser> groupUsagesByUser(@Param("lab") long lab, @Param("start") long from,
                                               @Param("end") long to);

    @Override
    @Modifying(clearAutomatically = true)
    @Transactional("billingLoggingTransactionManager")
    @Query("DELETE FROM #{#entityName} s WHERE s.day=:day AND s.file in (:files) AND s.compressed = false ")
    void deleteLogsForFilesOfDay(@Param("files") Iterable<Long> files, @Param("day") long dayToLog);

    @Override
    @Query("SELECT count(DISTINCT s.file) FROM #{#entityName} s where s.lab = :lab AND s.timestamp > :dateFrom AND " +
        "s.timestamp<=:dateTo")
    Long countFiles(@Param("lab") long lab, @Param("dateFrom") long from, @Param("dateTo") long to);

    @Override
    @Query("SELECT count(DISTINCT s.file) FROM #{#entityName} s where (s.lab = :lab AND s.day=:day)")
    Long countFiles(@Param("lab") long lab, @Param("day") long day);

    @Override
    @Query("SELECT new java.util.Date(u.timestamp) FROM #{#entityName} u" +
        " WHERE u.compressed = false " +
        " AND u.day < :day" +
        " group by u.day " +
        " order by u.timestamp desc ")
    List<Date> datesWhereSumLogsWereMissed(@Param("day") long day);

}
