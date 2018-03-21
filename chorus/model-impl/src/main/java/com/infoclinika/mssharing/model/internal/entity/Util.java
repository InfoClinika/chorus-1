/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.entity;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.annotation.Nullable;

/**
 * Since we are always using generated ids for entities (not domain related)
 * and entity can be always identified by it's id it's can be useful to convert ids to entities (detached).
 * For transforming a collection of such ids we can use Guava's util methods, like
 * <pre><tt>     Collection<Long> users = ...;
 *     Collection<User> entities = Collections2.transform(users, Util.USER_FROM_ID);
 * </tt></pre>
 * Entities from id should be created only throw this class.
 *
 * @author Stanislav Kurilin
 */
public final class Util {


    public static final Function<AbstractPersistable<Long>,Long> ENTITY_TO_ID = new Function<AbstractPersistable<Long>, Long>() {
        @Override
        public Long apply(AbstractPersistable<Long> input) {
            return input.getId();
        }
    };

    private Util() {
    }

    public static final Function<Long, InstrumentModel> INSTRUMENT_MODEL = new Function<Long, InstrumentModel>() {
        public InstrumentModel apply(Long input) {
            return new InstrumentModel(input);
        }
    };
    public static final Function<Long, User> USER_FROM_ID = new Function<Long, User>() {
        public User apply(Long input) {
            return new User(input);
        }
    };
    public static final Function<Long, Group> GROUP_FROM_ID = new Function<Long, Group>() {
        public Group apply(Long input) {
            return new Group(input);
        }
    };
    public static final Function<Long, Instrument> INSTRUMENT_FROM_ID = new Function<Long, Instrument>() {
        public Instrument apply(Long input) {
            return new Instrument(input);
        }
    };
    public static final Function<Long, InstrumentModel> INSTRUMENT_MODEL_FROM_ID = new Function<Long, InstrumentModel>() {
        public InstrumentModel apply(Long input) {
            return new InstrumentModel(input);
        }
    };
    public static final Function<Long, ActiveFileMetaData> FILE_FROM_ID = new Function<Long, ActiveFileMetaData>() {
        @Override
        public ActiveFileMetaData apply(@Nullable Long input) {
            return new ActiveFileMetaData(input);
        }
    };
    public static final Function<Long, ActiveProject> PROJECT_FROM_ID = new Function<Long, ActiveProject>() {
        @Override
        public ActiveProject apply(Long input) {
            return new ActiveProject(input);
        }
    };

    public static final Function<Long, Lab> LAB_FROM_ID = new Function<Long, Lab>() {
        @Override
        public Lab apply(Long input) {
            return new Lab(input);
        }
    };
}
