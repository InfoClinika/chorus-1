package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
public interface UserLabMembershipRequestRepositoryTemplate<T extends UserLabMembershipRequestTemplate> extends JpaRepository<T, Long> {

    @Query("select r from #{#entityName} r where r.lab.id = :labId")
    List<T> findByLab(@Param("labId") long labId);

    @Query("select r from #{#entityName} r where r.user.id = :userId and decision is null")
    List<T> findPendingByUser(@Param("userId") long userId);

    @Query("select r from #{#entityName} r join r.lab l join l.labMemberships lm where lm.head = true and lm.user.id = :userId and decision is null ")
    List<T> findPendingForHead(@Param("userId") long userId);
}
