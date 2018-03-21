package com.infoclinika.mssharing.platform.model.mailing;


import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate.UserDetails;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.InjectIntoByType;
import org.unitils.inject.annotation.TestedObject;

import java.io.IOException;

import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Pavel Kaplin
 */
public class NotifierImplTest extends UnitilsTestNG {

    public static final int ALEX = 1;
    public static final int PAVEL = 2;
    public static final int PROJECT = 3;
    private static final long LAB = 4;
    private static final long INSTRUMENT = 5;
    @TestedObject
    private NotifierTemplate notifier = new DefaultNotifier();
    @InjectInto(property = "emailer")
    private EmailerTemplate emailer = mock(EmailerTemplate.class);
    @InjectIntoByType
    private MailSendingHelperTemplate helper = mock(MailSendingHelperTemplate.class);
    @InjectIntoByType
    private VelocityEngine velocityEngine;

    public NotifierImplTest() throws IOException, VelocityException {
        velocityEngine = new VelocityEngineProvider().getVelocityEngine();
    }

    @BeforeMethod
    public void mockTestData() {
        reset(helper, emailer);
        when(helper.userDetails(ALEX)).thenReturn(new UserDetails("Alexei Tymchenko", "alexei@example.com"));
        when(helper.userDetails(PAVEL)).thenReturn(new UserDetails("Pavel Kaplin", "pavel@example.com"));
        when(helper.projectName(PROJECT)).thenReturn("Global Warm Project");
        when(helper.labName(LAB)).thenReturn("Secret Laboratory");
        when(helper.instrumentName(INSTRUMENT)).thenReturn("Precise Instrument");
    }

    @Test
    public void testProjectShared() throws Exception {
        notifier.projectShared(ALEX, PAVEL, PROJECT);
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Project Shared"),
                and(
                        contains("Alexei Tymchenko"),
                        contains("Global Warm Project")
                ));
    }

    @Test
    public void testRemovingFromProject() throws Exception {
        notifier.removingFromProject(ALEX, PROJECT);
        verify(emailer).send(
                eq("alexei@example.com"),
                contains("Project Access Revoked"),
                contains("Global Warm Project"));
    }

    @Test
    public void testRemovingFromProjectWithCreatingNew() throws Exception {
        notifier.removingFromProjectWithCreatingNew(ALEX, PROJECT, 5);
        verify(emailer).send(
                eq("alexei@example.com"),
                contains("Project Access Revoked"),
                contains("copied"));
    }

    @Test
    public void testLabCreationApproved() throws Exception {
        notifier.labCreationApproved("pavel@example.com", LAB);
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Lab Creation Request Approved"),
                contains("Secret Laboratory"));
    }

    @Test
    public void testLabCreationRejected() throws Exception {
        final String requesterEmail = "pavel@example.com";
        final String comment = "That's why!";
        final String labName = "My super lab";
        notifier.labCreationRejected(requesterEmail, comment, labName);
        verify(emailer).send(
                eq(requesterEmail),
                contains("Lab Creation Request Rejected"),
                contains(comment));
    }

    @Test
    public void testStaleOnLabRequest() throws Exception {
        notifier.staleOnLabRequest(PAVEL, 1);
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Error Processing Lab Request"),
                contains("decision"));
    }

    @Test
    public void testUserWasAddedToOperators() throws Exception {
        notifier.userWasAddedToOperators(PAVEL, ALEX, INSTRUMENT);
        verify(emailer).send(
                eq("alexei@example.com"),
                contains(" New Instrument Available"),
                contains("Precise Instrument"));
    }

    @Test
    public void testInstrumentRequestApproved() throws Exception {
        notifier.instrumentRequestApproved(PAVEL, ALEX, INSTRUMENT);
        verify(emailer).send(
                eq("alexei@example.com"),
                contains("Instrument Request Approved"),
                contains("Precise Instrument"));
    }

    @Test
    public void testInstrumentRequestRefuse() throws Exception {
        notifier.instrumentRequestRefuse(ALEX, PAVEL, INSTRUMENT, "That's why!");
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Instrument Request Rejected"),
                contains("That's why!"));
    }

    @Test
    public void testStaleOnInstrumentRequest() throws Exception {
        notifier.staleOnInstrumentRequest(ALEX, INSTRUMENT, PAVEL);
        verify(emailer).send(
                eq("alexei@example.com"),
                contains("Error Processing Instrument Request"),
                and(
                        contains("Pavel Kaplin"),
                        contains("Precise Instrument")
                ));
    }

    @Test
    public void testUserRegistered() throws Exception {
        notifier.userRegistered(PAVEL, "http://google.com");
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Confirm Your Email"),
                contains("http://google.com"));
    }

    @Test
    public void testRecoverPassword() throws Exception {
        notifier.recoverPassword(PAVEL, "http://google.com");
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Reset Your Password"),
                contains("http://google.com"));
    }

    @Test
    public void testLabMembershipApproved() throws Exception {
        notifier.labMembershipApproved(PAVEL, LAB);
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Lab Membership Request Approved"),
                contains("Secret Laboratory"));
    }

    @Test
    public void testLabMembershipRejected() throws Exception {
        notifier.labMembershipRejected(PAVEL, LAB, "That's why!");
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Lab Membership Request Rejected"),
                contains("That's why!"));
    }

    @Test
    public void testSendGeneratedPassword() throws Exception {
        notifier.sendGeneratedPassword(PAVEL, "123456");
        verify(emailer).send(
                eq("pavel@example.com"),
                contains("Your Password"),
                contains("123456")
        );
    }
}
