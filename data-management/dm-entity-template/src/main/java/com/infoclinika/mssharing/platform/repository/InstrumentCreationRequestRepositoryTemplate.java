package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.InstrumentCreationRequestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
public interface InstrumentCreationRequestRepositoryTemplate<T extends InstrumentCreationRequestTemplate>
    extends JpaRepository<T, Long> {

    @Query("select r from #{#entityName} r join r.lab l join l.labMemberships lm " +
        "where lm.head=true and lm.user.id=:user")
    List<T> findHeadId(@Param("user") long user);
}
