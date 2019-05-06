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

import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static com.infoclinika.mssharing.model.internal.repository.ChorusQueries.*;

/**
 * @author Stanislav Kurilin
 */
public interface ExperimentRepository extends ExperimentRepositoryTemplate<ActiveExperiment> {

    String EXPERIMENT_DASHBOARD_RECORD =
        " new com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord(" +
            " e.id," +
            " e.name," +
            " nullableLab," +
            " e.project," +
            " e.creator," +
            " count(file.id)," +
            " e.lastModification," +
            " e.downloadToken," +
            " e.experimentCategory," +
            " e.failed," +
            " e.experimentType" +
            ") ";
    String SELECT_CLAUSE = "select distinct e  from ExperimentDashboardRecord e ";
    String SELECT_COUNT_CLAUSE = "select count( distinct e) from ExperimentDashboardRecord e ";

    String FIND_ALL_AVAILABLE =
            " left join e.project p" +
            " where e.deleted = 0 and ( e.creator.id = :user or " +
            "p.creator.id = :user " +
            " or p.sharing.type = " + PUBLIC_PROJECT +
            " or " + HAVE_ACCESS_TO_PROJECT +
            " or " + IS_LAB_MEMBER + ")";
    String FIND_ALL_AVAILABLE_WITH_FILTER = SELECT_CLAUSE + FIND_ALL_AVAILABLE + " AND e.name like :s";
    String FIND_ALL_AVAILABLE_WITH_ADVANCED_FILTER = SELECT_CLAUSE + FIND_ALL_AVAILABLE;
    String COUNT_ALL_AVAILABLE_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + FIND_ALL_AVAILABLE;

    String FIND_PUBLIC =
            " left join e.project p where e.deleted = 0 and (p.sharing.type = " + PUBLIC_PROJECT + ")";
    String FIND_PUBLIC_WITH_FILTER = SELECT_CLAUSE + FIND_PUBLIC + " AND e.name like :s ";
    String FIND_PUBLIC_WITH_ADVANCED_FILTER = SELECT_CLAUSE + FIND_PUBLIC;
    String COUNT_PUBLIC_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + FIND_PUBLIC;

    String FIND_MY =
            " left join e.project p where e.deleted = 0   and e.creator.id = :user";
    String FIND_MY_WITH_FILTER = SELECT_CLAUSE + FIND_MY + " AND e.name like :s";
    String FIND_MY_WITH_ADVANCED_FILTER = SELECT_CLAUSE + FIND_MY;
    String COUNT_MY_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + FIND_MY;

    String FIND_SHARED =
            " left join e.project p " +
            "  where e.deleted = 0 \n" +
            " and (e.creator.id <> :user " +
            " and p.sharing.type = " + SHARED_PROJECT +
            " and (p.creator.id = :user or " + HAVE_ACCESS_TO_PROJECT + "))" +
            " or " + IS_LAB_MEMBER;

    String FIND_SHARED_WITH_FILTER = SELECT_CLAUSE + FIND_SHARED + " AND e.name like :s";
    String FIND_SHARED_WITH_ADVANCED_FILTER = SELECT_CLAUSE + FIND_SHARED;
    String COUNT_SHARED_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + FIND_SHARED;

    String FIND_ALL_BY_LAB =
            //" left join e.project p left join e.lab l" +
            " where e.lab is not null AND e.lab.id=:lab and e.deleted = 0";
    String FIND_ALL_BY_LAB_WITH_FILTER = SELECT_CLAUSE + FIND_ALL_BY_LAB + " and e.name like :s";
    String FIND_ALL_BY_LAB_WITH_ADVANCED_FILTER = SELECT_CLAUSE + FIND_ALL_BY_LAB;
    String COUNT_ALL_BY_LAB_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + FIND_ALL_BY_LAB;

    String FIND_BY_PROJECT = "where e.deleted=0 AND e.project = :project ";
    String FIND_BY_PROJECT_WITH_FILTER = SELECT_CLAUSE + FIND_BY_PROJECT + " and e.name like :query";
    String FIND_BY_PROJECT_WITH_ADVANCED_FILTER = SELECT_CLAUSE + FIND_BY_PROJECT;
    String COUNT_BY_PROJECT_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + FIND_BY_PROJECT;
    String FILTER_CLAUSE = " and (cast(e.id as string) like :s or e.name like :s or own.personData.firstName like :s " +
        " or own.personData.lastName like :s or lab.name like :s or p.name like :s) ";

