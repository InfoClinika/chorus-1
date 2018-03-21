/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity;

import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.model.internal.entity.User;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author andrii.loboda
 */
@Entity
@Table(name = "s_SearchDatabase")
public class ProteinDatabase extends AbstractPersistable<Long> {
    public static final String PROTEIN_SEQUENCE_COLUMN = "Protein Sequence";
    public static final String PROTEIN_DESCR_COLUMN = "Protein Description";

    @Basic(optional = false)
    private String name;

    @Basic
    private String contentId;

    @Basic(optional = false)
    private Date uploadDate;

    @ManyToOne(optional = false)
    private Species specie;

    @Basic(optional = true)
    private long sizeInBytes;

    @ManyToOne(optional = false)
    private User user;

    @Basic(optional = false)
    private boolean bPublic;

    @Basic(optional = false)
    private int proteinsInDatabaseCount;

    @Basic
    private String processedRef;
    @Basic
    private String processedLocalFile;
    @Basic(optional = false)
    private boolean reversed;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private ProteinDatabaseStatus status = ProteinDatabaseStatus.NOT_PERSISTED;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private ExperimentCategory category;

    ProteinDatabase() {
    }

    public ProteinDatabase(String name,  Species specie, Date uploadDate, long sizeInBytes, boolean bPublic, User user, boolean reversed, ExperimentCategory category) {
        this.name = name;
        this.specie = specie;
        this.uploadDate = uploadDate;
        this.sizeInBytes = sizeInBytes;
        this.bPublic = bPublic;
        this.user = user;
        this.reversed = reversed;
        this.category = category;
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

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public Species getSpecie() {
        return specie;
    }

    public void setSpecie(Species specie) {
        this.specie = specie;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isbPublic() {
        return bPublic;
    }

    public void setbPublic(boolean bPublic) {
        this.bPublic = bPublic;
    }

    public String getProcessedRef() {
        return processedRef;
    }

    public void setProcessedRef(String processedRef) {
        this.processedRef = processedRef;
    }

    public String getProcessedLocalFile() {
        return processedLocalFile;
    }

    public void setProcessedLocalFile(String processedLocalFile) {
        this.processedLocalFile = processedLocalFile;
    }

    public int getProteinsInDatabaseCount() {
        return proteinsInDatabaseCount;
    }

    public void setProteinsInDatabaseCount(int proteinsInDatabaseCount) {
        this.proteinsInDatabaseCount = proteinsInDatabaseCount;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public ProteinDatabaseStatus getStatus() {
        return status;
    }

    public void setStatus(ProteinDatabaseStatus status) {
        this.status = status;
    }

    public ExperimentCategory getCategory() {
        return category;
    }

    public void setCategory(ExperimentCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", name)
                .add("specie", specie)
                .add("sizeInBytes", sizeInBytes)
                .add("status", status)
                .add("user", user)
                .add("bPublic", bPublic)
                .add("proteinsInDatabaseCount", proteinsInDatabaseCount)
                .add("processedRef", processedRef)
                .add("processedLocalFile", processedLocalFile)
                .add("reversed", reversed)
                .toString();
    }

    public enum ProteinDatabaseStatus{
        NOT_PERSISTED, IN_PROGRESS, PERSISTED, NEED_TO_RE_PERSIST, FAILED;
    }
}
