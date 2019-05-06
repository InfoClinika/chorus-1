package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ExperimentLabel;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author andrii.loboda
 */
public interface ExperimentLabelRepository extends JpaRepository<ExperimentLabel, Long> {

    @Query("select label from ExperimentLabel label where label.type=:exLabelType")
    List<ExperimentLabel> findByType(@Param("exLabelType") ExperimentLabelType experimentLabelType);
}