    @Query("select count(*) from ActiveExperiment e where e.project.sharing.type = " + PUBLIC_PROJECT)
    long countOnlyPublic();

    @Query("select e from ExperimentDashboardRecord e where e.deleted = false and e.project.id = :project")
    List<ExperimentDashboardRecord> findRecordsByProject(@Param("project") long project);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.ShortExperimentDashboardRecord(" +
            "e.id, e.name, e.creator.personData.email, e.creator.id, e.lastModification, e.analyzesCount" +
            ")" +
           " from ExperimentDashboardRecord e where e.deleted = false and e.project.id = :project")
    List<ShortExperimentDashboardRecord> findShortRecordsByProject(@Param("project") long project);


    @Query("select e from ExperimentDashboardRecord e where e.deleted=0 " +
        " AND e.project = :project and e.name like :query")
    Page<ExperimentDashboardRecord> findByRecoredsProject(
        @Param("project") ActiveProject project,
        Pageable request,
        @Param("query") String query
    );

    @Query(FIND_BY_PROJECT_WITH_FILTER)
    Page<ExperimentDashboardRecord> findByProject(
        @Param("project") ActiveProject project,
        Pageable request,
        @Param("query") String query
    );

    @Query("select " +  EXPERIMENT_DASHBOARD_RECORD +
            " from ActiveExperiment e left join e.rawFiles.data file left join e.lab nullableLab " +
        "where e.project = :project group by e.id")
    List<ExperimentDashboardRecord> findDashboardItemsByProject(@Param("project") ActiveProject project);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.ExperimentShortRecord(" +
        "e.id, e.name, e.project, e.creator) from ActiveExperiment e where e.project = :project")
    List<ExperimentShortRecord> findShortItemsByProject(@Param("project") ActiveProject project);

    @Query("select count(e) from ActiveExperiment e where e.instrumentRestriction.instrument = :instrument")
    long countRestrictedTo(@Param("instrument") Instrument instrument);

    @Query("select e from ActiveExperiment e join e.rawFiles.data file where file.fileMetaData=:metaFile")
    List<ActiveExperiment> findByFile(@Param("metaFile") ActiveFileMetaData fileMetaData);

    @Query("select e from ActiveExperiment e where e.creator.id = :user and e.name = :name")
    List<ActiveExperiment> findByName(@Param("user") long user, @Param("name") String experimentName);

    @Query("select e from ActiveExperiment e where e.creator.id = :user and e.lab.id = :lab and e.name = :name")
    ActiveExperiment findByNameInLab(
        @Param("user") long user,
        @Param("lab") long lab,
        @Param("name") String experimentName
    );

    @Query("select distinct e" +
            " from ExperimentDashboardRecord e left join e.project p" +
            " where e.deleted = 0 AND e.name like :s and ( e.creator.id = :user or " +
            "p.creator.id = :user " +
            " or p.sharing.type = " + PUBLIC_PROJECT +
            " or " + HAVE_ACCESS_TO_PROJECT + ")")
    Page<ExperimentDashboardRecord> findAllAvailableRecords(
        @Param("user") long user,
        Pageable request,
        @Param("s") String filterQuery
    );

    @Override
    @Query("select e from ActiveExperiment e where e.downloadToken is not null and e.downloadToken = :token")
    ActiveExperiment findOneByToken(@Param("token") String token);

    @Query(SELECT_CLAUSE + " left join e.creator own left join e.lab lab " + FIND_ALL_AVAILABLE + FILTER_CLAUSE)
    Page<ExperimentDashboardRecord> findAllAvailable(
        @Param("user")long user,
        Pageable request,@Param("s")
            String filterQuery
    );

    @Query("select distinct e.id" +
            " from ExperimentDashboardRecord e left join e.project p" +
            " where e.deleted = 0 and ( e.creator.id = :user or " +
            "p.creator.id = :user " +
            " or p.sharing.type = " + PUBLIC_PROJECT +
            " or " + HAVE_ACCESS_TO_PROJECT + ")")
    List<Long> findAllAvailableIds(@Param("user")long user);

    @Query(SELECT_CLAUSE + " left join e.project p left join e.creator own left join e.lab lab " +
        FIND_ALL_BY_LAB + FILTER_CLAUSE)
    Page<ExperimentDashboardRecord> findAllRecordsByLab(
        @Param("lab") long lab,
        Pageable request,
        @Param("s") String filterQuery
    );

