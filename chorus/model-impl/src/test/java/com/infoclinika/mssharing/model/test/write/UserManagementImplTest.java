package com.infoclinika.mssharing.model.test.write;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.helper.Data;
import com.infoclinika.mssharing.model.helper.SecurityHelper.UserDetails;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.SharedPerson;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate.LabLineTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

/**
 * @author Ruslan Duboveckij
 */
public class UserManagementImplTest extends AbstractTest {

    private UserManagementTemplate.LabMembershipConfirmationUrlProvider DEFAULT_URL_PROVIDER = new UserManagementTemplate.LabMembershipConfirmationUrlProvider() {
        @Override
        public String getUrl(long user, long lab, long requestId, UserManagementTemplate.LabMembershipRequestActions action) throws URISyntaxException {
            return "";
        }
    };

    private class DefaultUserInfo {
        public final String firstName;
        public final String lastName;
        public final String email;
        public final String password;
        public final String verificationUrl;

        public DefaultUserInfo(String firstName, String lastName, String email, String password, String verificationUrl) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.verificationUrl = verificationUrl;
        }

        public UserManagementTemplate.PersonInfo toPersonInfo() {
            return new UserManagementTemplate.PersonInfo(firstName, lastName, email);
        }
    }

    private final DefaultUserInfo janos = new DefaultUserInfo("Janos", "Slint", "js@email.cml", "pwd", "/url/janos");

    @Test
    public void testUpdatePersonAndSendEmailTestingLeaveLab() throws Exception {
        long kate = uc.createKateAndLab2();
        uc.addKateToLab3();
        uc.addKateToLab4();
        final long lab2Head = uc.getLab2();
        final long lab3Live = uc.getLab3();
        final long lab4Deleted = uc.getLab4();
        userManagement.updatePersonAndSendEmail(kate, Data.KATE_INFO, newHashSet(lab3Live), DEFAULT_URL_PROVIDER);
        List<Long> checkLabs = newArrayList(Collections2.transform(dashboardReader.readUserLabs(kate),
                new Function<LabLineTemplate, Long>() {
                    @Nullable
                    @Override
                    public Long apply(@Nullable LabLineTemplate labLine) {
                        return labLine.id;
                    }
                }));

        Assert.assertTrue(checkLabs.contains(lab2Head) && checkLabs.contains(lab3Live)
                && !checkLabs.contains(lab4Deleted), "Could not remove user from lab");
    }

    @Test
    public void testCreateUser() {
        Long createdPerson = userManagement.createPerson(janos.toPersonInfo(), janos.password, new HashSet<Long>(), janos.verificationUrl);
        ;

        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(createdPerson);
        Assert.assertEquals(janos.toPersonInfo(), personInfo);
    }

    @Test
    public void testCreateUserAndSendEmail() throws URISyntaxException {
        uc.createLab2();
        Long lab2 = uc.getLab2();
        Long createdPerson = userManagement.createPersonAndSendEmail(janos.toPersonInfo(), generateString(), newHashSet(lab2), janos.verificationUrl, new UserManagementTemplate.LabMembershipConfirmationUrlProvider() {
            @Override
            public String getUrl(long user, long lab, long requestId, UserManagementTemplate.LabMembershipRequestActions action) throws URISyntaxException {
                return "";
            }
        });

        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(createdPerson);
        Assert.assertEquals(janos.toPersonInfo(), personInfo);
    }

    @Test
    public void testCreateUserWithGeneratedPassword() {
        long createdUser = userManagement.createUserWithGeneratedPassword(janos.toPersonInfo(), janos.verificationUrl);
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(createdUser);
        Assert.assertEquals(janos.toPersonInfo(), personInfo);
    }

    @Test
    public void testUpdateUserData() {
        long john = uc.createJohnWithoutLab();
        uc.createLab2();
        Long lab2 = uc.getLab2();
        userManagement.updatePerson(john, janos.toPersonInfo(), newHashSet(lab2));

        UserManagementTemplate.PersonInfo johnInfoAfterUpdate = userReader.readPersonInfo(john);
        Assert.assertEquals(janos.toPersonInfo(), johnInfoAfterUpdate);
    }

    @Test
    public void testUserCreationWhenInviteUser() {
        String invitationLink = userManagement.inviteUser(admin(), janos.email);
        UserManagementTemplate.PersonInfo personInfo = securityHelper.getUserDetailsByInvitationLink(invitationLink);
        Assert.assertEquals(personInfo.email, janos.email);
    }

    @Test
    public void testVerifyEmail() {
        long johnWithoutLab = uc.createJohnWithoutLab();
        UserDetails userBeforeVerification = securityHelper.getUserDetails(johnWithoutLab);
        Assert.assertFalse(userBeforeVerification.emailVerified);

        userManagement.verifyEmail(johnWithoutLab);
        UserDetails userAfterVerification = securityHelper.getUserDetails(johnWithoutLab);
        Assert.assertTrue(userAfterVerification.emailVerified);
    }

    @Test
    public void testResetPassword() {
        long john = uc.createJohnWithoutLab();

        final String newPasswordHash = generateString();
        userManagement.resetPassword(john, newPasswordHash);

        UserDetails johnDetailsAfterReset = securityHelper.getUserDetails(john);
        Assert.assertEquals(johnDetailsAfterReset.password, newPasswordHash);
    }

    @Test
    public void testUpdateUserInfoWhenSavingInvitedUser() throws URISyntaxException {
        final String passwordHash = generateString();
        uc.createLab2();
        Long lab2 = uc.getLab2();
        userManagement.inviteUser(admin(), janos.email);
        Long savedUser = userManagement.saveInvited(janos.toPersonInfo(), passwordHash, newHashSet(lab2), janos.verificationUrl, DEFAULT_URL_PROVIDER);

        UserManagementTemplate.PersonInfo savedUserInfo = userReader.readPersonInfo(savedUser);
        assertEquals(savedUserInfo, janos.toPersonInfo());
    }

    @Test
    public void testInvitationLinkIsDeletedAfterSavingInvitedUser() throws URISyntaxException {
        final String passwordHash = generateString();
        uc.createLab2();
        Long lab2 = uc.getLab2();
        String invitationLink = userManagement.inviteUser(admin(), janos.email);
        Long savedUser = userManagement.saveInvited(janos.toPersonInfo(), passwordHash, newHashSet(lab2), janos.verificationUrl, DEFAULT_URL_PROVIDER);

        UserDetails userByLink = securityHelper.getUserDetailsByInvitationLink(invitationLink);
        assertEquals(userByLink, null);
    }

    @Test
    public void testUserHasLabAfterMembershipRequestHasBeenApproved() {
        long kate = uc.createKateAndLab2();
        Long lab2 = uc.getLab2();
        Long user = userManagement.createPerson(janos.toPersonInfo(), janos.password, newHashSet(lab2), janos.verificationUrl);
        ImmutableSortedSet<RequestsReaderTemplate.LabMembershipRequest> lab2MembershipRequests = requestsReader.myLabMembershipInbox(kate);

        userManagement.approveLabMembershipRequest(kate, newArrayList(lab2MembershipRequests).get(0).requestId);
        Set<Long> labIds = getUserLabIds(user);

        assertTrue(labIds.contains(lab2));
    }

    @Test
    public void testUserDoesNotHaveLabAfterMembershipRequestHasBeenRejected() {
        long kate = uc.createKateAndLab2();
        Long lab2 = uc.getLab2();
        Long user = userManagement.createPerson(janos.toPersonInfo(), janos.password, newHashSet(lab2), janos.verificationUrl);
        ImmutableSortedSet<RequestsReaderTemplate.LabMembershipRequest> lab2MembershipRequests = requestsReader.myLabMembershipInbox(kate);

        userManagement.rejectLabMembershipRequest(kate, newArrayList(lab2MembershipRequests).get(0).requestId, "Because I said so!");
        Set<Long> labIds = getUserLabIds(user);

        assertTrue(!labIds.contains(lab2));
    }

    @Test
    public void testUserIsNotOperatorAfterLeavingLab() throws URISyntaxException {
        final long kate = uc.createKateAndLab2();
        uc.addKateToLab3();
        final long lab2 = uc.getLab2();
        final long lab3 = uc.getLab3();
        final long lab3HeadId = uc.createPaul();
        userManagement.updatePersonAndSendEmail(kate, Data.KATE_INFO, newHashSet(lab2, lab3), DEFAULT_URL_PROVIDER);
        final Set<Long> labsOfKate = getUserLabIds(kate);
        assertTrue(labsOfKate.contains(lab2) && labsOfKate.contains(lab3));
        final long instrumentId = createInstrumentAndApproveIfNeeded(kate, lab3);
        final Set<Long> operatorsOfInstrument = getOperatorsOfInstrument(instrumentId, lab3HeadId);
        assertTrue(operatorsOfInstrument.contains(kate));

        userManagement.updatePersonAndSendEmail(kate, Data.KATE_INFO, newHashSet(lab2), DEFAULT_URL_PROVIDER);

        final Set<Long> operatorsAfterKateRemoval = getOperatorsOfInstrument(instrumentId, lab3HeadId);
        assertFalse(operatorsAfterKateRemoval.contains(kate));
    }

    @Test
    public void testVerificationLinkSentDateBeingResetAfterEmailResending() {
        final long jake = uc.getJake();
        userManagement.resendActivationEmail(jake, "/url");
        final UserDetails jakeDetails = securityHelper.getUserDetails(jake);

        final Duration linkAge = Duration.between(jakeDetails.emailVerificationSentOnDate.toInstant(), Instant.now());
        final long ageInSeconds = linkAge.getSeconds();

        // check that email verification link is fresh (less than a minute)
        assertTrue(ageInSeconds <= 60);
    }

    @Test
    public void testPasswordResetSentDateBeingResetPasswordResetEmailResending() {
        final long jake = uc.getJake();
        userManagement.sendPasswordRecoveryInstructions(jake, "/url");
        final UserDetails jakeDetails = securityHelper.getUserDetails(jake);

        final Duration linkAge = Duration.between(jakeDetails.passwordResetSentOnDate.toInstant(), Instant.now());
        final long ageInSeconds = linkAge.getSeconds();

        // check that password reset link is fresh (less than a minute)
        assertTrue(ageInSeconds <= 60);
    }

    @Test
    public void testUserCanBeLocked() {
        final long jake = uc.getJake();

        final UserDetails jakeBeforeLock = securityHelper.getUserDetails(jake);
        assertFalse(jakeBeforeLock.locked);

        userManagement.lockUser(jake);
        final UserDetails jakeAfterLock = securityHelper.getUserDetails(jake);
        assertTrue(jakeAfterLock.locked);
    }

    @Test
    public void testUserCanBeUnlocked() {
        final long jake = uc.getJake();
        userManagement.lockUser(jake);
        final UserDetails jakeBeforeUnlock = securityHelper.getUserDetails(jake);
        assertTrue(jakeBeforeUnlock.locked);

        userManagement.unlockUser(jake);
        final UserDetails jakeAfterUnlock = securityHelper.getUserDetails(jake);
        assertFalse(jakeAfterUnlock.locked);
    }

    @Test
    public void testThatUnsuccessfulLoginAttemptsCounterResets() {
        final long jake = uc.getJake();

        userManagement.logUnsuccessfulLoginAttempt(jake);
        userManagement.logUnsuccessfulLoginAttempt(jake);
        userManagement.resetUnsuccessfulLoginAttempts(jake);
        userManagement.logUnsuccessfulLoginAttempt(jake);
        userManagement.logUnsuccessfulLoginAttempt(jake);

        final UserDetails userDetails = securityHelper.getUserDetails(jake);

        assertFalse(userDetails.locked);
    }

    @Test
    public void testThatUserBeingLockedAfterMaxUnsuccessfulLoginAttempts() {
        final long jake = uc.getJake();

        userManagement.logUnsuccessfulLoginAttempt(jake);
        userManagement.logUnsuccessfulLoginAttempt(jake);
        userManagement.logUnsuccessfulLoginAttempt(jake);
        userManagement.resetUnsuccessfulLoginAttempts(jake);

        final UserDetails userDetails = securityHelper.getUserDetails(jake);

        assertTrue(userDetails.locked);
    }

    private Set<Long> getOperatorsOfInstrument(long instrumentId, long actor) {
        final ImmutableSortedSet<SharedPerson> operators = detailsReader.readInstrument(actor, instrumentId).operators;
        return operators.stream().map(sharedPerson -> sharedPerson.id).collect(Collectors.toSet());

    }

    private Set<Long> getUserLabIds(Long user) {
        final ImmutableSet<LabLineTemplate> userLabs = dashboardReader.readUserLabs(user);
        return userLabs.stream().map(input -> input.id).collect(Collectors.toSet());
    }

}
