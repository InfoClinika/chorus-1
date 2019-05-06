package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ExperimentLabelToExperiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author andrii.loboda
 */
public interface ExperimentLabelToExperimentRepository extends JpaRepository<ExperimentLabelToExperiment, Long> {
    @Modifying
    @Query("delete from ExperimentLabelToExperiment exLabel where exLabel.experiment=(select ex " +
        " from ActiveExperiment ex where ex.id=:experiment)")
    void deleteAllByExperiment(@Param("experiment") long experiment);

    @Query("select exLabel from ExperimentLabelToExperiment exLabel join exLabel.experiment ex " +
        " where ex.id=:experiment")
    List<ExperimentLabelToExperiment> findLabelsById(@Param("experiment") long experiment);
}
