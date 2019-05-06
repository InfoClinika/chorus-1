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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentAccess;
import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
public class ManagingOperatorsTest extends AbstractInstrumentTest {

    @Test(dependsOnMethods = "testNotOnlyOperatorsCanAddMore")
    public void testCreatorBecomeOperator() {
        final long bob = uc.createLab3AndBob();
        final Long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        assertIsOperator(bob, instrument);
    }

    @Test
    public void testDefaultHasOperatorAccess() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        final InstrumentLineTemplate instrument = instrumentReader.readInstruments(bob).iterator().next();
        assertEquals(instrument.access, InstrumentAccess.OPERATOR);
    }

    @Test(enabled = false) // no longer valid case
    public void testUserGetEmailOnAddingHimDirectlyToOperators() {
        final long bob = uc.createLab3AndBob();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long joe = uc.createJoe();
        verify(notificator()).userWasAddedToOperators(eq(bob), eq(joe), eq(instrumentId));
    }

    @Test
    public void testUserGetEmailOnApprovingInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        verify(notificator()).sendInstrumentCreationApprovedNotification(eq(bob), anyString(), anyString());
    }

    @Test(enabled = false)
    public void testUserGetEmailOnRefusingInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
        final String refuseComment = generateString();
        verify(notificator()).instrumentRequestRefuse(eq(bob), eq(poll), eq(instrumentId), eq(refuseComment));
    }

    @Test
    public void testOperatorAfterAccessGranted() {
        final long bob = uc.createLab3AndBob();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
        final InstrumentLineTemplate instrument = instrumentReader.readInstruments(poll).iterator().next();

        assertEquals(instrument.access, InstrumentAccess.OPERATOR);
    }

    @Test(dependsOnMethods = "testNotOnlyOperatorsCanAddMore")
    public void testAddOperatorFromSameLab() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();

        assertIsOperator(poll, instrument);
    }

    @Test(dependsOnMethods = "testAddOperatorFromSameLab")
    public void testAddOperatorByOtherOperator() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
    }


    @Test
    public void testNotOnlyOperatorsCanAddMore() {
        final long poll = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();
    }

    //Users can add operators that are not system users //TODO: [stanislav.kurilin] implement it
    @Test(dependsOnMethods = {"testAddOperatorFromDifferentLab", "testNotOnlyOperatorsCanAddMore"}, enabled = false)
    public void testBecomingOperatorInOtherLab() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        uc.createLab2();
        uc.createKateAndLab2();
    }

    @Test(dependsOnMethods = {"testAddOperatorFromSameLab", "testNotOnlyOperatorsCanAddMore"}, enabled = false)
    public void testBecomingOperatorInSameLab() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
        assertIsOperator(poll, instrument);
    }

    @Test(dependsOnMethods = {"testAddOperatorFromSameLab", "testNotOnlyOperatorsCanAddMore"})
    public void testPeopleFromSameLabAvailableToBecomeOperator() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        assertTrue(Iterables.any(
            instrumentCreationHelper.availableOperators(uc.getLab3()),
            input -> input.id == poll
        ));
    }

    @Test(dependsOnMethods = {"testPeopleFromSameLabAvailableToBecomeOperator"})
    public void testPeopleFromOtherLabsIsntAvailableToBecomeOperator() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        assertFalse(Iterables.any(
            instrumentCreationHelper.availableOperators(uc.getLab3()),
            input -> input.id == kate
        ));
    }

    @Test(dependsOnMethods = "testNotOnlyOperatorsCanAddMore", enabled = false)
    public void testPendingInSameLab() {
        uc.createLab3();
        final long poll = uc.createPaul();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();
        final long bob = uc.tryBobCreation();
        assertIsOperator(bob, instrument);
    }

    @Test
    public void testLostAccessToInstrumentWithLabMembership() {
        final long paul = uc.createPaul();
        final long lab = uc.createLab3();
        final long joe = uc.createJoe();
        long instrument = uc.createInstrumentAndApproveIfNeeded(paul, lab).get();
        assertIsOperator(joe, instrument);
        labHeadManagement.removeUserFromLab(paul, lab, joe);
        assertIsNotOperator(joe, instrument);
        long request = userManagement.applyForLabMembership(joe, lab);
        userManagement.approveLabMembershipRequest(paul, request);
        assertIsOperator(joe, instrument);
    }

}
