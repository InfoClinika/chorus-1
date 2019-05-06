package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Oleksii Tymchenko, Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Attachment<U extends UserTemplate<?>> extends AbstractPersistable {

    @ManyToOne(optional = false, targetEntity = UserTemplate.class)
    private U owner;

    private String name;
    @Basic(optional = false)
    private Date uploadDate;
    @Basic
    private long sizeInBytes;

    public Attachment() {
    }

    public U getOwner() {
        return owner;
    }

    public void setOwner(U owner) {
        this.owner = owner;
    }

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

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
}
