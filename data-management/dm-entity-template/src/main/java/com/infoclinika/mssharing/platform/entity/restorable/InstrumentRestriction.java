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
package com.infoclinika.mssharing.platform.entity.restorable;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * @author Stanislav Kurilin
 */
@Embeddable
public class InstrumentRestriction<I extends InstrumentTemplate<?, ?>> {
    @ManyToOne(optional = false)
    private InstrumentModel instrumentModel;
    @ManyToOne(optional = true)
    private I instrument;

    public InstrumentRestriction(InstrumentModel instrumentModel, Optional<I> instrument) {
        this.instrumentModel = instrumentModel;
        this.instrument = instrument.orNull();
    }

    protected InstrumentRestriction() {
    }

    public InstrumentModel getInstrumentModel() {
        return instrumentModel;
    }

    public I getInstrument() {
        return instrument;
    }
}
