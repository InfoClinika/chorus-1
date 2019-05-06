package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.payment.FeatureLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Elena Kurilina
 */
public interface FeatureLogRepository extends CrudRepository<FeatureLog, Long> {

    @Query("SELECT f FROM FeatureLog f WHERE f.lab = :lab")
    public List<FeatureLog> findByLab(@Param("lab") long lab);
}
