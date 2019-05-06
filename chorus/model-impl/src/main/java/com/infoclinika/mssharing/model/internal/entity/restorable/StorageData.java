package com.infoclinika.mssharing.model.internal.entity.restorable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Embeddable
public class StorageData {

    @Enumerated
    @Column(name = "storage_status")
    private Status storageStatus = Status.UNARCHIVED;

    @Column(name = "to_archive")
    private boolean toArchive = false;

    @Column(name = "last_unarchive_timestamp")
    private Date lastUnarchiveTimestamp;

    //TODO: Merge with StorageData.Status enum
    private boolean archivedDownloadOnly = false;
    private Boolean archivedDownloadCharged;

    public Status getStorageStatus() {
        return storageStatus;
    }

    public void setStorageStatus(Status status) {
        this.storageStatus = status;
    }

    public boolean isToArchive() {
        return toArchive;
    }

    public void setToArchive(boolean toArchive) {
        this.toArchive = toArchive;
    }

    public Date getLastUnarchiveTimestamp() {
        return lastUnarchiveTimestamp;
    }

    public void setLastUnarchiveTimestamp(Date lastUnarchiveTimestamp) {
        this.lastUnarchiveTimestamp = lastUnarchiveTimestamp;
    }

    public boolean isArchivedDownloadOnly() {
        return archivedDownloadOnly;
    }

    public void setArchivedDownloadOnly(boolean downloadOnly) {
        this.archivedDownloadOnly = downloadOnly;
    }

    public Boolean isArchivedDownloadCharged() {
        return archivedDownloadCharged;
    }

    public void setArchivedDownloadCharged(Boolean archivedDownloadCharged) {
        this.archivedDownloadCharged = archivedDownloadCharged;
    }

    public enum Status {
        ARCHIVING_REQUESTED,
        ARCHIVED,
        UNARCHIVED,
        UNARCHIVING_REQUESTED
    }
}
