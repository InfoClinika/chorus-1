package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.HourlyArchiveStorageUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Herman Zamula
 */
public interface HourlyArchiveStorageUsageRepository
        extends ArchiveStorageUsageRepository<HourlyArchiveStorageUsage>, CrudRepository<HourlyArchiveStorageUsage, Long> {

    @Override
    @Query(
            value = "select new com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage(ss, " +
                    "       (select coalesce(sum(s.hours),0) from HourlyArchiveStorageUsage s where s.day=ss.day AND s.file = ss.file and compressed = false)," +
                    "       (select coalesce(SUM(s.charge),0) from HourlyArchiveStorageUsage s where s.day=ss.day AND s.file = ss.file AND compressed = false)," +
                    "0l)" +
                    " from HourlyArchiveStorageUsage ss where (ss.lab = :lab AND ss.day=:day) AND ss.compressed = false " +
                    " AND ss.timestamp = (SELECT max(s.timestamp) FROM HourlyArchiveStorageUsage s WHERE s.day=ss.day AND s.file = ss.file)",

            countQuery = "select count(ss.id)" +
                    " from HourlyArchiveStorageUsage ss where (ss.lab = :lab AND ss.day=:day) AND ss.compressed = false " +
                    " AND ss.timestamp = (SELECT max(s.timestamp) FROM HourlyArchiveStorageUsage s WHERE s.day=ss.day AND s.file = ss.file)"
    )

    Page<GroupedStorageUsage<HourlyArchiveStorageUsage>> groupedNotCompressedUsagesByFile(@Param("lab") long lab, @Param("day") long dayToLog, Pageable pageable);
}
