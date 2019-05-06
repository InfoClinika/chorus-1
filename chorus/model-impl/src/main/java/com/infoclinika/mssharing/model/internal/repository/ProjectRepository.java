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

import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.view.ProjectDashboardRecord;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static com.infoclinika.mssharing.model.internal.repository.ChorusQueries.PUBLIC_PROJECT;
import static com.infoclinika.mssharing.model.internal.repository.ChorusQueries.SHARED_PROJECT;
import static com.infoclinika.mssharing.platform.repository.QueryTemplates.HAVE_ACCESS_TO_PROJECT;

/**
 * @author Stanislav Kurilin
 */
public interface ProjectRepository extends ProjectRepositoryTemplate<ActiveProject> {

    String FILTER_CLAUSE = " and (cast(p.id as string) like :s or p.name like :s or owner.personData.firstName " +
        " like :s or owner.personData.lastName like :s or p.areaOfResearch like :s " +
        " or cast(p.lastModification as string) " +
        " like :s) ";

    @Query("select count(*) from ActiveProject p where p.sharing.type = " + PUBLIC_PROJECT)
    long countOnlyPublic();

    /*
     * Pageable
     */

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p " +
        "  where  pr.deleted=0 AND p.sharing.type = " + SHARED_PROJECT +
        " and p.name like :s  and p.creator.id <> :user and " + HAVE_ACCESS_TO_PROJECT)
    Page<ProjectDashboardRecord> sharedProjects(
        @Param("user") long user,
        Pageable request,
        @Param("s") String nameFilter
    );

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p " +
        "  where  pr.deleted=0 AND p.sharing.type = " + SHARED_PROJECT +
        " and p.creator.id <> :user and " + HAVE_ACCESS_TO_PROJECT)
    List<ProjectDashboardRecord> sharedProjects(@Param("user") long user);

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p join pr.lab l where pr.deleted=0 AND " +
        "l is not null and l.id=:lab AND p.name like :s")
    Page<ProjectDashboardRecord> findByLab(@Param("lab") long lab, @Param("s") String nameFilter, Pageable request);

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p join pr.lab l where pr.deleted=0 AND " +
        "l is not null and l.id=:lab")
    List<ProjectDashboardRecord> findByLab(@Param("lab") long lab);

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p where p.creator.id=:user and p.name " +
        " like :s and pr.deleted=0")
    Page<ProjectDashboardRecord> privateProjects(
        @Param("user") long user,
        Pageable request,
        @Param("s") String nameFilter
    );

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p " +
        " where p.creator.id=:user and pr.deleted=0")
    List<ProjectDashboardRecord> privateProjects(@Param("user") long user);

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p where pr.deleted=0 AND " +
        " p.creator.id <> :user and p.name like :s and p.sharing.type = " + PUBLIC_PROJECT)
    Page<ProjectDashboardRecord> publicProjectsRecordsNotOwned(
        @Param("user") long user,
        @Param("s") String nameFilter,
        Pageable request
    );

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p " +
        "  where pr.deleted=0 AND p.name like :s and (p.sharing.type = " + PUBLIC_PROJECT +
        " or p.creator.id = :user or " + HAVE_ACCESS_TO_PROJECT + ")")
    Page<ProjectDashboardRecord> findAllAvailableRecords(
        @Param("user") long user,
        @Param("s") String nameFilter,
        Pageable request
    );

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p " +
        "  where pr.deleted=0 and (p.sharing.type = " + PUBLIC_PROJECT +
        " or p.creator.id = :user or " + HAVE_ACCESS_TO_PROJECT + ")")
    List<ProjectDashboardRecord> findAllAvailableRecords(@Param("user") long user);

    /*
     * Non-pageable
     */

    @Deprecated
    @Query("select distinct pr from ProjectDashboardRecord pr join pr.project p" +
        " where pr.deleted=false " +
        " and p.creator.id<>:user" +
        " and p.sharing.type = " + PUBLIC_PROJECT)
    List<ProjectDashboardRecord> publicProjectsNotOwned(@Param("user") long user);

    @Override
    @Query("select p from ActiveProject p " +
        " left join p.lab lab " +
        " left join p.creator owner " +
        " where lab is not null and lab.id=:lab " + FILTER_CLAUSE)
    Page<ActiveProject> findByLabAndName(@Param("lab") long lab, @Param("s") String nameFilter, Pageable request);

    @Override
    @Query("select p from ActiveProject p " +
        " left join p.lab lab " +
        " left join p.creator owner " +
        " where  p.isDeleted=false" +
        " AND p.sharing.type = " + SHARED_PROJECT + FILTER_CLAUSE +
        " and p.creator.id <> :user " +
        " and " + HAVE_ACCESS_TO_PROJECT)
    Page<ActiveProject> findSharedNotOwned(@Param("user") long user, @Param("s") String nameFilter, Pageable request);

    @Override
    @Query("select p from ActiveProject p" +
        " left join p.lab lab" +
        " left join p.creator owner " +
        " where p.creator.id=:user " + FILTER_CLAUSE +
        " and p.isDeleted=false")
    Page<ActiveProject> findMy(@Param("user") long user, @Param("s") String nameFilter, Pageable request);

    @Query("select p from ActiveProject p " +
        " left join p.lab lab " +
        " left join p.creator owner " +
        " where p.isDeleted=false" + FILTER_CLAUSE +
        " and p.creator.id <> :user " +
        " and p.sharing.type = " + PUBLIC_PROJECT)
    Page<ActiveProject> findPublicNotOwned(@Param("user") long user, @Param("s") String nameFilter, Pageable request);

    @Query("select p from ActiveProject p " +
        " left join p.lab lab " +
        " left join p.creator owner " +
        " where p.isDeleted=false " + FILTER_CLAUSE +
        " and (p.sharing.type = " + PUBLIC_PROJECT +
        " or p.creator.id = :user" +
        " or " + HAVE_ACCESS_TO_PROJECT + ")")
    Page<ActiveProject> findAllAvailable(@Param("user") long user, @Param("s") String nameFilter, Pageable request);

    @Query("select count(p) from ActiveProject p where p.lab.id = :labId")
    long countByLabId(@Param("labId") Long labId);

}
