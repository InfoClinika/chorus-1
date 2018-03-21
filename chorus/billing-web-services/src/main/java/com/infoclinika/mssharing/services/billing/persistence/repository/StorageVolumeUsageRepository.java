package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.StorageVolumeUsage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author timofey 21.03.16.
 */
public interface StorageVolumeUsageRepository extends CrudRepository<StorageVolumeUsage, Long> {
    @Query("select s from StorageVolumeUsage s where s.lab = :lab and s.timestamp = (select max(ss.timestamp) from StorageVolumeUsage ss where ss.lab = :lab)")
    StorageVolumeUsage findLast(@Param("lab") long lab);

    @Query("select coalesce(sum(s.charge), 0) from StorageVolumeUsage s " +
            " where s.lab = :lab and s.timestamp > :startDate AND s.timestamp <= :endDate ")
    long sumAllRawPricesByLabUnscaled(@Param("lab") long lab, @Param("startDate") long from, @Param("endDate") long to);

    @Query("select coalesce(sum(s.charge), 0) from StorageVolumeUsage s " +
            " where (s.lab = :lab and s.day=:day) ")
    long sumAllRawPricesByLabUnscaled(@Param("lab")long lab,@Param("day") long day);

    @Query("select s from StorageVolumeUsage s where s.lab = :lab AND s.timestamp>:dateFrom " +
            "AND s.timestamp <= :dateTo")
    List<StorageVolumeUsage> findByLab(@Param("lab") long lab, @Param("dateFrom") long from, @Param("dateTo") long to);

    @Query("select s from StorageVolumeUsage s where s.lab = :lab AND s.day = :day")
    List<StorageVolumeUsage> findByLab(@Param("lab") long lab, @Param("day") long day);

}