    @Query("select distinct e" +
            " from ExperimentDashboardRecord e left join e.project p " +
            "  where e.deleted = 0 AND e.name like :s \n" +
            " and (e.creator.id <> :user " +
            " and p.sharing.type = " + SHARED_PROJECT +
            " and (p.creator.id = :user or " + HAVE_ACCESS_TO_PROJECT + "))")
    Page<ExperimentDashboardRecord> findSharedRecords(
        @Param("user") long user,
        Pageable request,
        @Param("s") String filterQuery
    );

    @Query(SELECT_CLAUSE + " left join e.creator own left join e.lab lab " + FIND_SHARED + FILTER_CLAUSE)
    Page<ExperimentDashboardRecord> findShared(
        @Param("user")long user,
        Pageable request,
        @Param("s") String filterQuery
    );

    @Query("select distinct e" +
            " from ExperimentDashboardRecord e where e.deleted = 0 AND e.name like :s  and e.creator.id = :user")
    Page<ExperimentDashboardRecord> findMyRecords(
        @Param("user") long user,
        Pageable request,
        @Param("s") String filterQuery
    );

    @Query(SELECT_CLAUSE + " left join e.creator own left join e.lab lab " + FIND_MY + FILTER_CLAUSE)
    Page<ExperimentDashboardRecord> findMy(@Param("user")long user, Pageable request,@Param("s") String filterQuery);

    @Query(SELECT_CLAUSE + " left join e.creator own left join e.lab lab " + FIND_PUBLIC + FILTER_CLAUSE)
    Page<ExperimentDashboardRecord> findPublicRecords(@Param("s") String filterQuery, Pageable request);

    @Query("select file from ActiveExperiment e join e.rawFiles.data file where e.id=:experimentId")
    List<RawFile> findFilesByExperimentId(@Param("experimentId") long experiment);

    @Query("select file from ActiveExperiment e join e.rawFiles.data file join file.fileMetaData metaFile " +
        "where e.id=:experimentId and metaFile.id in(:fileMetaDataIds)")
    List<RawFile> findRawFilesByExperimentIdAndMetaData(
        @Param("experimentId") long experiment,
        @Param("fileMetaDataIds") List<Long> fileMetaDataIds
    );

    /* For Search purposes */

    @Query(ALL_AVAILABLE_EXPERIMENTS_WITH_QUERY)
    List<ExperimentDashboardRecord> searchExperimentsRecords(@Param("user") long user, @Param("query") String query);

    @Query(ALL_AVAILABLE_EXPERIMENTS_WITH_QUERY)
    Page<ExperimentDashboardRecord> searchPagedExperimentsRecords(
        @Param("user") long user,
        @Param("query") String query,
        Pageable pageable
    );

