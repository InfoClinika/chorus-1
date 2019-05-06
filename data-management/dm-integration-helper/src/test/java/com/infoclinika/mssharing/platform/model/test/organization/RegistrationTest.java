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
package com.infoclinika.mssharing.platform.model.test.organization;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.test.helper.AbstractTest;
import com.infoclinika.mssharing.platform.model.testing.helper.Data;
import com.infoclinika.mssharing.platform.model.testing.helper.UserTestHelper;
import org.testng.annotations.Test;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.platform.model.testing.helper.Data.BOBS_EMAIL;
import static com.infoclinika.mssharing.platform.model.testing.helper.Data.KATE_INFO;
import static org.testng.Assert.*;

/**
 * Testing labs and users creation flows
 *
 * @author Stanislav Kurilin
 */
public class RegistrationTest extends AbstractTest {

    @Test
    public void testUserCreationInActivatedLabs() {
        final long request = uc.requestLab2creation();
        final long lab = labManagement.confirmLabCreation(admin(), request);
        userManagement.createPersonAndApproveMembership(KATE_INFO, "1231", ImmutableSet.of(lab), null);
    }

    @Test
    public void testEmailBecomeUnAvailableAfterUserCreation() {
        assertTrue(registrationHelper.isEmailAvailable(BOBS_EMAIL));
        uc.createLab3AndBob();
        assertFalse(registrationHelper.isEmailAvailable(BOBS_EMAIL));
    }

    //Test user information become available in "profile" forms
    @Test
    public void testReadShortForm() {
        final long bobsId = uc.createLab3AndBob();
        final UserTestHelper.UserShortForm userShortForm = userTestHelper.shortForm(bobsId);
        assertEquals(userShortForm.id, bobsId);
        assertEquals(userShortForm.email, BOBS_EMAIL);
        assertEquals(userShortForm.name, String.format("%s %s", Data.BOBS_FIRST, Data.BOBS_LAST));
        assertTrue(userShortForm.units.contains(Data.LAB_3));
    }

    @Test
    public void testAccountSettingsForm() {
        final long bobsId = uc.createLab3AndBob();
        final UserTestHelper.AccountSettingsForm accountSettingsForm = userTestHelper.accountSettingsForm(bobsId);
        assertEquals(accountSettingsForm.email, BOBS_EMAIL);
        assertEquals(accountSettingsForm.firstName, Data.BOBS_FIRST);
        assertEquals(accountSettingsForm.lastName, Data.BOBS_LAST);
        assertTrue(accountSettingsForm.laboratories.contains(Data.LAB_3));
    }

    //Basic user flows after registration
    @Test
    public void testUserCanLoginAfterRegistration() {
        assertNull(securityHelper.getUserDetailsByEmail(BOBS_EMAIL));
        uc.createLab3AndBob();
        assertNotNull(securityHelper.getUserDetailsByEmail(BOBS_EMAIL));
    }


    @Test
    public void testUserCanChangeHisPassword() {
        final Long id = uc.createLab3AndBob();
        final String oldHash = checkNotNull(securityHelper.getUserDetailsByEmail(BOBS_EMAIL)).password;
        userManagement.changePassword(id, Data.BOBS_PASS, encoder.encode("a123"));
        final String newHash = checkNotNull(securityHelper.getUserDetailsByEmail(BOBS_EMAIL)).password;
        assertNotEquals(oldHash, newHash);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testUserShouldKnowOldPasswordToChangeIt() {
        final Long id = uc.createLab3AndBob();
        userManagement.changePassword(id, "WRONG", encoder.encode("a123"));
    }


}
