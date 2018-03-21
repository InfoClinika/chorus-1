package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.FileDownloadJob;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Elena Kurilina
 */
public interface FileDownloadJobRepository extends JpaRepository<FileDownloadJob, Long> {

    @Query("select u from FileDownloadJob u where u.fileMetaData = :meta")
    @Nullable
    FileDownloadJob findByMetaData(@Param("meta") ActiveFileMetaData metaData);

    @Query("select u from FileDownloadJob  u where  u.fileMetaData.id = :metaId")
    FileDownloadJob findByFileMetaDataId(@Param("metaId") long metaId);

    @Query("select u from FileDownloadJob u join u.fileMetaData f join f.instrument i join i.lab l where f.storageData.storageStatus " +
            "= com.infoclinika.mssharing.model.internal.entity.restorable.StorageData$Status.UNARCHIVING_REQUESTED")
    List<FileDownloadJob> findNotCompleted();
}
