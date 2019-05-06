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
package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Stanislav Kurilin
 */
public interface InstrumentRepository extends InstrumentRepositoryTemplate<Instrument> {

    @Query("select i.id from Instrument i where i.serialNumber = :sn")
    Long isAlreadyPresented(@Param("sn") String serialNumber);

    @Query("select i.lab from Instrument i where i.serialNumber = :sn")
    Lab labOfInstrument(@Param("sn") String serialNumber);

    @Query("select i.lab from Instrument i where i.id = :id")
    Lab labOfInstrument(@Param("id") long instrumentId);
}


