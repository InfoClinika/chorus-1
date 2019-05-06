package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.DailyAnalyzableStorageUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface DailyAnalyseStorageUsageRepository
    extends AnalyzeStorageUsageRepository<DailyAnalyzableStorageUsage>,
    CrudRepository<DailyAnalyzableStorageUsage, Long> {

    @Override
    @Query(
        value = "select new com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage(ss, " +
            "  (select coalesce(sum(s.hours),0) from DailyAnalyzableStorageUsage s " +
            " where s.day=ss.day AND  s.file = ss.file and compressed = false)," +
            "  (select coalesce(SUM(s.charge),0) from DailyAnalyzableStorageUsage s " +
            " where s.day=ss.day AND  s.file = ss.file AND compressed = false)" +
            ")  " +
            " from DailyAnalyzableStorageUsage ss where ((ss.day=:day AND ss.lab = :lab) AND compressed=false " +
            " AND ss.timestamp = (SELECT max(s.timestamp) FROM DailyAnalyzableStorageUsage s " +
            "WHERE s.day=ss.day AND s.file = ss.file))",

        countQuery = "select count(ss.id) " +
            " from DailyAnalyzableStorageUsage ss where ((ss.day=:day AND ss.lab = :lab) AND compressed=false " +
            " AND ss.timestamp = (SELECT max(s.timestamp) FROM DailyAnalyzableStorageUsage s " +
            "WHERE s.day=ss.day AND s.file = ss.file))"
    )
    Page<GroupedStorageUsage<DailyAnalyzableStorageUsage>> groupedNotCompressedUsagesByFile(@Param("lab") long lab,
                                                                                            @Param("day") long day,
                                                                                            Pageable pageable);

    @Query("select sum(u.bytes) from DailyAnalyzableStorageUsage u where u.lab = :lab and u.day = :day group by u.day")
    Long getRawStorageUsageForDay(@Param("lab") long lab, @Param("day") long day);

    @Query(
        "select sum(u.bytes) from DailyAnalyzableStorageUsage u where u.lab = :lab and u.day >= " +
            ":fromDay and u.day <= :toDay group by u.day")
    List<Long> getTotalBytesPerDay(@Param("lab") long lab, @Param("fromDay") long fromDaySinceEpoch,
                                   @Param("toDay") long toDaySinceEpoch);

    @Query("select max(u.day) from DailyAnalyzableStorageUsage u where u.lab = :lab")
    Long getLastProcessedDaySinceEpoch(@Param("lab") long lab);
}
