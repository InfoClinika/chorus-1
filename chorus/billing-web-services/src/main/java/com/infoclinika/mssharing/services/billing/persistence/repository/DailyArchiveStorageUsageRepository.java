package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.DailyArchiveStorageUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface DailyArchiveStorageUsageRepository
        extends ArchiveStorageUsageRepository<DailyArchiveStorageUsage>, CrudRepository<DailyArchiveStorageUsage, Long> {

        @Override
        @Query(
                value = "select new com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage(ss, " +
                        "       (select coalesce(sum(s.hours),0) from DailyArchiveStorageUsage s where s.day=ss.day AND s.file = ss.file and compressed = false)," +
                        "       (select coalesce(SUM(s.charge),0) from DailyArchiveStorageUsage s where s.day=ss.day AND s.file = ss.file AND compressed = false)," +
                        "0l)" +
                        " from DailyArchiveStorageUsage ss where (ss.lab = :lab AND ss.day=:day) AND ss.compressed = false " +
                        " AND ss.timestamp = (SELECT max(s.timestamp) FROM DailyArchiveStorageUsage s WHERE s.day=ss.day AND s.file = ss.file)",

                countQuery = "select count(ss.id)" +
                        " from DailyArchiveStorageUsage ss where (ss.lab = :lab AND ss.day=:day) AND ss.compressed = false " +
                        " AND ss.timestamp = (SELECT max(s.timestamp) FROM DailyArchiveStorageUsage s WHERE s.day=ss.day AND s.file = ss.file)"
        )
        Page<GroupedStorageUsage<DailyArchiveStorageUsage>> groupedNotCompressedUsagesByFile(@Param("lab") long lab, @Param("day") long dayToLog, Pageable pageable);

        @Query("select sum(u.bytes) from DailyArchiveStorageUsage u where u.lab = :lab and u.day >= :fromDay and u.day <= :toDay group by u.day")
        List<Long> getTotalBytesPerDay(@Param("lab") long lab, @Param("fromDay") long fromDaySinceEpoch, @Param("toDay") long toDaySinceEpoch);

        @Query("select max(u.day) from DailyArchiveStorageUsage u where u.lab = :lab")
        Long getLastProcessedDaySinceEpoch(@Param("lab") long lab);

        @Query("select sum(u.bytes) from DailyArchiveStorageUsage u where u.lab = :lab and u.day = :day group by u.day")
        Long getStorageUsageForDay(@Param("lab") long lab, @Param("day") long day);
}
