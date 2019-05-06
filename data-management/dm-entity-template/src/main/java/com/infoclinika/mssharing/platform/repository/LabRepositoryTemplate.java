package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.LabTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
public interface LabRepositoryTemplate<LAB extends LabTemplate> extends JpaRepository<LAB, Long> {

    @Query("select m.lab from UserLabMembership m where m.user.personData.email = :email and m.head = true")
    List<LAB> findByHeadEmail(@Param("email") String email);

    @Query("select l from #{#entityName} l where l.name= :labName")
    LAB findByName(@Param("labName") String labName);

    @Query("select count(*) from UserLabMembership m where m.lab.id = :labId")
    long membersCount(@Param("labId") long labId);

    @Query("select l from #{#entityName} l join l.labMemberships lm where lm.user.id=:user ")
    List<LAB> findForUser(@Param("user") long user);
}
