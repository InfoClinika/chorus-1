/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller.request;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents project on create and update project screen.
 *
 * @author Oleksii Tymchenko
 */
public class ProjectOperationRequest {
    private long projectId;
    public Long lab;
    public String name;
    public String areaOfResearch = "";
    public String description = "";

    private final Map<Long, Boolean> colleagues = new HashMap<Long, Boolean>();
    private final Map<Long, Boolean> groups = new HashMap<Long, Boolean>();
    private boolean withEmailNotification;

    private boolean blogEnabled;

    public ProjectOperationRequest() {
    }

    public Map<Long, Boolean> getColleagues() {
        return colleagues;
    }

    public void setColleagues(Map<Long, Boolean> colleagues) {
        this.colleagues.clear();
        this.colleagues.putAll(colleagues);
    }

    public Map<Long, Boolean> getGroups() {
        return groups;
    }

    public void setGroups(Map<Long, Boolean> groups) {
        this.groups.clear();
        this.groups.putAll(groups);
    }

    public boolean isWithEmailNotification() {
        return withEmailNotification;
    }

    public void setWithEmailNotification(boolean withEmailNotification) {
        this.withEmailNotification = withEmailNotification;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAreaOfResearch() {
        return areaOfResearch;
    }

    public void setAreaOfResearch(String areaOfResearch) {
        this.areaOfResearch = areaOfResearch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBlogEnabled() {
        return blogEnabled;
    }

    public void setBlogEnabled(boolean blogEnabled) {
        this.blogEnabled = blogEnabled;
    }
}
