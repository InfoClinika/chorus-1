/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
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
    static final String SELECT_INSTRUMENT_WITH_OPERATORS_BY_ID_USER = "select i from Instrument i join i.operators o where o.id = :user";
    static final String SELECT_AVAILABLE_FILES = "select i from ActiveFileMetaData f join f.instrument i where f.contentId is not null and " + ChorusQueries.IS_FILE_AVAILABLE;

    @Query("select i.id from Instrument i where i.serialNumber = :sn")
    Long isAlreadyPresented(@Param("sn") String serialNumber);

    @Query("select i.lab from Instrument i where i.serialNumber = :sn")
    Lab labOfInstrument(@Param("sn") String serialNumber);

/*
    @Query("select distinct new com.infoclinika.mssharing.platform.repository.DictionaryRepoItem(m.id, concat(v.name, ' - ', m.name) as name)" +
            "from Instrument i join i.lab l join i.model m join m.vendor v " +
            "where (i in (" + SELECT_INSTRUMENT_WITH_OPERATORS_BY_ID_USER + ") or " +
            "i in (" + SELECT_AVAILABLE_FILES + ")) " +
            "and (cast(:lab as integer) = 0 or l.id = :lab) order by name")
    List<DictionaryRepoItem> availableInstrumentModels(@Param("user") long userId, @Param("lab") long labId);

    @Query("select i from Instrument i join i.lab l join i.model m " +
            "where (i in (" + SELECT_INSTRUMENT_WITH_OPERATORS_BY_ID_USER + ") or " +
            "i in (" + SELECT_AVAILABLE_FILES + ")) and m.id = :model order by i.name")
    List<Instrument> availableInstrumentsByModel(@Param("user") long userId, @Param("model") long modelId);*/

}


