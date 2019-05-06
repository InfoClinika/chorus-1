/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.platform.repository.ExperimentFileRepositoryTemplate;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.infoclinika.mssharing.platform.repository.QueryTemplates.IS_FILE_AVAILABLE;

/**
 * @author Stanislav Kurilin
 */
@Repository("experimentFileRepository")
public interface RawFilesRepository extends ExperimentFileRepositoryTemplate<RawFile> {

    @Query("select rawFile from RawFile rawFile where rawFile.experiment.id=:experiment " +
        " and rawFile.fileMetaData.id=:file")
    RawFile findByFileAndExperiment(@Param("file") long file, @Param("experiment") long experiment);

    @Query("select rf from RawFile rf join rf.fileMetaData f where rf.id in (:ids) and " + IS_FILE_AVAILABLE +
        " order by field(rf.id, :ids)")
    List<RawFile> findAllAvailable(@Param("user") long actor, @Param("ids") Iterable<Long> ids);

    @Query("select rawFile from RawFile rawFile where rawFile.experiment.id=:experiment " +
        " and rawFile.fileMetaData.name=:fileName")
    RawFile findByFileNameAndExperiment(@Param("fileName") String fileName, @Param("experiment") long experiment);

    @Transactional
    @Modifying
    @Query("delete from RawFile rf where rf.experiment.id=:experimentId")
    void deleteByExperimentId(@Param("experimentId") Long experimentId);
}
