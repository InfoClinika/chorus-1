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
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.stats.S3ObjectProjection;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.infoclinika.mssharing.model.internal.repository.ChorusQueries.*;

/**
 * @author Stanislav Kurilin
 */
@Repository
public interface FileMetaDataRepository extends FileRepositoryTemplate<ActiveFileMetaData> {

    String SELECT_UNFINISHED_FILE_BY_USER = "from ActiveFileMetaData f join f.instrument i " +
        " where f.contentId is null and f.owner.id = :user and f.archiveId is null";
    String SELECT_CLAUSE = "select f from ActiveFileMetaData f ";

    String AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE = " join f.instrument i join f.specie s " +
        " where (f.contentId is not null or f.archiveId is not null) and i.id = :instrument and " +
        CHECK_SAME_SPECIE + " and f.invalid = false and " + IS_FILE_AVAILABLE;
    String AVAILABLE_FILES_BY_INSTRUMENT_MODEL = " join f.instrument i join f.specie s join f.instrument.lab l " +
        " where (f.contentId is not null or f.archiveId is not null) and i.model.id = :model and " +
        CHECK_SAME_SPECIE + " and (cast(:lab as integer) = 0 or l.id = :lab) and " +
        " f.invalid = false and " + IS_FILE_AVAILABLE;

    String SELECT_AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE = SELECT_CLAUSE + AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE;
    String SELECT_AVAILABLE_FILES_BY_INSTRUMENT_MODEL = SELECT_CLAUSE + AVAILABLE_FILES_BY_INSTRUMENT_MODEL;
    String SELECT_COUNT_CLAUSE = "select count(f) from ActiveFileMetaData f ";
    String JOIN_ADVANCED_FITER_PROPS = " left join f.instrument instrument left join f.metaInfo metaInfo left " +
        " join instrument.lab lab ";
    String FIND_MY = " where " +
        "(f.owner.id = :user " +
        "or f.instrument.lab.id in (select ulm.lab.id from UserLabMembership ulm where ulm.user.id = :user)" +
        ") ";
    String FIND_MY_WITH_FILTER = SELECT_CLAUSE + FIND_MY + " and (f.name like :query or f.labels like :query)";
    String FIND_MY_WITH_ADVANCED_FILTER = SELECT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_MY;
    String COUNT_MY_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_MY;

    String SELECT_AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE_WITH_ADVANCED_FILTER = SELECT_CLAUSE +
        JOIN_ADVANCED_FITER_PROPS +
        AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE;
    String COUNT_AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE +
        JOIN_ADVANCED_FITER_PROPS +
        AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE;

    String SELECT_AVAILABLE_FILES_BY_INSTRUMENT_MODEL_WITH_ADVANCED_FILTER = SELECT_CLAUSE +
        JOIN_ADVANCED_FITER_PROPS +
        AVAILABLE_FILES_BY_INSTRUMENT_MODEL;
    String COUNT_AVAILABLE_FILES_BY_INSTRUMENT_MODEL_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE +
        JOIN_ADVANCED_FITER_PROPS +
        AVAILABLE_FILES_BY_INSTRUMENT_MODEL;

    String FIND_SHARED = " where " +
        " (select count(*)  from ActiveExperiment e join e.rawFiles.data d join e.project p " +
        " where " +
        " f.id = d.fileMetaData.id " +
        " and (d.fileMetaData.owner.id <> :user " +
        " and f.instrument.lab.id not in (select ulm.lab.id from UserLabMembership ulm " +
        "    where ulm.user.id = :user) " +
        " and (p.sharing.type = " + SHARED_PROJECT +
        " and " + HAVE_ACCESS_TO_PROJECT + "))) > 0" +
        " and (select count(*) from ActiveExperiment ex join ex.rawFiles.data d join ex.project pr " +
        "    where pr.sharing.type = " + PUBLIC_PROJECT + " and d.fileMetaData = f) = 0";
    String FIND_SHARED_WITH_FILTER = SELECT_CLAUSE + FIND_SHARED + " and (f.name like :query or f.labels like :query) ";
    String FIND_SHARED_ADVANCED_FILTER = SELECT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_SHARED;
    String COUNT_SHARED_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_SHARED;

