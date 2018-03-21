package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.web.controller.response.SuccessErrorResponse;
import com.infoclinika.mssharing.web.demo.SpringSupportTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpSession;


import javax.inject.Inject;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Pavel Kaplin
 */
@RunWith(Theories.class)
public class SecurityControllerTest extends SpringSupportTest {

    public static final String PAVEL_EMAIL = "pavel@example.com";
    public static final String JOHN_EMAIL = "john@example.com";
    @Inject
    private SecurityController securityController;

    @Inject
    private EmailVerificationCrypto crypto;

    @Inject
    private UserManagement userManagement;
    @Inject
    private UserRepository userRepository;

    @Inject
    private SecurityHelper securityHelper;

    private SecurityHelper.UserDetails pavel;
    private SecurityHelper.UserDetails johnWithExpiredLink;


    @Before
    public void setUp() {

        // pavel is a normal user
        final UserManagementTemplate.PersonInfo pavelInfo = new UserManagementTemplate.PersonInfo("Pavel", "Kaplin", PAVEL_EMAIL);
        final String password = "password";
        final String link = "/link";
        userManagement.createPerson(pavelInfo, password, new HashSet<>(), link);
        pavel = securityHelper.getUserDetailsByEmail(PAVEL_EMAIL);


        // john is a user with expired email verification link
        final long johnId = userManagement.createPerson(new UserManagementTemplate.PersonInfo("John", "Wick", JOHN_EMAIL), password, new HashSet<>(), link);
        final User john = userRepository.findOne(johnId);
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, -7);
        final Date lastWeek = now.getTime();
        john.setEmailVerificationSentOnDate(lastWeek);
        john.setPasswordResetSentOnDate(lastWeek);
        userRepository.save(john);

        johnWithExpiredLink = securityHelper.getUserDetailsByEmail(JOHN_EMAIL);

    }

    @Test
    public void testGetEmailVerificationMacCode() throws Exception {
        final String email = "pavel@example.com";
        final String macCode = crypto.getMac(email);
        assertTrue(crypto.isMacValid(email, macCode));
    }

    @Test
    public void testGetPasswordRecoveryUrl() throws Exception {
        String macCode = securityController.getMac(pavel);
        assertTrue(crypto.isMacValid(securityController.getMacString(pavel), macCode));
    }

    @Test
    public void testEmailCannotBeValidatedUsingExpiredLink() throws Exception {

        String macCode = securityController.getMac(johnWithExpiredLink.email);
        final MockHttpSession session = new MockHttpSession();
        securityController.verifyEmail(johnWithExpiredLink.email, macCode, session);

        assertEquals(session.getAttribute(SecurityController.EMAIL_VERIFIED), false);
    }

    @Test
    public void testEmailCanBeValidatedUsingFreshLink() throws Exception {

        String macCode = securityController.getMac(pavel.email);
        final MockHttpSession session = new MockHttpSession();
        securityController.verifyEmail(pavel.email, macCode, session);

        assertEquals(session.getAttribute(SecurityController.EMAIL_VERIFIED), true);
    }

    @Test
    public void testPasswordCannotBeResetUsingExpiredLink() throws Exception {

        String macCode = securityController.getMac(johnWithExpiredLink);
        final MockHttpSession session = new MockHttpSession();
        final SuccessErrorResponse successErrorResponse = securityController.canResetPassword(johnWithExpiredLink.email, macCode, session);

        assertNull(successErrorResponse.successMessage);
        assertNotNull(successErrorResponse.errorMessage);
    }


    @Test
    public void testPasswordBeResetUsingFreshLink() throws Exception {

        String macCode = securityController.getMac(pavel);
        final MockHttpSession session = new MockHttpSession();
        final SuccessErrorResponse successErrorResponse = securityController.canResetPassword(pavel.email, macCode, session);

        assertNull(successErrorResponse.errorMessage);
        assertNotNull(successErrorResponse.successMessage);
    }

}
