package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.FileDownloadJob;
import com.infoclinika.mssharing.model.internal.entity.FilesDownloadGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @author Elena Kurilina
 */
public interface FileDownloadGroupRepository extends JpaRepository<FilesDownloadGroup, Long> {

    @Query("select distinct u from FilesDownloadGroup u join u.jobs j where j in(:job) ")
    List<FilesDownloadGroup> findByJob(@Param("job") Collection<FileDownloadJob> job);

    @Query("select distinct u from FilesDownloadGroup u join u.jobs j where j = :job ")
    List<FilesDownloadGroup> findByJob(@Param("job") FileDownloadJob job);

    @Query("select (count(u) > 0) from FilesDownloadGroup u join u.jobs j where j = :job and u != :group")
    boolean isOtherGroupsPresentsForJob(@Param("job") FileDownloadJob job, @Param("group") FilesDownloadGroup group);

    @Query("select u from FilesDownloadGroup u where u.experimentId = :experimentId")
    @Nullable
    FilesDownloadGroup findByExperiment(@Param("experimentId") Long experimentId);

}
