package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.ProjectSharingRequestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
public interface ProjectSharingRequestRepositoryTemplate<T extends ProjectSharingRequestTemplate> extends JpaRepository<T, Long> {
    @Query("select r from #{#entityName} r " +
            "where r.projectId = :projectId")
    List<T> findByProject(@Param("projectId") long projectId);

    @Query("select r from #{#entityName} r " +
            "where r.requesterId = :requester AND r.projectId = :projectId")
    T findByRequesterAndProject(@Param("requester") long requester, @Param("projectId") long projectId);

    @Query("select r from #{#entityName} r join r.downloadExperimentLinks l " +
            "where r.requesterId = :requester AND l = :link")
    T findByRequesterAndExperimentLink(@Param("requester") long requester, @Param("link") String link);

    @Query("select r from ProjectSharingRequestTemplate r " +
            "where (select count(*) from ProjectTemplate p where p.creator.id = :creator and p.id = r.projectId) > 0 ")
    List<T> findByProjectCreator(@Param("creator") long creator);
}
