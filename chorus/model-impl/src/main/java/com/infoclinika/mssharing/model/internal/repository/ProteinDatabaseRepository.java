/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.ProteinDatabase;
import com.infoclinika.mssharing.model.internal.entity.User;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author andrii.loboda
 */
public interface ProteinDatabaseRepository extends CrudRepository<ProteinDatabase, Long> {
//    @Query("select distinct pdb from ProteinSearch ps join ps.data.experiment ex join ps.data.params.commonParams.db pdb where pdb.contentId is not null and ex.id in (:experimentIds)")
//    List<ProteinDatabase> findAllByExperimentIds(@Param("experimentIds") List<Long> experimentIds);

    //FIXME[Alexander Serebriyan]: find correct query
    @Query("select distinct pdb from ProteinDatabase pdb where pdb.contentId is not null")
    List<ProteinDatabase> findAllByExperimentIds();

    @Query("select pdb from ProteinDatabase pdb where (pdb.bPublic=true or pdb.user=:user) and pdb.contentId is not null ")
    List<ProteinDatabase> findMyAndPublic(@Param("user") User user);

    @Query("select pdb from ProteinDatabase pdb where pdb.user.id=:user and pdb.contentId is not null ")
    List<ProteinDatabase> findMy(@Param("user") long user);

    @Query("select pdb from ProteinDatabase pdb where pdb.bPublic=true and pdb.contentId is not null ")
    List<ProteinDatabase> findPublic();

    @Query("select case when (count(pdb) = 0) then true else false end " +
            "from ProteinDatabase pdb " +
            "where pdb.name = :proteinDatabaseName " +
            "and pdb.id <> :proteinDatabaseId")
    boolean hasUniqueName(@Param("proteinDatabaseId") long proteinDatabaseId, @Param("proteinDatabaseName") String proteinDatabaseName);

    @Query("select db from ProteinDatabase db where db.status=:status")
    List<ProteinDatabase> findAllByStatus(@Param("status") ProteinDatabase.ProteinDatabaseStatus status);

    @Query("select db from ProteinDatabase db where db.name=:databaseName")
    ProteinDatabase findByName(@Param("databaseName") String databaseName);
}