    String FIND_ALL_STARTING_WITH = " where " + IS_FILE_AVAILABLE;
    String FIND_ALL_STARTING_WITH_FILTER = SELECT_CLAUSE + FIND_ALL_STARTING_WITH +
        " and  (f.name like :query or f.labels like :query)";
    String FIND_ALL_STARTING_WITH_ADVANCED_FILTER = SELECT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_ALL_STARTING_WITH;
    String COUNT_ALL_STARTING_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + JOIN_ADVANCED_FITER_PROPS +
        FIND_ALL_STARTING_WITH;

    String FIND_PUBLIC = " where f.isDeleted = false and " +
        " (select count(*) from ActiveExperiment e join e.rawFiles.data d join e.project p" +
        " where f.id = d.fileMetaData.id and (" +
        " p.sharing.type = " + PUBLIC_PROJECT + ")) > 0 ";
    String FIND_PUBLIC_WITH_FILTER = SELECT_CLAUSE + FIND_PUBLIC + " and (f.name like :query or f.labels like :query)";
    String FIND_PUBLIC_WITH_ADVANCED_FILTER = SELECT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_PUBLIC;
    String COUNT_PUBLIC_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_PUBLIC;

    String FIND_BY_LAB = " where f.instrument.lab.id = :lab and " + IS_FILE_AVAILABLE;
    String FIND_BY_LAB_WITH_FILTER = SELECT_CLAUSE + FIND_BY_LAB +
        " and  (f.name like :query or f.labels like :query) ";
    String FIND_BY_LAB_WITH_ADVANCED_FILTER = SELECT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_BY_LAB;
    String COUNT_BY_LAB_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_BY_LAB;

    String FIND_BY_INSTRUMENT = " where instrument.id = :instrument and " + IS_FILE_AVAILABLE;
    String FIND_BY_INSTRUMENT_WITH_FILTER = SELECT_CLAUSE + " join f.instrument instrument " +
        FIND_BY_INSTRUMENT + " and (f.name like :query or f.labels like :query) ";
    String FIND_BY_INSTRUMENT_WITH_ADVANCED_FILTER = SELECT_CLAUSE + JOIN_ADVANCED_FITER_PROPS + FIND_BY_INSTRUMENT;
    String COUNT_BY_INSTRUMENT_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE + JOIN_ADVANCED_FITER_PROPS +
        FIND_BY_INSTRUMENT;

    String SELECT_CLAUSE_BY_EXPERIMENT = "select distinct f from RawFile rf join rf.fileMetaData f ";
    String SELECT_COUNT_CLAUSE_BY_EXPERIMENT = "select count(distinct f.id) from RawFile rf join rf.fileMetaData f ";
    String FIND_BY_EXPERIMENT = " where rf.experiment.id=:experiment ";
    String FIND_BY_EXPERIMENT_WITH_ADVANCED_FILTER = SELECT_CLAUSE_BY_EXPERIMENT + JOIN_ADVANCED_FITER_PROPS +
        FIND_BY_EXPERIMENT;
    String COUNT_BY_EXPERIMENT_WITH_ADVANCED_FILTER = SELECT_COUNT_CLAUSE_BY_EXPERIMENT + JOIN_ADVANCED_FITER_PROPS +
        FIND_BY_EXPERIMENT;
    String FILTER_CLAUSE = " and (cast(f.id as string) like :query or f.name like :query or instrument.name " +
        " like :query or lab.name like :query or f.labels like :query) ";

    @Override
    @Query("select case when f is not null then count(f.id) else 0 end from #{#entityName} f join f.instrument i " +
        " join f.specie s " +
        " where (f.contentId is not null or f.archiveId is not null) and i.id = :instrument" +
        " and f.invalid = false and " + IS_FILE_AVAILABLE + " group by f")
    Long countAvailableFilesByInstrument(@Param("user") long actor, @Param("instrument") long instrument);

    @Query(SELECT_CLAUSE + " where f.instrument=:instrument")
    List<ActiveFileMetaData> byInstrument(@Param("instrument") Instrument instrument);

    @Query("select count(f) from ActiveFileMetaData f where f.instrument=:instrument")
    long countByInstrument(@Param("instrument") Instrument instrument);

