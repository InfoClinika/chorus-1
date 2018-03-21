package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Herman Zamula
 */
public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {

    @Query("select (count(f) > 0) from DailySummary f where f.serverDayFormatted=:dayFormatted")
    boolean isDayExists(@Param("dayFormatted") String dayFormatted);

    DailySummary findByLabIdAndServerDayFormatted(long labId, String serverDayFormatted);

}
