package com.infoclinika.mssharing.platform.entity.restorable;

import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class FileMetaDataTemplate<U extends UserTemplate<?>, I extends InstrumentTemplate<U, ?>>
    extends AbstractRestorable {

    @ManyToOne
    protected Species specie;
    @ManyToOne(optional = false)
    private I instrument;
    @ManyToOne
    private U owner;
    @Index(name = "FILE_NAME_IDX")
    @Basic(optional = false)
    private String name;
    @Basic(optional = false)
    private Date uploadDate;
    @Basic
    private String bucket;
    @Basic
    private String contentId;
    @Basic
    private boolean readOnly;
    @Basic
    private long sizeInBytes;
    private String labels;
    //Upload ID at Amazon S3
    private String uploadId;
    //Destination path of _unfinished_ upload at Amazon S3
    private String destinationPath;
    private boolean copy;
    @Basic(optional = false)
    private boolean invalid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public U getOwner() {
        return owner;
    }

    public void setOwner(U owner) {
        this.owner = owner;
    }

    public boolean isCopy() {
        return copy;
    }

    public void setCopy(boolean copy) {
        this.copy = copy;
    }

    public I getInstrument() {
        return instrument;
    }

    public void setInstrument(I instrument) {
        this.instrument = instrument;
    }

    public Species getSpecie() {
        return specie;
    }

    public void setSpecie(Species specie) {
        this.specie = specie;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public abstract FileMetaDataTemplate copy(String copyName, UserTemplate owner);
}
