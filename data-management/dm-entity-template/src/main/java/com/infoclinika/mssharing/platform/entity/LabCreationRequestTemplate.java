/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.Date;

/**
 * @author Oleksii Tymchenko
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class LabCreationRequestTemplate extends AbstractAggregate {

    @Basic(optional = false)
    private String labName;

    @Basic(optional = false)
    private String institutionUrl;
    private PersonData headData;
    private String contactEmail;
    private Date requestDate;

    protected LabCreationRequestTemplate() {
    }

    public LabCreationRequestTemplate(final String labName,
                                      final String institutionUrl,
                                      final PersonData headData,
                                      final String contactEmail,
                                      final Date requestDate) {
        this.labName = labName;
        this.institutionUrl = institutionUrl;
        this.headData = headData;
        this.contactEmail = contactEmail;
        this.requestDate = requestDate;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getInstitutionUrl() {
        return institutionUrl;
    }

    public void setInstitutionUrl(String institutionUrl) {
        this.institutionUrl = institutionUrl;
    }

    public PersonData getHeadData() {
        return headData;
    }

    public void setHeadData(PersonData headData) {
        this.headData = headData;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }
}
