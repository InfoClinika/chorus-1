/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity.view;

import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractProject;
import com.infoclinika.mssharing.model.write.ExperimentCategory;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oleksii Tymchenko
 *
 * View for mapping ActiveExperiment records in dashboard. Read only.
 * See classpath*: views.sql
 */
@javax.persistence.Table(name = "experiment_dashboard_record")
@Entity
public class ExperimentDashboardRecord {

    @Id
    private long id;
    private String name;
    private String description;
    @ManyToOne
    @JoinColumn(name = "lab_id")
    private Lab lab;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_lab_id")
    private Lab billLab;
    @ManyToOne
    @JoinColumn(name = "project_id")
    private AbstractProject project;
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
    private long numberOfFiles;
    private int analyzesCount;
    private Date lastModification;
    private boolean deleted;
    private String downloadToken;
    private String labName;
    @Transient
    private List<Long> processedIds = new LinkedList<Long>();

    @Enumerated(EnumType.STRING)
    private ExperimentCategory experimentCategory;


    public ExperimentDashboardRecord(Long id, String name, Lab lab, AbstractProject project, User creator, Long numberOfFiles,
                                     Date lastModification, String downloadToken, boolean deleted) {
        this.id = id;
        this.name = name;
        this.lab = lab;
        this.project = project;
        this.creator = creator;
        this.numberOfFiles = numberOfFiles;
        this.lastModification = lastModification;
        this.downloadToken = downloadToken;
        this.deleted = deleted;
    }

    public ExperimentDashboardRecord(Long id, String name, Lab lab, AbstractProject project, User creator, Long numberOfFiles,
                                     Date lastModification, String downloadToken,
                                     ExperimentCategory experimentCategory) {
        this.id = id;
        this.name = name;
        this.lab = lab;
        this.project = project;
        this.creator = creator;
        this.numberOfFiles = numberOfFiles;
        this.lastModification = lastModification;
        this.downloadToken = downloadToken;
        this.experimentCategory = experimentCategory;
    }


    public void updateProcessedIds(Collection<Long> ids) {
        processedIds.clear();
        processedIds.addAll(ids);
    }

    public ExperimentDashboardRecord() {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Lab getLab() {
        return lab;
    }

    public Lab getBillLab() {
        return billLab;
    }

    public AbstractProject getProject() {
        return project;
    }

    public User getCreator() {
        return creator;
    }

    public long getNumberOfFiles() {
        return numberOfFiles;
    }

    public int getAnalyzesCount() {
        return analyzesCount;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public String getDownloadToken() {
        return downloadToken;
    }

    public String getLabName() {
        return labName;
    }

    public List<Long> getProcessedIds() {
        return processedIds;
    }

    public ExperimentCategory getExperimentCategory() {
        return experimentCategory;
    }

    public void setExperimentCategory(ExperimentCategory experimentCategory) {
        this.experimentCategory = experimentCategory;
    }
}
