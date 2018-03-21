package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.MonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @author Herman Zamula
 */
public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {

    @Query("SELECT s FROM MonthlySummary s where month(s.loggedMonth) = month(:dateMonth) and year(s.loggedMonth) = year(:dateMonth)")
    List<MonthlySummary> findForMonth(@Param("dateMonth") Date dateMonth);

    List<MonthlySummary> findByLabId(long lab);

}
