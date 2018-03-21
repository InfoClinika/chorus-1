package com.infoclinika.mssharing.model.internal.entity;



import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.StorageData;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

/**
 * @author Elena Kurilina
 */

@Entity
@Table(name = "file_download_job")
public class FileDownloadJob extends AbstractPersistable<Long> {

    @Column
    public String listener = null;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(unique = true, name = "file_id")
    public ActiveFileMetaData fileMetaData;

    public FileDownloadJob(ActiveFileMetaData fileMetaData) {
        this.fileMetaData = fileMetaData;
    }

    public FileDownloadJob() {
    }

    @Transient
    public boolean isCompleted() {
        return fileMetaData.getStorageData().getStorageStatus().equals(StorageData.Status.UNARCHIVED);
    }
}
