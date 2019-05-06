package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static com.infoclinika.mssharing.platform.repository.QueryTemplates.*;

/**
 * @author Herman Zamula
 */
public interface FileRepositoryTemplate<T extends FileMetaDataTemplate> extends JpaRepository<T, Long> {

    String UNSPECIFIED_NAME = "Unspecified";
    String CHECK_SAME_SPECIE = "((select s.name from Species s where s.id = :specie) in ('" + UNSPECIFIED_NAME + "') " +
        "or s.name = '" + UNSPECIFIED_NAME + "' or s.id = :specie)";
    String COUNT_USER_MEMBER_OF_FILE_INSTRUMENT_LAB = "(select count(*) as c from UserLabMembership ulm " +
        "left join ulm.user ulm_u left join ulm.lab ulm_l where ulm_u.id = :user and f.instrument.lab = ulm_l) ";
    String BOOLEAN_SELECT_STATEMENT = "select case when count(f.id)>0 then true else false end from #{#entityName} f ";


    /* Find by filter paged */

    @Query("select f from FileMetaDataTemplate f " +
        "where f.isDeleted = false and (f.name like :query or f.labels like :query) and " + IS_FILE_AVAILABLE)
    Page<T> findAllAvailable(@Param("user") long user, @Param("query") String query, Pageable pageable);

    @Query("select distinct f from #{#entityName} f join f.instrument i " +
        "where f.isDeleted = false and  " + IS_FILE_AVAILABLE + " order by f.name")
    List<T> findAllAvailable(@Param("user") long actor);

    @Query("select f from FileMetaDataTemplate f where f.isDeleted = false and " +
        "(f.owner.id = :user or " + COUNT_USER_MEMBER_OF_FILE_INSTRUMENT_LAB + " > 0 ) " +
        "and (f.name like :query or f.labels like :query)")
    Page<T> findMy(@Param("user") long user, @Param("query") String query, Pageable pageable);

    @Query(
        "select f from FileMetaDataTemplate f where f.isDeleted = false and (f.name like :query or " +
            "f.labels like :query) and" +
            " (select count(*)  from ExperimentTemplate e join e.rawFiles.data d join e.project p " +
            " where e.isDeleted = false " +
            " and f.id = d.fileMetaData.id " +
            " and (d.fileMetaData.owner.id <> :user " +
            " and " + COUNT_USER_MEMBER_OF_FILE_INSTRUMENT_LAB + " = 0 " +
            " and (p.sharing.type = " + SHARED_PROJECT +
            " and " + HAVE_ACCESS_TO_PROJECT + "))) > 0" +
            " and (select count(*) from ExperimentTemplate ex join ex.rawFiles.data d join ex.project pr " +
            "where ex.isDeleted = false and pr.sharing.type = " +
            PUBLIC_PROJECT + " and d.fileMetaData = f) = 0")
    Page<T> findShared(@Param("user") long user, @Param("query") String query, Pageable pageable);

    @Query("select f from FileMetaDataTemplate f where f.isDeleted = false and " +
        " (select count(*) from ExperimentTemplate e join e.rawFiles.data d join e.project p " +
        " where e.isDeleted = false and f.id = d.fileMetaData.id and (" +
        "p.sharing.type = " + PUBLIC_PROJECT + ")) > 0 and (f.name like :query or f.labels like :query)")
    Page<T> findPublic(@Param("query") String query, Pageable pageable);

    /* Find by filter */

    @Query("select f from FileMetaDataTemplate f where f.isDeleted = false and " +
        "(f.owner.id = :user " +
        "or " + COUNT_USER_MEMBER_OF_FILE_INSTRUMENT_LAB + " > 0) ")
    List<T> findAllMy(@Param("user") long user);

    @Query("select f from #{#entityName} f where f.isDeleted = false and " +
        " (select count(*)  from ExperimentTemplate e join e.rawFiles.data d join e.project p " +
        // JOIN_COLLABORATORS +
        " where  " +
        " e.isDeleted = false " +
        " and f.id = d.fileMetaData.id " +
        " and (d.fileMetaData.owner.id <> :user " +
        " and " + COUNT_USER_MEMBER_OF_FILE_INSTRUMENT_LAB + " = 0 " +
        " and (p.sharing.type = " + SHARED_PROJECT +
        " and " + HAVE_ACCESS_TO_PROJECT + "))) > 0" +
        " and (select count(*) from ExperimentTemplate ex join ex.rawFiles.data d join ex.project pr " +
        "where ex.isDeleted = false and pr.sharing.type = " + PUBLIC_PROJECT + " and d.fileMetaData = f) = 0")
    List<T> findAllShared(@Param("user") long user);

    @Query("select f from #{#entityName} f where f.isDeleted = false and " +
        " (select count(e.id) from ExperimentTemplate e join e.rawFiles.data d join e.project p" +
        " where e.isDeleted = false and f.id = d.fileMetaData.id and (" +
        "p.sharing.type = " + PUBLIC_PROJECT + ")) > 0")
    List<T> findAllPublic();


    @Query(
        "select f from FileMetaDataTemplate f join f.instrument instrument where f.isDeleted = false " +
            "and instrument.id = :instr and  (f.name like :query or f.labels like :query) and " + IS_FILE_AVAILABLE)
    Page<T> findByInstrument(@Param("instr") long instrument, @Param("user") Long user, Pageable pageable,
                             @Param("query") String s);

