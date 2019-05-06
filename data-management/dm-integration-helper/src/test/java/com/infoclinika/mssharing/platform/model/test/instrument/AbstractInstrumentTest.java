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
package com.infoclinika.mssharing.platform.model.test.instrument;

import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.test.helper.AbstractTest;

import static com.google.common.collect.Iterables.size;
import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentAccess;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
abstract class AbstractInstrumentTest extends AbstractTest {
    /**
     * All methods that use this assertion should be depended on testOnlyOperatorsCanAddMore
     */
    protected void assertIsOperator(long user, final long instrumentId) {
        assertEquals(instrumentReader.readInstruments(user).iterator().next().access, InstrumentAccess.OPERATOR);
        assertTrue(
            Iterables.any(instrumentReader.readInstrumentsWhereUserIsOperator(user), input -> input.id == instrumentId)
        );
    }

    protected void assertIsNotOperator(final long user, final long instrument) {
        assertFalse(
            Iterables.any(instrumentReader.readInstrumentsWhereUserIsOperator(user), input -> input.id == instrument)
        );
    }

    protected void assertNumberOfAvailableInstrumentTypes(long user, long number) {
        final Iterable<DictionaryItem> models = experimentCreationHelper.availableInstrumentModels(user, null);
        assertEquals(size(models), number);
        for (DictionaryItem model : models) {
            assertTrue(size(experimentCreationHelper.availableInstrumentsByModel(user, model.id)) > 0);
        }
    }

    public long anySpecies() {
        return experimentCreationHelper.species().iterator().next().id;
    }

}
