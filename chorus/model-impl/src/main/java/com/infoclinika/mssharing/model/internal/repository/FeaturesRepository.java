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

import com.infoclinika.mssharing.model.internal.entity.Feature;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

import static com.infoclinika.mssharing.model.internal.repository.ChorusQueries.FEATURE_ENABLED_PER_LAB_STATE;
import static com.infoclinika.mssharing.model.internal.repository.ChorusQueries.FEATURE_ENABLED_STATE;

/**
 * @author pavel.kaplin, andrii.loboda, herman.zamula, timofei.kasianov
 */
public interface FeaturesRepository extends CrudRepository<Feature, String> {

    @Query("select distinct f from Feature f " +
        " where f.enabledState = " + FEATURE_ENABLED_STATE +
        " or (f.enabledState = " + FEATURE_ENABLED_PER_LAB_STATE +
        " and (select count(ff) from Feature ff join ff.enabledLabs l " +
        " where ff.name = f.name and l.id = :labId) > 0)")
    Set<Feature> enabledForLab(@Param("labId") long labId);

    @Query("select (count(f) > 0) from Feature f " +
        " where (f.name = :name and f.enabledState = " + FEATURE_ENABLED_STATE + ") " +
        " or (f.enabledState = " + FEATURE_ENABLED_PER_LAB_STATE + " and (select count(ff) from Feature ff " +
        " join ff.enabledLabs l where ff.name = f.name and l.id = :labId) > 0)")
    boolean isEnabledForLab(@Param("name") String name, @Param("labId") long labId);

}
