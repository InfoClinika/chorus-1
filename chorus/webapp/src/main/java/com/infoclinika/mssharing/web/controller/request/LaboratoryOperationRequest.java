/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */

package com.infoclinika.mssharing.web.controller.request;

/**
 * @author Oleksii Tymchenko
 */
/**
 * Represents lab on create and update lab screen.
 */

 public class LaboratoryOperationRequest {
    private long id;
    private String headFirstName = "";
    private String headLastName = "";
    private String headEmail = "";

    private String institutionUrl = "";
    private String name = "";
    private String contactEmail = "";
    private String comment = "";


    public LaboratoryOperationRequest(){
    }

    public String getHeadFirstName() {
        return headFirstName;
    }

    public void setHeadFirstName(String headFirstName) {
        this.headFirstName = headFirstName;
    }

    public String getHeadLastName() {
        return headLastName;
    }

    public void setHeadLastName(String headLastName) {
        this.headLastName = headLastName;
    }

    public String getHeadEmail() {
        return headEmail;
    }

    public void setHeadEmail(String headEmail) {
        this.headEmail = headEmail;
    }

    public String getInstitutionUrl() {
        return institutionUrl;
    }

    public void setInstitutionUrl(String institutionUrl) {
        this.institutionUrl = institutionUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

