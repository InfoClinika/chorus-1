package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author andrii.loboda
 */
public interface ExperimentSampleRepository extends JpaRepository<ExperimentSample, Long> {
}