    //Problems with condition laziness. Need to optimize?
    @Override
    @Query(SELECT_CLAUSE + " join f.instrument instrument left join instrument.lab lab " +
        FIND_BY_INSTRUMENT + FILTER_CLAUSE)
    Page<ActiveFileMetaData> findByInstrument(
        @Param("instrument") long instrument,
        @Param("user") Long user,
        Pageable pageable,
        @Param("query") String s
    );

    @Override
    @Query("select f from RawFile rf join rf.fileMetaData f left join f.instrument instrument " +
        " left join instrument.lab lab where " +
        " rf.experiment.id=:experiment " + FILTER_CLAUSE)
    Page<ActiveFileMetaData> findByExperiment(
        @Param("experiment") long experiment,
        Pageable pageable,
        @Param("query") String s
    );

    @Query("select file.fileMetaData from ActiveExperiment e join e.rawFiles.data file where e.id = :exp")
    List<ActiveFileMetaData> findByExperiment(@Param("exp") Long experimentId);

    @Override
    @Query(SELECT_CLAUSE + " left join f.instrument instrument left join instrument.lab lab " +
        FIND_BY_LAB + FILTER_CLAUSE)
    Page<ActiveFileMetaData> findByLab(
        @Param("lab") long lab,
        @Param("user") Long user,
        Pageable pageable,
        @Param("query") String s
    );

    @Query(SELECT_CLAUSE + " where f.instrument = :instrument and f.name = :name")
    List<ActiveFileMetaData> findByInstrumentWithName(
        @Param("instrument") Instrument instrument,
        @Param("name") String fileName
    );

    @Query(SELECT_CLAUSE + " where (f.contentId = :content AND f.copy = 0)")
    @Nullable
    List<ActiveFileMetaData> findByContentId(@Param("content") String content);

    @Query("SELECT f FROM ActiveFileMetaData f WHERE (f.contentId in (:contentIds) AND f.copy=FALSE)")
    List<ActiveFileMetaData> findAllByContentId(@Param("contentIds") Set<String> contentIds);

    @Query("SELECT f FROM ActiveFileMetaData f WHERE (f.archiveId in (:archiveIds) AND f.copy=FALSE)")
    List<ActiveFileMetaData> findAllByArchiveId(@Param("archiveIds") Set<String> archiveIds);

    @Query(SELECT_CLAUSE + " left join f.instrument instrument left join instrument.lab lab " +
        " where f.isDeleted = false and " + IS_FILE_AVAILABLE + FILTER_CLAUSE)
    Page<ActiveFileMetaData> findAllStartingWith(
        @Param("user") long user,
        @Param("query") String query,
        Pageable pageable
    );

    @Query(SELECT_CLAUSE + " left join f.instrument instrument left join instrument.lab lab " +
        FIND_SHARED + FILTER_CLAUSE)
    Page<ActiveFileMetaData> findShared(@Param("user") long user, Pageable pageable, @Param("query") String query);

    @Query("select f.id from ActiveFileMetaData f where " +
        " (select count(*)  from ActiveExperiment e join e.rawFiles.data d join e.project p " +
        " where " +
        " f.id = d.fileMetaData.id " +
        " and (d.fileMetaData.owner.id <> :user " +
        " and " + COUNT_USER_MEMBER_OF_FILE_INSTRUMENT_LAB + " = 0 " +
        " and (p.sharing.type = " + SHARED_PROJECT +
        " and " + HAVE_ACCESS_TO_PROJECT + "))) > 0" +
        " and (select count(*) from ActiveExperiment ex join ex.rawFiles.data d " +
        " join ex.project pr where pr.sharing.type = " + PUBLIC_PROJECT + " and d.fileMetaData = f) = 0")
    List<Long> findAllSharedIds(@Param("user") long user);

    @Override
    @Query(SELECT_CLAUSE + " left join f.instrument instrument left join instrument.lab lab " +
        FIND_PUBLIC + FILTER_CLAUSE)
    Page<ActiveFileMetaData> findPublic(@Param("query") String query, Pageable pageable);

    @Override
    @Query(SELECT_CLAUSE + " where " +
        " (select count(e.id) from ActiveExperiment e join e.rawFiles.data d join e.project p" +
        " where f.id = d.fileMetaData.id and (" +
        "p.sharing.type = " + PUBLIC_PROJECT + ")) > 0")
    List<ActiveFileMetaData> findAllPublic();

