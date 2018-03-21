package com.infoclinika.mssharing.model.internal.entity.restorable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@Entity
public class DeletedFileMetaData extends AbstractFileMetaData {

    @Basic
    private Date deletionDate = new Date();

    protected DeletedFileMetaData() {
    }
    public DeletedFileMetaData(ActiveFileMetaData f) {
        super(f.getOwner(), f.getName(), f.getUploadDate(), f.getInstrument(), f.getSizeInBytes(), f.getLabels(),
                f.getSpecie(), f.isArchive());
        this.setArchiveId(f.getArchiveId());
        this.setContentId(f.getContentId());
        this.setCopy(f.isCopy());
        this.setDestinationPath(f.getDestinationPath());
        this.setInvalid(f.isInvalid());
        this.setLastAccess(f.getLastAccess());
        this.setLastPingDate(f.getLastPingDate());
        this.setMetaInfo(f.getMetaInfo());
        this.setUploadId(f.getUploadId());
        this.setStorageData(f.getStorageData());
        setDeleted(true);
    }

}