    @Query(
        "select f from FileMetaDataTemplate f where f.isDeleted = false and f.instrument.lab.id = :lab " +
            "and (f.name like :query or f.labels like :query) and " + IS_FILE_AVAILABLE)
    Page<T> findByLab(@Param("lab") long lab, @Param("user") Long user, Pageable pageable, @Param("query") String s);

    @Query("select f from ExperimentFileTemplate rf join rf.fileMetaData f " +
        "where  rf.experiment.id=:experiment and (f.name like :query or f.labels like :query)")
    Page<T> findByExperiment(@Param("experiment") long experiment, Pageable pageable, @Param("query") String s);

    @Query("select file.fileMetaData from ExperimentTemplate e join e.rawFiles.data file where e.id = :exp")
    List<T> findByExperiment(@Param("exp") long experimentId);


    @Query("select f from #{#entityName} f where f.instrument.id=:instrumentId")
    List<T> byInstrument(@Param("instrumentId") Long instrumentId);

    @Query("select f from #{#entityName} f " +
        " where f.instrument.id=:instrumentId " +
        " and " + IS_FILE_AVAILABLE)
    List<T> findAvailableByInstrument(@Param("user") long user, @Param("instrumentId") Long instrumentId);

    @Query(BOOLEAN_SELECT_STATEMENT + " where f.instrument.id=:instrumentId and f.name=:fileName and " +
        IS_FILE_AVAILABLE)
    boolean isFileAlreadyUploadedForInstrument(@Param("user") long user, @Param("instrumentId") long instrumentId,
                                               @Param("fileName") String fileName);

    @Query("select f from #{#entityName} f where f.instrument.id=:instrumentId and f.name=:fileName and " +
        IS_FILE_AVAILABLE)
    List<T> findByNameForInstrument(@Param("user") long user, @Param("instrumentId") long instrumentId,
                                    @Param("fileName") String fileName);


    @Query("select case when f is not null then count(f.id) else 0 end from #{#entityName} f join f.instrument i " +
        "join f.specie s where i.id = :instrument and f.invalid = false and " + IS_FILE_AVAILABLE + " group by f")
    Long countAvailableFilesByInstrument(@Param("user") long actor, @Param("instrument") long instrument);

    @Query("select f from #{#entityName} f where f.id in(:fileIds)")
    List<T> findAllByIds(@Param("fileIds") Iterable<Long> fileIds);

    @Query("select f from #{#entityName} f join f.instrument i " +
        "where f.contentId is null and f.owner.id = :user")
    List<T> unfinishedByUser(@Param("user") long actor);

    @Query("select count(f) from #{#entityName} f where f.instrument.id=:instrument")
    long countByInstrument(@Param("instrument") long instrument);

    @Query("select new com.infoclinika.mssharing.platform.repository.FileProjectUsage(p.id, 0l, p.sharing.type) from " +
            "ExperimentTemplate e join e.rawFiles.data d join e.project p where :file = d.fileMetaData.id ")
    List<FileProjectUsage> findFileProjectUsage(@Param("file") long fileId);

    @Query("select new com.infoclinika.mssharing.platform.repository.FileExperimentUsage(e.id, 0l, p.sharing.type) " +
        "from ExperimentTemplate e join e.rawFiles.data d join e.project p where :file = d.fileMetaData.id ")
    List<FileExperimentUsage> findFileExperimentUsage(@Param("file") long fileId);

    @Query("select f from #{#entityName} f join f.instrument i join f.specie s join f.instrument.lab l " +
        "where i.model.id = :model and " +
        CHECK_SAME_SPECIE + " and (cast(:lab as integer) = 0 or l.id = :lab) and " +
        "f.invalid = false and " + IS_FILE_AVAILABLE + " order by f.name")
    List<T> availableFilesByInstrumentModel(@Param("user") long actor, @Param("model") long model,
                                            @Param("specie") long specie, @Param("lab") long lab);

    @Query("select f from #{#entityName} f join f.instrument i join f.specie s " +
        "where i.id = :instrument and " +
        CHECK_SAME_SPECIE + " and f.invalid = false and " + IS_FILE_AVAILABLE + " order by f.name")
    List<T> availableFilesByInstrumentAndSpecie(@Param("user") long actor, @Param("specie") long specie,
                                                @Param("instrument") long instrument);

    /* Conditional queries for validation */

    @Query(BOOLEAN_SELECT_STATEMENT + " where f.id=:file and " + IS_FILE_AVAILABLE)
    boolean isUserCanReadFile(@Param("user") long user, @Param("file") long file);

    /* For Search purposes */

    @Query(QueryTemplates.Search.Files.COUNT_ALL_AVAILABLE_FILES_WITH_QUERY)
    long searchFilesCount(@Param("user") long user, @Param("query") String query);

    @Query(QueryTemplates.Search.Files.ALL_AVAILABLE_FILES_WITH_QUERY)
    List<T> searchFiles(@Param("user") long user, @Param("query") String query);

    @Query(QueryTemplates.Search.Files.ALL_AVAILABLE_FILES_WITH_QUERY)
    Page<T> searchPagedFiles(@Param("user") long user, @Param("query") String query, Pageable pageable);

    @Query("select f from #{#entityName} f where f.name = :name")
    T findByName(@Param("name") String name);

}
