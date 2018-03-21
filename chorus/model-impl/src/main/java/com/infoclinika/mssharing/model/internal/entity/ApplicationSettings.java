/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Basic;
import javax.persistence.Entity;

/**
 * @author Elena Kurilina
 */
@Entity
public class ApplicationSettings extends AbstractPersistable<Long> {

    @Basic(optional = true)
    public long value;

    @Basic
    private String name;

    ApplicationSettings() {
    }

    public ApplicationSettings(long sizeInBytes, String name) {
        this.value = sizeInBytes;
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
