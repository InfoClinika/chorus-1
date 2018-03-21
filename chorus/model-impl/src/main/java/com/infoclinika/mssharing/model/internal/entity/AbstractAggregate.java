/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity;

import org.hibernate.search.annotations.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Basic;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author Stanislav Kurilin
 */
@MappedSuperclass
public abstract class AbstractAggregate extends AbstractPersistable<Long> {
    @Basic(optional = false)
    @Field(analyze = Analyze.NO, name = "lastModification.sort", boost = @Boost(3000f))
    @DateBridge(resolution = Resolution.MILLISECOND)
    protected Date lastModification = new Date(); //united by some value to prevent NPE's

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }
}
