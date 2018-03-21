/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Stanislav Kurilin
 */
@Entity
public class InstrumentModel extends Dictionary {

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    private Vendor vendor;

    @JoinTable(
            name = "InstrumentModel_vendor_extension",
            joinColumns = @JoinColumn(name = "InstrumentModel_id"),
            inverseJoinColumns = @JoinColumn(name = "extensions_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"InstrumentModel_id", "extensions_id"})
    )
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Set<VendorExtension> extensions = newHashSet();

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    private InstrumentType type;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    private InstrumentStudyType studyType;

    private boolean additionalFiles;
    private boolean folderArchiveSupport;

    public InstrumentModel() {
    }

    public InstrumentModel(long id) {
        setId(id);
    }

    public InstrumentModel(String name, Vendor vendor, InstrumentType type, InstrumentStudyType studyType) {
        super(name);
        this.vendor = vendor;
        this.type = type;
        this.studyType = studyType;
    }

    public InstrumentModel(String name, Vendor vendor, InstrumentType type, InstrumentStudyType studyType, Set<VendorExtension> extensions) {
        super(name);
        this.vendor = vendor;
        this.type = type;
        this.studyType = studyType;
        this.extensions = extensions;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public InstrumentModel setVendor(Vendor vendor) {
        this.vendor = vendor;
        return this;
    }

    public Set<VendorExtension> getExtensions() {
        return extensions;
    }

    public InstrumentModel setExtensions(Set<VendorExtension> extensions) {
        this.extensions = extensions;
        return this;
    }

    public InstrumentType getType() {
        return type;
    }

    public InstrumentModel setType(InstrumentType type) {
        this.type = type;
        return this;
    }

    public InstrumentStudyType getStudyType() {
        return studyType;
    }

    public InstrumentModel setStudyType(InstrumentStudyType studyType) {
        this.studyType = studyType;
        return this;
    }

    public boolean isAdditionalFiles() {
        return additionalFiles;
    }

    public InstrumentModel setAdditionalFiles(boolean additionalFiles) {
        this.additionalFiles = additionalFiles;
        return this;
    }

    public boolean isFolderArchiveSupport() {
        return folderArchiveSupport;
    }

    public InstrumentModel setFolderArchiveSupport(boolean folderArchiveSupport) {
        this.folderArchiveSupport = folderArchiveSupport;
        return this;
    }
}
