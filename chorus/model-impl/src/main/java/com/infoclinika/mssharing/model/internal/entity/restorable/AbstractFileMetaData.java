package com.infoclinika.mssharing.model.internal.entity.restorable;

import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import org.hibernate.annotations.Index;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Elena Kurilina
 */
@Entity(name = "FileMetaData")
public abstract class AbstractFileMetaData extends FileMetaDataTemplate<User, Instrument> {
    // on amazon glacier vault
    @Basic(optional = true)
    @Index(name = "ARCHIVE_ID_IDX")
    private String archiveId;
    @Basic(optional = false)
    private Date lastAccess;
    @Basic(optional = false)
    private boolean archive = false;
    @Basic
    private Date lastPingDate;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private FileMetaAnnotations metaInfo;

    @Embedded
    private StorageData storageData = new StorageData();

    @ManyToOne
    @JoinColumn(name = "bill_lab")
    private Lab billLab;

    @Basic(optional = false)
    private boolean sizeIsConsistent = false;

    public StorageData getStorageData() {
        //Sometimes storageData field is null due to bug in hibernate
        if (storageData == null) {
            storageData = new StorageData();
        }
        return storageData;
    }

    protected void setStorageData(StorageData storageData) {
        this.storageData = storageData;
    }

    public AbstractFileMetaData() {
    }

    public AbstractFileMetaData(User owner, String name, Date uploadDate, Instrument instrument, long sizeInBytes, String labels, Species specie, boolean archive) {
        setOwner(owner);
        setName(name);
        setUploadDate(uploadDate);
        this.lastAccess = uploadDate;
        setInstrument(instrument);
        setLabels(labels);
        setSizeInBytes(sizeInBytes);
        this.specie = specie;
        this.archive = archive;
    }

    public AbstractFileMetaData(Long id) {
        setId(id);
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public Date getLastPingDate() {
        return lastPingDate;
    }

    public void setLastPingDate(Date lastPingDate) {
        this.lastPingDate = lastPingDate;
    }

    public FileMetaAnnotations getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(FileMetaAnnotations metaInfo) {
        this.metaInfo = metaInfo;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    public boolean isSizeConsistent() {
        return sizeIsConsistent;
    }

    public void setSizeIsConsistent(boolean sizeIsConsistent) {
        this.sizeIsConsistent = sizeIsConsistent;
    }

    @Override
    public FileMetaDataTemplate copy(String copyName, UserTemplate owner){
        ActiveFileMetaData copy = new ActiveFileMetaData((User) owner, copyName, this.getUploadDate(), this.getInstrument(), this.getSizeInBytes(), this.getLabels(), this.getSpecie(), this.isArchive());
        copy.setCopy(true);
        copy.setContentId(this.getContentId());
        copy.setArchiveId(this.getArchiveId());
        copy.setDestinationPath(this.getDestinationPath());
        copy.setStorageData(this.getStorageData());
        copy.setSizeInBytes(this.getSizeInBytes());
        return copy;
    }

    @Nullable
    public Lab getBillLab() {
        return billLab;
    }

    public void setBillLab(@Nullable Lab billLab) {
        this.billLab = billLab;
    }

    @PreUpdate
    void preUpdate() {
        setLastAccess(new Date());
    }
}