    @Query("select new com.infoclinika.mssharing.model.internal.repository.ExperimentAdditionalInfoRecord(" +

            " e.id, " +
            //[1] isUserCanCreateExperimentsInProject
            " (select count(distinct _e.id) " +
            "from ActiveExperiment _e " +
            "left join _e.project _p " +
            "left join _p.lab _l " +
            "left join _l.labMemberships _ms " +
            "left join _p.sharing.collaborators _puc " +
            "left join _p.sharing.groupsOfCollaborators _pgc " +
            "left join _pgc.group _pgg " +
            "left join _pgg.collaborators _pggc " +
            "where _e.id = e.id " +
            "and (" +
            "_p.creator.id = :user " +
            "or (_ms.head = true and _ms.user.id = :user) " +
            "or (_puc.user.id = :user and _puc.level = " + WRITE_ACCESS + ") " +
            "or (_pggc.id = :user and _pgc.accessLevel = " + WRITE_ACCESS + ") " +
            ")), " +
            //[2] isExperimentReadyToDownload(true if the count equals to files count)
            " (select count(distinct _f.id) " +
            "from RawFile _f left join _f.experiment _exp left join _f.fileMetaData _fmd " +
            "where _exp.id = e.id " +
            "and _fmd.storageData.storageStatus = " + STORAGE_STATUS_UNARCHIVED + "), " +
            //[3] canArchiveExperiment
            " (select count(distinct _e.id)" +
            " from ActiveExperiment _e " +
            " left join _e.lab _l " +
            " left join _l.labMemberships _ms " +
            " left join _e.rawFiles.data _rf " +
            " left join _rf.fileMetaData _fmd " +
            " where _e.id = e.id and (_e.creator.id = :user or (_ms.user.id = :user and _ms.head = true)) " +
            "    and (select count(distinct _f.id) from RawFile _f left join _f.experiment _exp " +
            "       left join _f.fileMetaData _f_fmd where _exp.id = _e.id and _f_fmd.storageData.storageStatus in " +
            "          (" + STORAGE_STATUS_UNARCHIVED + "," + STORAGE_STATUS_UNARCHIVING + ")" +
            "             and (_f_fmd.owner.id = :user or (_ms.user.id = :user and _ms.head = true))) > 0 " +
            "), " +
            //[4] canUnarchiveExperiment
            " (select count(distinct _e.id)" +
            "   from ActiveExperiment _e " +
            " left join _e.lab _l " +
            " left join _l.labMemberships _ms " +
            " left join _e.rawFiles.data _rf " +
            " left join _rf.fileMetaData _fmd " +
            " where _e.id = e.id and (_e.creator.id = :user or (_ms.user.id = :user and _ms.head = true)) " +
            "    and (select count(distinct _f.id) from RawFile _f left join _f.experiment _exp " +
            "       left join _f.fileMetaData _f_fmd " +
            "         where _exp.id = _e.id and (_f_fmd.storageData.storageStatus != " + STORAGE_STATUS_UNARCHIVED +
            "           and (_f_fmd.owner.id = :user or (_ms.user.id = :user and _ms.head = true) ) ) ) > 0 " +
            "), " +
            //[5] count files archivedDownloadOnly (count archived files of requested for download only)
            " (select count(distinct _e.id)" +
            "   from ActiveExperiment _e " +
            " left join _e.lab _l " +
            " left join _l.labMemberships _ms " +
            " left join _e.rawFiles.data _rf " +
            " left join _rf.fileMetaData _fmd " +
            " where _e.id = e.id and (_e.creator.id = :user or (_ms.user.id = :user and _ms.head = true)) " +
            "    and (select count(distinct _f.id) from RawFile _f left join _f.experiment _exp " +
            "      left join _f.fileMetaData _f_fmd " +
            "         where _exp.id = _e.id and (_f_fmd.storageData.archivedDownloadOnly is true " +
            "           and (_f_fmd.owner.id = :user or (_ms.user.id = :user and _ms.head = true) ) ) ) > 0 " +
            "), " +
            //[6] count files UnArchiveRequest (count archived files of requested for unarchiving)
            " (select count(distinct _e.id)" +
            "   from ActiveExperiment _e " +
            " left join _e.lab _l " +
            " left join _l.labMemberships _ms " +
            " left join _e.rawFiles.data _rf " +
            " left join _rf.fileMetaData _fmd " +
            " where _e.id = e.id and (_e.creator.id = :user or (_ms.user.id = :user and _ms.head = true)) " +
            "    and (select count(distinct _f.id) from RawFile _f left join _f.experiment _exp " +
            "      left join _f.fileMetaData _f_fmd " +
            "         where _exp.id = _e.id and (_f_fmd.storageData.storageStatus = " + STORAGE_STATUS_UNARCHIVING +
            "           and (_f_fmd.owner.id = :user or (_ms.user.id = :user and _ms.head = true) ) ) ) > 0 " +
            ") " +
            ") " +
            " from ActiveExperiment e " +
            " where e.id in(:ids) group by e.id")
    List<ExperimentAdditionalInfoRecord> getAdditionalInfo(
        @Param("user") long user,
        @Param("ids") List<Long> ids
    );

    @Query("select rf.fileMetaData from ActiveExperiment e join e.rawFiles.data rf where e.id = :experiment " +
        "and rf.fileMetaData.id in (select rf2.fileMetaData.id from ActiveExperiment e2 join e2.rawFiles.data rf2 " +
        "where e2.id != e.id)")
    List<ActiveFileMetaData> filesInOtherExperiments(@Param("experiment") long experiment);

    @Query("select distinct e from ActiveExperiment e left join e.lab lab left join e.creator own " +
            " where (e.name like :s or own.personData.firstName like :s or own.personData.lastName like :s " +
            " or lab.name like :s) ")
    Page<ActiveExperiment> findAllWithFilter(@Param("s") String query, Pageable request);

    @Query("select count(e) from ActiveExperiment e where e.lab.id = :labId")
    int countByLabId(@Param("labId") Long labId);

    @Query("select count(e) from ActiveExperiment e where e.billLaboratory.id = :billLabId")
    int countByBillLabId(@Param("billLabId") Long billLabId);
}

