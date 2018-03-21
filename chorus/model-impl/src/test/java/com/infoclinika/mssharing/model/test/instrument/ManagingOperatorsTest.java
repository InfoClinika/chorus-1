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
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.model.helper.Data;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.model.read.RequestsReader;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
public class ManagingOperatorsTest extends AbstractInstrumentTest {
    @Test(dependsOnMethods = "testOnlyOperatorsCanAddMore")
    public void testCreatorBecomeOperator() {
        final long bob = uc.createLab3AndBob();
        final Long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        assertIsOperator(bob, instrument);
    }

    @Test
    public void testDefaultHasNoAccess() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        final InstrumentLine instrument = dashboardReader.readInstruments(bob).iterator().next();
        assertEquals(instrument.access, DashboardReader.InstrumentAccess.NO_ACCESS);
    }

    @Test
    public void testPendingAfterRequestAccess() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();
        instrumentManagement.requestAccessToInstrument(bob, instrumentId);
        assertIsPending(bob, instrumentId);
    }


    @Test
    public void testReadOutgoingInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
        instrumentManagement.requestAccessToInstrument(poll, instrumentId);

        final ImmutableSortedSet<RequestsReader.InstrumentRequest> instrumentRequests = requestsReader.myInstrumentInbox(poll);
        assertEquals(instrumentRequests.size(), 1);
        final RequestsReader.InstrumentRequest request = instrumentRequests.first();
        assertEquals((Object) request.instrument, instrumentId);
        assertEquals(request.requester, poll);
        assertNotNull(request.instrumentName);
        assertNotNull(request.sent);
    }

    @Test
    public void testReadInboxInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();

        instrumentManagement.requestAccessToInstrument(poll, instrumentId);

        final ImmutableSortedSet<RequestsReader.InstrumentRequest> instrumentRequests = requestsReader.myInstrumentInbox(bob);
        assertEquals(instrumentRequests.size(), 1);
        final RequestsReader.InstrumentRequest request = instrumentRequests.first();
        assertEquals((Object) request.instrument, instrumentId);
        assertEquals(request.requester, poll);
        assertNotNull(request.instrumentName);
        assertNotNull(request.sent);
    }

    @Test
    public void testAllOperatorsGetInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.addOperatorDirectly(bob, instrumentId, poll);

        instrumentManagement.requestAccessToInstrument(joe, instrumentId);

        assertEquals(requestsReader.myInstrumentInbox(bob).size(), 1);
        assertEquals(requestsReader.myInstrumentInbox(poll).size(), 1);
    }

    @Test
    public void testFirstWinsOnAddOperatorsApproveInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.addOperatorDirectly(bob, instrumentId, poll);
        instrumentManagement.requestAccessToInstrument(joe, instrumentId);

        instrumentManagement.approveAccessToInstrument(bob, instrumentId, joe);
        instrumentManagement.refuseAccessToInstrument(poll, instrumentId, joe, generateString());

        assertIsOperator(poll, instrumentId);
        verify(notificator()).staleOnInstrumentRequest(poll, instrumentId, joe);
    }

    @Test
    public void testFirstWinsOnRefuseOperatorsApproveInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.addOperatorDirectly(bob, instrumentId, poll);
        instrumentManagement.requestAccessToInstrument(joe, instrumentId);

        instrumentManagement.refuseAccessToInstrument(bob, instrumentId, joe, generateString());
        instrumentManagement.approveAccessToInstrument(poll, instrumentId, joe);

        assertIsOperator(poll, instrumentId);
        verify(notificator()).staleOnInstrumentRequest(poll, instrumentId, joe);
    }

    @Test(dependsOnMethods = "testFirstWinsOnAddOperatorsApproveInstrumentRequest")
    public void testNoLoosingOnSameSolutionToApproveInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.addOperatorDirectly(bob, instrumentId, poll);
        instrumentManagement.requestAccessToInstrument(joe, instrumentId);

        instrumentManagement.addOperatorDirectly(poll, instrumentId, joe);
        instrumentManagement.addOperatorDirectly(bob, instrumentId, joe);

        assertIsOperator(poll, instrumentId);
        verify(notificator(), never()).staleOnInstrumentRequest(bob, instrumentId, joe);
    }

    @Test(dependsOnMethods = "testFirstWinsOnRefuseOperatorsApproveInstrumentRequest")
    public void testNoLoosingOnSameSolutionToRefuseInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.addOperatorDirectly(bob, instrumentId, poll);
        instrumentManagement.requestAccessToInstrument(joe, instrumentId);

        instrumentManagement.refuseAccessToInstrument(poll, instrumentId, joe, generateString());
        instrumentManagement.refuseAccessToInstrument(bob, instrumentId, joe, generateString());

        assertIsOperator(poll, instrumentId);
        verify(notificator(), never()).staleOnInstrumentRequest(bob, instrumentId, joe);
    }
    //TODO: [stanislav.kurilin] test adding operator directly when request was send

    @Test
    public void testEmptyBoxesAfterRequestProcessed() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.addOperatorDirectly(bob, instrumentId, poll);
        instrumentManagement.requestAccessToInstrument(joe, instrumentId);

        instrumentManagement.approveAccessToInstrument(poll, instrumentId, joe);

        assertEquals(requestsReader.myInstrumentInbox(bob).size(), 0);
        assertEquals(requestsReader.myInstrumentInbox(poll).size(), 0);
        assertEquals(requestsReader.myInstrumentInbox(joe).size(), 0);
    }

    @Test
    public void testEmptyBoxesAfterRefusingRequest() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.addOperatorDirectly(bob, instrumentId, poll);
        instrumentManagement.requestAccessToInstrument(joe, instrumentId);

        instrumentManagement.refuseAccessToInstrument(poll, instrumentId, joe, generateString());

        assertEquals(requestsReader.myInstrumentInbox(bob).size(), 0);
        assertEquals(requestsReader.myInstrumentInbox(poll).size(), 0);
        assertEquals(requestsReader.myInstrumentInbox(joe).size(), 0);
    }

    @Test
    public void testUserGetEmailOnAddingHimDirectlyToOperators() {
        final long bob = uc.createLab3AndBob();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long joe = uc.createJoe();
        instrumentManagement.addOperatorDirectly(bob, instrumentId, joe);
        verify(notificator()).userWasAddedToOperators(eq(bob), eq(joe), eq(instrumentId));
    }

    @Test
    public void testUserGetEmailOnApprovingInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();
        instrumentManagement.requestAccessToInstrument(bob, instrumentId);
        instrumentManagement.approveAccessToInstrument(poll, instrumentId, bob);
        verify(notificator()).instrumentRequestApproved(eq(poll), eq(bob), eq(instrumentId));
    }

    @Test
    public void testUserGetEmailOnRefusingInstrumentRequest() {
        final long bob = uc.createLab3AndBob();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
        final String refuseComment = generateString();
        instrumentManagement.requestAccessToInstrument(poll, instrumentId);
        instrumentManagement.refuseAccessToInstrument(bob, instrumentId, poll, refuseComment);
        verify(notificator()).instrumentRequestRefuse(eq(bob), eq(poll), eq(instrumentId), eq(refuseComment));
    }

    @Test
    public void testSeveralUsersCanBePending() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();

        instrumentManagement.requestAccessToInstrument(bob, instrumentId);
        instrumentManagement.requestAccessToInstrument(joe, instrumentId);

        assertIsPending(bob, instrumentId);
        assertIsPending(joe, instrumentId);
    }

    @Test
    public void testUserCanPendingToSeveralInstruments() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();

        final Long instrumentA = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();
        final Long instrumentB = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();

        instrumentManagement.requestAccessToInstrument(bob, instrumentA);
        instrumentManagement.requestAccessToInstrument(bob, instrumentB);

        assertIsPending(bob, instrumentA);
        assertIsPending(bob, instrumentB);
    }

    @Test
    public void testOperatorAfterAccessGranted() {
        final long bob = uc.createLab3AndBob();
        final Long instrumentId = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
        instrumentManagement.requestAccessToInstrument(poll, instrumentId);
        instrumentManagement.addOperatorDirectly(bob, instrumentId, poll);
        final InstrumentLine instrument = dashboardReader.readInstruments(poll).iterator().next();

        assertEquals(instrument.access, DashboardReader.InstrumentAccess.OPERATOR);
    }

    @Test(dependsOnMethods = "testOnlyOperatorsCanAddMore")
    public void testAddOperatorFromSameLab() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
        instrumentManagement.addOperatorDirectly(bob, instrument, poll);
        assertIsOperator(poll, instrument);
    }

    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testOnlyOperatorsCanAddMore")
    public void testAddOperatorFromDifferentLab() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        uc.createLab2();
        final long kate = uc.createKateAndLab2();

        instrumentManagement.addOperatorDirectly(bob, instrument, kate);
    }

    @Test(dependsOnMethods = "testAddOperatorFromSameLab")
    public void testAddOperatorByOtherOperator() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long poll = uc.createPaul();
        instrumentManagement.addOperatorDirectly(bob, instrument, poll);
        addOperatorDirectly(bob, instrument, "test_user_operator@example.com");
    }


    @Test(expectedExceptions = AccessDenied.class)
    public void testOnlyOperatorsCanAddMore() {
        final long poll = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();
        addOperatorDirectly(bob, instrument, "some@example.com");
    }

    //Users can add operators that are not system users //TODO: [stanislav.kurilin] implement it
    @Test(dependsOnMethods = {"testAddOperatorFromDifferentLab", "testOnlyOperatorsCanAddMore"})
    public void testBecomingOperatorInOtherLab() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long paul = uc.createPaul();
        instrumentManagement.addOperatorDirectly(bob, instrument, paul);
        uc.createLab2();
        uc.createKateAndLab2();
    }

    @Test(dependsOnMethods = {"testAddOperatorFromSameLab", "testOnlyOperatorsCanAddMore"})
    public void testBecomingOperatorInSameLab() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long paul = uc.createPaul();
        instrumentManagement.addOperatorDirectly(bob, instrument, paul);
        assertIsOperator(paul, instrument);
    }

    @Test(dependsOnMethods = {"testAddOperatorFromSameLab", "testOnlyOperatorsCanAddMore"})
    public void testPeopleFromSameLabAvailableToBecomeOperator() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        assertTrue(Iterables.any(instrumentCreationHelper.availableOperators(uc.getLab3()), new Predicate<InstrumentCreationHelperTemplate.PotentialOperator>() {

            @Override
            public boolean apply(InstrumentCreationHelperTemplate.PotentialOperator input) {
                return input.id == poll;
            }
        }));
    }

    @Test(dependsOnMethods = {"testPeopleFromSameLabAvailableToBecomeOperator"})
    public void testPeopleFromOtherLabsIsntAvailableToBecomeOperator() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        assertFalse(Iterables.any(instrumentCreationHelper.availableOperators(uc.getLab3()), new Predicate<InstrumentCreationHelperTemplate.PotentialOperator>() {

            @Override
            public boolean apply(InstrumentCreationHelperTemplate.PotentialOperator input) {
                return input.id == kate;
            }
        }));
    }

    @Test(dependsOnMethods = "testOnlyOperatorsCanAddMore")
    public void testPendingInSameLab() {
        uc.createLab3();
        final long poll = uc.createPaul();
        final long bob = uc.tryBobCreation();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();
        instrumentManagement.addOperatorDirectly(poll, instrument, bob);
        assertIsOperator(bob, instrument);
    }

    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testOnlyOperatorsCanAddMore")
    public void testPendingInOtherLab() {
        uc.createLab3();
        final long poll = uc.createPaul();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3()).get();
        uc.createLab2();
        final long kate = uc.createKateAndLab2();
        instrumentManagement.addOperatorDirectly(poll, instrument, kate);
    }

    @Test
    public void testLostAccessToInstrumentWithLabMembership() {
        final long paul = uc.createPaul();
        final long lab = uc.createLab3();
        final long joe = uc.createJoe();
        long instrument = uc.createInstrumentAndApproveIfNeeded(paul, lab).get();
        assertIsNotOperator(joe, instrument);
        instrumentManagement.addOperatorDirectly(paul, instrument, joe);
        assertIsOperator(joe, instrument);
        labHeadManagement.removeUserFromLab(paul, lab, joe);
        assertIsNotOperator(joe, instrument);
        long request = userManagement.applyForLabMembership(joe, lab);
        userManagement.approveLabMembershipRequest(paul, request);
        assertIsNotOperator(joe, instrument);
    }

}
