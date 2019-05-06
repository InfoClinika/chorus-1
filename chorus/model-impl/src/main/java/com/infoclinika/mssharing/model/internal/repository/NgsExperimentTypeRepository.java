package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.restorable.NgsExperimentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Vitalii Petkanych
 */
public interface NgsExperimentTypeRepository extends JpaRepository<NgsExperimentType, Integer> {

    @Query("select t from NgsExperimentType t where t.title = :title")
    NgsExperimentType findByTitle(@Param("title") String title);

}