    @Query(SELECT_CLAUSE + " left join f.instrument instrument left join instrument.lab lab " + FIND_MY + FILTER_CLAUSE)
    Page<ActiveFileMetaData> findMy(@Param("user") long user, Pageable pageable, @Param("query") String s);

    @Query("select file.fileMetaData.id from ActiveExperiment e join e.rawFiles.data file " +
        " where file.fileMetaData.id in (:files)")
    List<Long> usedInExperiments(@Param("files") Iterable<Long> files);

    @Query("select file.fileMetaData.id from ActiveExperiment e join e.rawFiles.data file where e.id = :exp")
    List<Long> idsByExperiment(@Param("exp") Long experimentId);

    @Query("select coalesce(sum(f.sizeInBytes), 0) from ActiveFileMetaData f")
    long sizeOfAll();

    @Query("select f.id from ActiveFileMetaData f left join f.metaInfo m where m is null")
    List<Long> findFileIdsWithoutMetaData();

    @Override
    @Query(SELECT_AVAILABLE_FILES_BY_INSTRUMENT_MODEL + " order by f.name")
    List<ActiveFileMetaData> availableFilesByInstrumentModel(
        @Param("user") long actor,
        @Param("model") long model,
        @Param("specie") long specie,
        @Param("lab") long lab
    );

    @Query(SELECT_AVAILABLE_FILES_BY_INSTRUMENT_MODEL)
    Page<ActiveFileMetaData> availableFilesByInstrumentModel(
        @Param("user") long actor,
        @Param("model") long model,
        @Param("specie") long specie,
        @Param("lab") long lab,
        Pageable pageable
    );

    @Override
    @Query(SELECT_AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE + " order by f.name")
    List<ActiveFileMetaData> availableFilesByInstrumentAndSpecie(
        @Param("user") long actor,
        @Param("specie") long specie,
        @Param("instrument") long instrument
    );

    @Query(SELECT_AVAILABLE_FILES_BY_INSTRUMENT_AND_SPECIE)
    Page<ActiveFileMetaData> availableFilesByInstrumentAndSpecie(
        @Param("user") long actor,
        @Param("specie") long specie,
        @Param("instrument") long instrument,
        Pageable pageable
    );

    @Query(SELECT_CLAUSE + " join f.instrument i join f.specie s " +
        "where (f.contentId is not null or f.archiveId is not null) and i.id = :instrument" +
        " and f.invalid = false and " + IS_FILE_AVAILABLE + " order by f.name")
    List<ActiveFileMetaData> availableFilesByInstrument(
        @Param("user") long actor,
        @Param("instrument") long instrument
    );

    @Query("select f.id " + SELECT_UNFINISHED_FILE_BY_USER + " and i.id = :instrument")
    List<Long> incompleteFiles(@Param("user") long actor, @Param("instrument") long instrument);

    @Query("select distinct i from ActiveFileMetaData f join f.instrument i where " +
        IS_FILE_AVAILABLE + " order by i.name")
    List<Instrument> instrumentsWithAvailableFiles(@Param("user") long actor);

    @Override
    @Query("select f " + SELECT_UNFINISHED_FILE_BY_USER)
    List<ActiveFileMetaData> unfinishedByUser(@Param("user") long actor);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.FileLastAccess(e.id, e.contentId," +
        " e.lastAccess, e.archiveId, lab.id) " +
        "from ActiveFileMetaData e join e.instrument i join i.lab lab")
    List<FileLastAccess> findLastAccessForAll();

    @Query(SELECT_CLAUSE + " join f.instrument i join i.lab where f.storageData.storageStatus " +
        " = com.infoclinika.mssharing.model.internal.entity.restorable.StorageData$Status.ARCHIVED ")
    List<ActiveFileMetaData> findAllArchivedFiles();

    @Query("select f.id from ActiveFileMetaData f join f.instrument i where i.lab.id =:lab")
    List<Long> idsByLab(@Param("lab") long lab);

    @Query(SELECT_CLAUSE + " join f.instrument i join i.lab where f.archiveId = :key")
    ActiveFileMetaData findByArchiveId(@Param("key") String key);

    @Query(SELECT_CLAUSE + " where f.storageData.storageStatus " +
        " = com.infoclinika.mssharing.model.internal.entity.restorable.StorageData$Status.UNARCHIVING_REQUESTED ")
    List<ActiveFileMetaData> findReadyToUnarchive();

