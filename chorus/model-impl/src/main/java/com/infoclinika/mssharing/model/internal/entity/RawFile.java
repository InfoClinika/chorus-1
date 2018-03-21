/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity;


import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.model.impl.entities.AnnotationDefault;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Stanislav Kurilin
 */
@Entity
public class RawFile extends ExperimentFileTemplate<AbstractFileMetaData, AbstractExperiment, AnnotationDefault> {
    @Column
    @Basic(optional = false)
    private boolean scansUploaded;

    @Column
    @Basic(optional = false)
    private int fractionNumber;

    @ManyToOne(optional = false)
    private ExperimentPreparedSample preparedSample;

    public RawFile(AbstractFileMetaData fileMetaData, int fractionNumber, ExperimentPreparedSample preparedSample) {
        super(fileMetaData, newArrayList());
        scansUploaded = false;
        this.fractionNumber = fractionNumber;
        this.preparedSample = preparedSample;
    }

    public RawFile() {
        scansUploaded = false;
    }

    public boolean isScansUploaded() {
        return scansUploaded;
    }

    public void setScansUploaded(boolean scansUploaded) {
        this.scansUploaded = scansUploaded;
    }

    public ExperimentPreparedSample getPreparedSample() {
        return preparedSample;
    }

    public void setPreparedSample(ExperimentPreparedSample preparedSample) {
        this.preparedSample = preparedSample;
    }

    public int getFractionNumber() {
        return fractionNumber;
    }

    public void setFractionNumber(int fractionNumber) {
        this.fractionNumber = fractionNumber;
    }
}
