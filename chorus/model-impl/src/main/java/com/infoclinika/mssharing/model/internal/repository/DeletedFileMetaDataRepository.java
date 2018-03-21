package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedFileMetaData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author Elena Kurilina
 */
public interface DeletedFileMetaDataRepository extends CrudRepository<DeletedFileMetaData, Long> {
    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name," +
            " 'file', e.instrument.lab.name) from DeletedFileMetaData e left join e.instrument.lab l " +
            " where e.owner.id = :owner AND " +
            " l in (SELECT lm.lab FROM e.owner.labMemberships lm where lm.user.id = :owner)")
    List<DeletedItem> findByOwner(@Param("owner") long owner);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name," +
            " 'file', e.instrument.lab.name) from DeletedFileMetaData e where e.instrument.lab.id in (:labs)")
    List<DeletedItem> findByLabs(@Param("labs") Collection<Long> labs);

    @Query("select new com.infoclinika.mssharing.model.internal.repository.DeletedItem(e.id, e.deletionDate, e.name, " +
            "'file', e.instrument.lab.name) " +
            " from DeletedFileMetaData e")
    List<DeletedItem> findAllDeleted();

    @Query("select f from DeletedFileMetaData f where f.instrument.id=:instrument")
    List<DeletedFileMetaData> byInstrument(@Param("instrument") long instrument);

    @Query("select f from DeletedFileMetaData f where f.owner.id = :owner AND f.instrument.lab.id = :lab")
    List<DeletedFileMetaData> findByOwnerAndLab(@Param("owner") long owner, @Param("lab") long lab);
}
