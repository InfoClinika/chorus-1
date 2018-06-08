package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ProcessingRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ProcessingRunRepository<P extends ProcessingRun> extends JpaRepository<P, Long>{

    @Query("select pr from #{#entityName} pr where pr.name =:name and pr.experimentTemplate.id =:experiment")
    P findByNameAndExperiment(@Param("name")String name, @Param("experiment")long experiment);

    @Query("select pr from #{#entityName} pr where pr.experimentTemplate.id =:experiment")
    List<P> findAll(@Param("experiment")long experiment);

    @Query("select pr from #{#entityName} pr where pr.id =:id and pr.experimentTemplate.id =:experiment")
    P findByIdAndExperimentId(@Param("experiment")long experiment, @Param("id")long id);

}
