package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.HourlyAnalyzableStorageUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author Herman Zamula
 */
public interface HourlyAnalyseStorageUsageRepository
    extends AnalyzeStorageUsageRepository<HourlyAnalyzableStorageUsage>,
    CrudRepository<HourlyAnalyzableStorageUsage, Long> {
    @Override
    @Query(
        value = "select new com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage(ss, " +
            "  (select coalesce(sum(s.hours),0) from HourlyAnalyzableStorageUsage s where s.day=ss.day " +
            "AND s.file = ss.file and compressed = false)," +
            "  (select coalesce(SUM(s.charge),0) from HourlyAnalyzableStorageUsage s where s.day=ss.day " +
            "AND  s.file = ss.file AND compressed = false)) " +
            " from HourlyAnalyzableStorageUsage ss where ((ss.day=:day AND ss.lab = :lab) AND compressed=false " +
            " AND ss.timestamp = (SELECT max(s.timestamp) FROM HourlyAnalyzableStorageUsage s WHERE s.day=ss.day " +
            "AND s.file = ss.file))",

        countQuery = "select count(ss.id) " +
            " from HourlyAnalyzableStorageUsage ss where ((ss.day=:day AND ss.lab = :lab) AND compressed=false " +
            " AND ss.timestamp = (SELECT max(s.timestamp) FROM HourlyAnalyzableStorageUsage s WHERE s.day=ss.day " +
            "AND s.file = ss.file))"
    )
    Page<GroupedStorageUsage<HourlyAnalyzableStorageUsage>> groupedNotCompressedUsagesByFile(@Param("lab") long lab,
                                                                                             @Param("day") long day,
                                                                                             Pageable pageable);
}
