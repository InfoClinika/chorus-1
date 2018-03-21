/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.platform.entity.LabTemplate;

import javax.persistence.*;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Stanislav Kurilin
 */
@Entity
public class Lab extends LabTemplate<User> {
    @Basic(optional = false)
    private boolean isFake;

    @Basic(optional = false)
    private float uploadLimitInGb = 10000;


    public Lab() {
    }

    public Lab(String name, String institutionUrl, User head, String contactEmail) {
        setName(name);
        setInstitutionUrl(institutionUrl);
        setContactEmail(contactEmail);
        setHead(head);
    }

    public Lab(long id) {
        setId(id);
    }

    public boolean isFake() {
        return isFake;
    }

    public void setFake(boolean fake) {
        isFake = fake;
    }

    public float getUploadLimitInGb() {
        return uploadLimitInGb;
    }

    public void setUploadLimitInGb(float uploadLimitInGb) {
        this.uploadLimitInGb = uploadLimitInGb;
    }


}
