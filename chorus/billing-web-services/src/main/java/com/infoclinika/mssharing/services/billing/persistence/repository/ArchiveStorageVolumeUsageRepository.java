package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.ArchiveStorageVolumeUsage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author timofey 12.04.16.
 */
public interface ArchiveStorageVolumeUsageRepository extends CrudRepository<ArchiveStorageVolumeUsage, Long> {

    @Query(
        "select s from ArchiveStorageVolumeUsage s where s.lab = :lab and s.timestamp = (select max(ss.timestamp) " +
            "from ArchiveStorageVolumeUsage ss where ss.lab = :lab)")
    ArchiveStorageVolumeUsage findLast(@Param("lab") long lab);

    @Query("select coalesce(sum(s.charge), 0) from ArchiveStorageVolumeUsage s " +
        " where s.lab = :lab and s.timestamp > :startDate AND s.timestamp <= :endDate ")
    long sumAllRawPricesByLabUnscaled(@Param("lab") long lab, @Param("startDate") long from, @Param("endDate") long to);

    @Query("select coalesce(sum(s.charge), 0) from ArchiveStorageVolumeUsage s " +
        " where (s.lab = :lab and s.day=:day) ")
    long sumAllRawPricesByLabUnscaled(@Param("lab") long lab, @Param("day") long day);

    @Query("select s from ArchiveStorageVolumeUsage s where s.lab = :lab AND s.timestamp>:dateFrom " +
        "AND s.timestamp <= :dateTo")
    List<ArchiveStorageVolumeUsage> findByLab(@Param("lab") long lab, @Param("dateFrom") long from,
                                              @Param("dateTo") long to);

    @Query("select s from ArchiveStorageVolumeUsage s where s.lab = :lab AND s.day = :day")
    List<ArchiveStorageVolumeUsage> findByLab(@Param("lab") long lab, @Param("day") long day);

}
