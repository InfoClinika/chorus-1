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
package com.infoclinika.mssharing.model.internal.entity;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Since we are always using generated ids for entities (not domain related)
 * and entity can be always identified by it's id it's can be useful to convert ids to entities (detached).
 * For transforming a collection of such ids we can use Guava's util methods, like

 * Entities from id should be created only throw this class.
 *
 * @author Stanislav Kurilin
 */
public final class Util {

    private Util() {
    }

    public static final Function<AbstractPersistable<Long>, Long> ENTITY_TO_ID = AbstractPersistable::getId;
    public static final Function<Long, InstrumentModel> INSTRUMENT_MODEL = InstrumentModel::new;
    public static final Function<Long, User> USER_FROM_ID = User::new;
    public static final Function<Long, Group> GROUP_FROM_ID = Group::new;
    public static final Function<Long, Instrument> INSTRUMENT_FROM_ID = Instrument::new;
    public static final Function<Long, InstrumentModel> INSTRUMENT_MODEL_FROM_ID = InstrumentModel::new;
    public static final Function<Long, ActiveFileMetaData> FILE_FROM_ID = ActiveFileMetaData::new;
    public static final Function<Long, ActiveProject> PROJECT_FROM_ID = ActiveProject::new;
    public static final Function<Long, Lab> LAB_FROM_ID = Lab::new;
}
