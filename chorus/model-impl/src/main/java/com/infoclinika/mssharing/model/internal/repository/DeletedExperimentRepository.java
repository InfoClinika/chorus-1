package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedExperiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author Elena Kurilina
 */
public interface DeletedExperimentRepository extends JpaRepository<DeletedExperiment, Long> {
    @Query("select e from DeletedExperiment e where e.project.id = :project")
    List<DeletedExperiment> findByProject(@Param("project") long project);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name, " +
            "'experiment',  (case when (l is null) then 'No Lab' else l.name end))" +
            " from DeletedExperiment e inner join e.project p left join e.lab l where e.creator.id = :owner AND " +
            "(l is null OR  l in (SELECT lm.lab FROM e.creator.labMemberships lm where lm.user.id = :owner)) AND p.deletionDate is null")
    List<DeletedItem> findByOwner(@Param("owner") long owner);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name, " +
            "'experiment', e.lab.name) " +
            "from DeletedExperiment e inner join e.project p where p.deletionDate is null")
    List<DeletedItem> findAllDeleted();

    @Query("select e from DeletedExperiment e join e.rawFiles.data file where file.fileMetaData.id=:metaFile")
    List<DeletedExperiment> findByFile(@Param("metaFile") long fileMetaData);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name, " +
            "'experiment', l.name)" +
            " from DeletedExperiment e inner join e.project p left join e.lab l " +
            " where p.deletionDate is null and l.id in (:labs)")
    List<DeletedItem> findByLabs(@Param("labs") Collection<Long> labs);

    @Query("select e from DeletedExperiment e where e.creator.id  = :owner AND e.lab.id = :lab")
    List<DeletedExperiment> findByOwnerAndLab(@Param("owner") long owner, @Param("lab") long lab);
}
