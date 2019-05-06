package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.UserProjectAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
@Repository
public interface UserProjectAccessRepository extends JpaRepository<UserProjectAccess, Long> {

    @Query("select upa from UserProjectAccess upa where upa.project.id=:projectId")
    List<UserProjectAccess> findByProjectId(@Param("projectId") Long projectId);

    @Modifying
    @Transactional
    @Query("delete from UserProjectAccess upa where upa.project.id=:projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
