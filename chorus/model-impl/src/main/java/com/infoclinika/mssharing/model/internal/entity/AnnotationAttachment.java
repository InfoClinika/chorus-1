package com.infoclinika.mssharing.model.internal.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * @author andrii.loboda
 */
@Entity
public class AnnotationAttachment extends AbstractAggregate {
    @ManyToOne(optional = false)
    private User owner;
    private String name;
    @Basic(optional = false)
    private Date uploadDate;
    @Basic(optional = true)
    private long sizeInBytes;

    AnnotationAttachment() {
    }


    public AnnotationAttachment(User owner, String name, Date uploadDate, long sizeInBytes) {
        this.owner = owner;
        this.name = name;
        this.uploadDate = uploadDate;
        this.sizeInBytes = sizeInBytes;
    }

    public User getOwner() {
        return owner;
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
