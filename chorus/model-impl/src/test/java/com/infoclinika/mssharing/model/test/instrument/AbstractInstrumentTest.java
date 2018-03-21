/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.test.instrument;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;

import java.util.Set;

import static com.google.common.collect.Iterables.size;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
abstract class AbstractInstrumentTest extends AbstractTest {
    /**
     * All methods that use this assertion should be depended on testOnlyOperatorsCanAddMore
     */
    protected void assertIsOperator(long user, final long instrumentId) {
        assertEquals(dashboardReader.readInstruments(user).iterator().next().access, DashboardReader.InstrumentAccess.OPERATOR);
        assertTrue(Iterables.any(dashboardReader.readInstrumentsWhereUserIsOperator(user), new Predicate<InstrumentItem>() {
            @Override
            public boolean apply(InstrumentItem input) {
                return input.id == instrumentId;
            }
        }));
    }

    protected void addOperatorDirectly(long user, long instrumentId, String operatorEmail) throws AccessDenied{
        final long person = userManagement.createPersonAndApproveMembership(
                new UserManagementTemplate.PersonInfo("User", "Name", operatorEmail),
                "test_pwd", ImmutableSet.of(uc.getLab3()), "/");
        instrumentManagement.addOperatorDirectly(user, instrumentId, person);
    }

    protected void assertIsNotOperator(final long user, final long instrument) {
        assertFalse(Iterables.any(dashboardReader.readInstrumentsWhereUserIsOperator(user), new Predicate<InstrumentItem>() {
            @Override
            public boolean apply(InstrumentItem input) {
                return input.id == instrument;
            }
        }));
    }

    protected void assertIsPending(long user, final long instrument) {
        final Set<InstrumentLine> all = dashboardReader.readInstruments(user);
        final InstrumentLine instrumentItem = Iterables.find(all, new Predicate<InstrumentLine>() {
            @Override
            public boolean apply(InstrumentLine input) {
                return input.id == instrument;
            }
        });
        assertEquals(instrumentItem.access, DashboardReader.InstrumentAccess.PENDING);
    }

    protected void assertNumberOfAvailableInstrumentTypes(long user, long number) {
        final Iterable<DictionaryItem> models = experimentCreationHelper.availableInstrumentModels(user, null);
        assertEquals(size(models), number);
        for (DictionaryItem model : models) {
            assertTrue(size(experimentCreationHelper.availableInstrumentsByModel(user, model.id)) > 0);
        }
    }

    protected long anySpecies() {
        return experimentCreationHelper.species().iterator().next().id;
    }

}
