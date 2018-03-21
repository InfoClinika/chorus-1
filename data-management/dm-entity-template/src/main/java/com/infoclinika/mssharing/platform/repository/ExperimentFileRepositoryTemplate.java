package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface ExperimentFileRepositoryTemplate<T extends ExperimentFileTemplate> extends JpaRepository<T, Long> {

    @Query("select rawFile from #{#entityName} rawFile where rawFile.fileMetaData=:fileMetaData")
    List<T> findByMetaData(@Param("fileMetaData") FileMetaDataTemplate fileMetaData);

    @Query("select rawFile from #{#entityName} rawFile where rawFile.fileMetaData.id=:id")
    List<T> findByMetaData(@Param("id") long fileMetaDataId);
}
