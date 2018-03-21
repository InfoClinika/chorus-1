package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
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
public interface ProjectRepositoryTemplate<T extends ProjectTemplate> extends JpaRepository<T, Long> {

    String BOOLEAN_SELECT_STATEMENT = "select case when count(p.id)>0 then true else false end from #{#entityName} p ";

    /**
     * Returns all projects that was shared with specified group
     */
    @Query("select p from #{#entityName} p join p.sharing.groupsOfCollaborators gc where :group = gc.group.id")
    List<T> findBySharedGroup(@Param("group") long group);


    @Query("select p from #{#entityName} p where p.creator.id  = :owner AND p.lab.id = :lab")
    List<T> findByOwnerAndLab(@Param("owner") long owner, @Param("lab") long lab);

    @Query("select p from #{#entityName} p where p.creator.id = :userId and p.name = :name")
    T findOneByName(@Param("userId") long userId, @Param("name") String projectName);

    @Query("select p from #{#entityName} p where p.creator.id = :userId and p.name = :name")
    List<T> findByName(@Param("userId") long userId, @Param("name") String projectName);

    @Query("select new com.infoclinika.mssharing.platform.repository.FileUsage(d.fileMetaData.id, e.project) " +
            "from ExperimentTemplate e join e.rawFiles.data d join e.project p where e.isDeleted = false ")
    List<FileUsage> whereFileIsUsed();

    @Query("select p from #{#entityName} p " +
            " where p.isDeleted=false " +
            " and (p.creator.id = :user" +
            " or p.sharing.type=" + PUBLIC_PROJECT +
            " or " + HAVE_ACCESS_TO_PROJECT + ")")
    List<T> findAllAvailable(@Param("user") long user);

    @Query("select p from #{#entityName} p where p.creator.id=:user and p.isDeleted=false")
    List<T> findMy(@Param("user") long user);

    @Query("select p from #{#entityName} p " +
            " where  p.isDeleted=false" +
            " and p.sharing.type = " + SHARED_PROJECT +
            " and p.creator.id <> :user " +
            " and " + HAVE_ACCESS_TO_PROJECT)
    List<T> findSharedNotOwned(@Param("user") long user);

    @Query("select p from #{#entityName} p where p.isDeleted=false " +
            " and p.creator.id<>:user" +
            " and p.sharing.type = " + PUBLIC_PROJECT)
    List<T> findPublicNotOwned(@Param("user") long user);

    @Query("select p from #{#entityName} p " +
            JOIN_LAB_MEMBERS +
            " where p.isDeleted=false " +
            " and (p.creator.id=:user " +
            " or " + IS_USER_LAB_HEAD +
            " or " + HAVE_WRITE_ACCESS_TO_PROJECT + ")")
    List<T> findAllowedForWriting(@Param("user") long user);

    /* Paged */

    @Query("select pr from ProjectTemplate pr " +
            " join pr.lab lab where lab is not null and lab.id=:lab AND pr.name like :s")
    Page<T> findByLabAndName(@Param("lab") long lab, @Param("s") String nameFilter, Pageable request);

    @Query("select p from ProjectTemplate p " +
            " left join p.lab lab " +
            " where  p.isDeleted=false" +
            " AND p.sharing.type = " + SHARED_PROJECT +
            " and p.name like :s " +
            " and p.creator.id <> :user " +
            " and " + HAVE_ACCESS_TO_PROJECT)
    Page<T> findSharedNotOwned(@Param("user") long user, @Param("s") String nameFilter, Pageable request);

    @Query("select p from ProjectTemplate p" +
            " left join p.lab lab " +
            " where p.creator.id=:user " +
            " and p.name like :s " +
            " and p.isDeleted=false")
    Page<T> findMy(@Param("user") long user, @Param("s") String nameFilter, Pageable request);

    @Query("select p from ProjectTemplate p " +
            " left join p.lab lab " +
            " where p.isDeleted=false" +
            " AND p.name like :s " +
            " and p.creator.id <> :user " +
            " and p.sharing.type = " + PUBLIC_PROJECT)
    Page<T> findPublicNotOwned(@Param("user") long user, @Param("s") String nameFilter, Pageable request);

    @Query("select p from ProjectTemplate p " +
            " left join p.lab lab " +
            " where p.isDeleted=false " +
            " AND p.name like :s" +
            " and (p.sharing.type = " + PUBLIC_PROJECT +
            " or p.creator.id = :user" +
            " or " + HAVE_ACCESS_TO_PROJECT + ")")
    Page<T> findAllAvailable(@Param("user") long user, @Param("s") String nameFilter, Pageable request);

    /* Validation queries */

    @Query(BOOLEAN_SELECT_STATEMENT +
            JOIN_LAB_MEMBERS +
            " where p.isDeleted=false " +
            " and p.id=:project " +
            " and (p.creator.id=:user " +
            " or " + IS_USER_LAB_HEAD +
            " or " + HAVE_WRITE_ACCESS_TO_PROJECT + ")")
    boolean isProjectAllowedForWriting(@Param("user") long user, @Param("project") long project);

    @Query(BOOLEAN_SELECT_STATEMENT +
            JOIN_LAB_MEMBERS +
            " where p.id=:project " +
            " and (p.creator.id=:user " +
            " or p.sharing.type=" + PUBLIC_PROJECT +
            " or " + IS_USER_LAB_HEAD +
            " or " + HAVE_ACCESS_TO_PROJECT + ")")
    boolean isUserCanReadProject(@Param("user") long user, @Param("project") long project);

    /* For Search purposes */

    @Query(Search.Projects.COUNT_ALL_AVAILABLE_PROJECTS_WITH_QUERY)
    long searchProjectsCount(@Param("user") long user, @Param("query") String query);

    @Query(Search.Projects.ALL_AVAILABLE_PROJECTS_WITH_QUERY)
    List<T> searchProjects(@Param("user") long user, @Param("query") String query);

    @Query(Search.Projects.ALL_AVAILABLE_PROJECTS_WITH_QUERY)
    Page<T> searchPagedProjects(@Param("user") long user, @Param("query") String query, Pageable pageable);

    @Query(Search.Projects.ALL_AVAILABLE_PROJECTS_WITH_QUERY_AND_ID)
    Page<T> searchPagedProjectsWithId(@Param("user") long user, @Param("query") String query, Pageable pageable);

}
