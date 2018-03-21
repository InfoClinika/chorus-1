package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
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
public interface ExperimentRepositoryTemplate<E extends ExperimentTemplate> extends JpaRepository<E, Long> {

    @Query("select e from #{#entityName} e where e.creator.id = :userId and e.name = :name")
    E findOneByName(@Param("userId") long userId, @Param("name") String experimentName);

    @Query("select e from #{#entityName} e where e.downloadToken is not null and e.downloadToken = :token")
    E findOneByToken(@Param("token") String token);

    /*------------------------------- List methods --------------------------------------------------------------------*/
    @Query("select distinct e" +
            " from #{#entityName} e join e.project p" +
            " where ( e.creator.id = :user or " +
            "p.creator.id = :user " +
            " or p.sharing.type = " + PUBLIC_PROJECT +
            " or " + HAVE_ACCESS_TO_PROJECT + ")")
    List<E> findAllAvailable(@Param("user") long user);

    @Query("select distinct e" +
            " from #{#entityName} e join e.project p " +
            "  where (e.creator.id <> :user " +
            " and p.sharing.type = " + SHARED_PROJECT +
            " and (p.creator.id = :user or " + HAVE_ACCESS_TO_PROJECT + "))")
    List<E> findShared(@Param("user") long user);

    @Query("select distinct e from #{#entityName} e join e.project p join fetch e.rawFiles.data d where (p.sharing.type = " + PUBLIC_PROJECT + ")")
    List<E> findPublic();

    @Query("select distinct e from #{#entityName} e where e.creator.id = :user")
    List<E> findOwned(@Param("user") long user);

    @Query("select distinct e from #{#entityName} e join fetch e.rawFiles.data d where e.creator.id = :user")
    List<E> findMy(@Param("user") long user);

    @Query("select distinct e from #{#entityName} e join fetch e.rawFiles.data d where e.project.id = :project")
    List<E> findByProject(@Param("project") long project);

    @Query("select distinct e from #{#entityName} e join e.project p where p.id = :project and p.creator.id=e.creator.id")
    List<E> findCreatedByProjectCreator(@Param("project") long project);

    /*--------------------------------- Paged ------------------------------------------------------------------------*/

    @Query("select distinct e  from #{#entityName} e join e.project p" +
            " where e.name like :s and ( e.creator.id = :user or " +
            "p.creator.id = :user " +
            " or p.sharing.type = " + PUBLIC_PROJECT +
            " or " + HAVE_ACCESS_TO_PROJECT + ")")
    Page<E> findAllAvailable(@Param("user") long user, @Param("s") String filterQuery, Pageable request);

    @Query("select distinct e" +
            " from #{#entityName} e join e.project p " +
            "  where e.name like :s \n" +
            " and (e.creator.id <> :user " +
            " and p.sharing.type = " + SHARED_PROJECT +
            " and (p.creator.id = :user or " + HAVE_ACCESS_TO_PROJECT + "))")
    Page<E> findShared(@Param("user") long user, @Param("s") String filterQuery, Pageable request);

    @Query("select distinct e from #{#entityName} e where e.name like :s and e.creator.id = :user")
    Page<E> findMy(@Param("user") long user, @Param("s") String filterQuery, Pageable request);

    @Query("select distinct e from #{#entityName} e join e.project p where e.name like :s and (p.sharing.type = " + PUBLIC_PROJECT + ")")
    Page<E> findPublic(@Param("s") String filterQuery, Pageable request);

    @Query("select distinct e from #{#entityName} e join e.project p join e.lab l where l.id=:lab and e.name like :s")
    Page<E> findAllByLab(@Param("lab") long lab, @Param("s") String filterQuery, Pageable request);

    @Query("select distinct e from #{#entityName} e where e.project.id = :project and e.name like :query")
    Page<E> findByProject(@Param("project") long project, @Param("query") String query, Pageable request);

    /**
     * Validation helper methods
     */

    @Query("select coalesce(count(e.id), 0l) from #{#entityName} e join e.rawFiles.data file where file.fileMetaData.id=:file")
    long countByFile(@Param("file") long file);

    @Query("select coalesce(count(e.id), 0l) from #{#entityName} e where e.project.id = :project")
    long countByProject(@Param("project") long project);

        /* For Search purposes */

    @Query(Search.Experiment.COUNT_ALL_AVAILABLE_EXPERIMENTS_WITH_QUERY)
    long searchExperimentsCount(@Param("user") long user, @Param("query") String query);

    @Query(Search.Experiment.ALL_AVAILABLE_EXPERIMENTS_WITH_QUERY)
    List<E> searchExperiments(@Param("user") long user, @Param("query") String query);

    @Query(Search.Experiment.ALL_AVAILABLE_EXPERIMENTS_WITH_QUERY)
    Page<E> searchPagedExperiments(@Param("user") long user, @Param("query") String query, Pageable pageable);

}