    @Query("select f.id from ActiveFileMetaData f join f.instrument i join i.lab l where l.id =:lab")
    List<Long> findIdsByLab(@Param("lab") long lab);

    @Query("select f.id from ActiveFileMetaData f where f.storageData.toArchive = true ")
    List<Long> findIdsMarkedForArchiving();

    @Query(SELECT_CLAUSE + " join fetch f.instrument i join fetch i.lab where f.destinationPath = :path")
    ActiveFileMetaData findByDestinationPath(@Param("path") String path);

    @Query(SELECT_CLAUSE + " where f.storageData.lastUnarchiveTimestamp != null ")
    List<ActiveFileMetaData> findUnarchivedWIthExpirationTime();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update ActiveFileMetaData f set f.lastChargingSumDate=:date where f.id=:file")
    void updateLastChargingSumDate(@Param("file") long file, @Param("date") Date date);

    @Query("select min(p.sharing.type) from ActiveExperiment e join e.rawFiles.data d join e.project p" +
        " where :file = d.fileMetaData.id")
    Sharing.Type getSharingTypeThroughExperiment(@Param("file") long file);

    Page<ActiveFileMetaData> findBySizeIsConsistent(boolean consistent, Pageable pageable);

    @Query("select distinct f.id " +
        "from ActiveFileMetaData f " +
        "left join f.instrument i " +
        "left join i.lab l " +
        "left join f.owner u " +
        "where l.id not in(1,2,8,68,69,90,250) " +
        "and (f.contentId is not null or f.archiveId is not null) " +
        "and f.sizeIsConsistent = false ")
    List<Long> getInconsistentFilesIds();

    @Query("select f from ActiveFileMetaData f left join f.instrument instrument left join instrument.lab lab " +
        " left join f.owner own" +
        " where (f.name like :query or instrument.name like :query or lab.name " +
        " like :query or own.personData.firstName like :query or own.personData.lastName like :query) ")
    Page<ActiveFileMetaData> findAllWithFilter(@Param("query") String s, Pageable pageable);

    @Query("select f from ActiveFileMetaData f where f.id in (:ids) and " + IS_FILE_AVAILABLE)
    List<ActiveFileMetaData> findAllAvailable(@Param("user") long actor, @Param("ids") Iterable<Long> ids);

    @Query("select f from RawFile rf join rf.fileMetaData f where rf.id in (:ids)")
    List<ActiveFileMetaData> findAllByRawFileId(@Param("ids") Iterable<Long> rawFileIds);

    @Query("select f from ActiveFileMetaData f where f.name in :fileNamesList")
    List<ActiveFileMetaData> findAllByNamesList(@Param("fileNamesList") Iterable<String> fileNamesList);

    @Query("select f from ActiveFileMetaData f where f.name = :name and f.instrument.id = :instrumentId")
    List<ActiveFileMetaData> findByNameAndInstrument_id(
        @Param("name") String name,
        @Param("instrumentId") long instrumentId
    );

    Set<ActiveFileMetaData> findByNameInAndInstrument_id(List<String> name, long instrumentId);

    @Transactional
    @Modifying
    @Query("delete from ActiveFileMetaData fmd where fmd.id in (:ids)")
    void deleteByIds(@Param("ids") List<Long> ids);

    @Transactional
    @Modifying
    @Query("update ActiveFileMetaData f set f.toReplace = true, f.corrupted = true " +
        " where f.toReplace = false " +
        " and f.contentId is null " +
        " and f.archiveId is null" +
        " and f.uploadDate < :beforeDate")
    void markAllUnsuccessfullyUploadedFilesToReplaceAndCorrupted(@Param("beforeDate") Date beforeDate);

    @Query("select fmdt.id as id, " +
        " fmdt.contentId as path " +
        " from ActiveFileMetaData fmdt " +
        " inner join fmdt.instrument inst" +
        " inner join fmdt.instrument.lab lab" +
        " where fmdt.uploadDate >= :from " +
        " and fmdt.uploadDate < :to " +
        " and fmdt.contentId is not null " +
        " and lab.id = :labId")
    List<S3ObjectProjection> getAllRawFilesPerLab(@Param("labId") long labId,
                                               @Param("from") Date from,
                                               @Param("to") Date to);
}
