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
import com.infoclinika.mssharing.model.internal.entity.ProteinDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * @author andri.loboda
 */
public interface ProteinDescriptionRepository extends JpaRepository<ProteinDescription, Long> {
    @Query("select pd from ProteinDescription pd where pd.proteinId in (:proteinIDs) and pd.database = :db")
    List<ProteinDescription> findByProteinIDsAndDB(@Param("proteinIDs") Set<String> proteinIDs, @Param("db") ProteinDatabase db);

    @Query("select distinct pd.proteinId from ProteinDescription pd where pd.proteinId in (:proteinIDs) and pd.database = :db")
    Set<String> findPersistedProteinsByProteinIDsAndDB(@Param("proteinIDs") Set<String> proteinIDs, @Param("db") ProteinDatabase db);

    @Query("select pd from ProteinDescription pd where pd.id in (:ids)")
    List<ProteinDescription> findAllByIds(@Param("ids") Set<Long> apIds);

    @Query("select pd from ProteinDescription pd join pd.database db where db.id=:dbId")
    List<ProteinDescription> findByDatabase(@Param("dbId") long dbId);
}
