package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.StorageUsageStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface StorageUsageStatisticsRepository extends CrudRepository<StorageUsageStatistics, Long> {

    @Query("SELECT sus FROM StorageUsageStatistics sus " +
        " WHERE sus.from >= :from AND sus.to <= :to ORDER BY sus.lab.id ASC")
    List<StorageUsageStatistics> findWithinRange(
        @Param("from") Date from,
        @Param("to") Date to
    );

    @Query("SELECT sus FROM StorageUsageStatistics sus " +
            " WHERE sus.to = :deadline ORDER BY sus.lab.id ASC")
    List<StorageUsageStatistics> findByDeadline(@Param("deadline") Date deadline);

    @Query("SELECT DISTINCT sus.to FROM StorageUsageStatistics sus ORDER BY sus.to DESC")
    List<Date> findAllStatisticsSearches();
}
