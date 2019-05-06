package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.GroupTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface GroupRepositoryTemplate<G extends GroupTemplate> extends JpaRepository<G, Long> {

    @Query("select g from #{#entityName} g " +
        "where owner.id = :owner or (:includeAllUsers = true and includesAllUsers = true)")
    List<G> findByOwner(@Param("owner") long owner, @Param("includeAllUsers") boolean includeAllUsers);

    @Query("select g from #{#entityName} g where g.name = :name")
    G findOneByName(@Param("name") String name);

    @Query("select g from #{#entityName} g where includesAllUsers = true")
    G findAllUsersGroup();


}
