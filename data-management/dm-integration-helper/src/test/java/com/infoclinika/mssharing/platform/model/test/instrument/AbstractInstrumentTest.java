/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.test.instrument;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.test.helper.AbstractTest;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;

import java.util.Set;

import static com.google.common.collect.Iterables.size;
import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentAccess;
import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
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
        assertTrue(Iterables.any(instrumentReader.readInstrumentsWhereUserIsOperator(user), new Predicate<InstrumentItem>() {
            @Override
            public boolean apply(InstrumentItem input) {
                return input.id == instrumentId;
            }
        }));
    }

    protected void addOperatorDirectly(long user, long instrumentId) {
        final Long person = userManagement.createPersonAndApproveMembership(new PersonInfo("User", "Name", "just.for.test.assertIsOperator@example.com"),
                "pwd", ImmutableSet.of(uc.getLab3()), "/");
        instrumentManagement.addOperatorDirectly(user, instrumentId, person);
    }

    protected void assertIsNotOperator(final long user, final long instrument) {
        assertFalse(Iterables.any(instrumentReader.readInstrumentsWhereUserIsOperator(user), new Predicate<InstrumentItem>() {
            @Override
            public boolean apply(InstrumentItem input) {
                return input.id == instrument;
            }
        }));
    }

    protected void assertIsPending(long user, final long instrument) {
        final Set<? extends InstrumentLineTemplate> all = instrumentReader.readInstruments(user);
        final InstrumentLineTemplate instrumentItem = Iterables.find(all, new Predicate<InstrumentLineTemplate>() {
            @Override
            public boolean apply(InstrumentLineTemplate input) {
                return input.id == instrument;
            }
        });
        assertEquals(instrumentItem.access, InstrumentAccess.PENDING);
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
