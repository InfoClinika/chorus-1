package com.infoclinika.mssharing.model.internal.repository;


import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProcessingFileRepository extends JpaRepository<ProcessingFile, Long>{

    String BOOLEAN_SELECT_STATEMENT = "select case when count(pf.id)>0 then true else false end from #{#entityName} pf ";


    @Query(BOOLEAN_SELECT_STATEMENT + "where pf.experimentTemplate.id=:experimentId and pf.name=:fileName")
    boolean isProcessingFileAlreadyUploadedToExperiment(@Param("experimentId") long experimentId, @Param("fileName") String fileName);

    @Query("select pf from  #{#entityName} pf where pf.name=:name and pf.experimentTemplate.id=:experimentId")
    ProcessingFile findByName(@Param("name") String processingFileName, @Param("experimentId") long experimentId);
}
