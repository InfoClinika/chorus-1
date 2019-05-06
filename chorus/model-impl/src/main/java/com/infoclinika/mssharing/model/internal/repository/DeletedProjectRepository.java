package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedProject;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author Elena Kurilina
 */
public interface DeletedProjectRepository extends ProjectRepositoryTemplate<DeletedProject> {

    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name, " +
            "'project', (case when (l is null) then 'No Lab' else l.name end)) " +
            " from DeletedProject e left join e.lab l  where e.creator.id = :owner AND " +
            " (l is null OR  l in (SELECT lm.lab FROM e.creator.labMemberships lm where lm.user.id = :owner))")
    List<DeletedItem> findByOwner(@Param("owner") long owner);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name, " +
            "'project', (case when (l is null) then 'No Lab' else l.name end)) " +
            " from DeletedProject e left join e.lab l where l is null OR l.id in (:labs)")
    List<DeletedItem> findByLabs(@Param("labs") Collection<Long> labs);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name, " +
            "'project', '') " +
            " from DeletedProject e")
    List<DeletedItem> findAllDeleted();

}
