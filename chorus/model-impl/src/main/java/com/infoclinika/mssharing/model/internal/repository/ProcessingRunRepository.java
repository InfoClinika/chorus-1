package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ProcessingRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessingRunRepository<P extends ProcessingRun> extends JpaRepository<P, Long>{

    @Query("select pr from #{#entityName} pr where pr.name =:name and pr.experimentTemplate.id =:experiment")
    P findByNameAndExperiment(@Param("name")String name, @Param("experiment")long experiment);

}
