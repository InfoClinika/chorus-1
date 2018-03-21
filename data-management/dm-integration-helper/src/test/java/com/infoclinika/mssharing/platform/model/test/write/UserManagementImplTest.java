package com.infoclinika.mssharing.platform.model.test.write;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.helper.SecurityHelperTemplate.UserDetails;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import com.infoclinika.mssharing.platform.model.test.helper.AbstractTest;
import com.infoclinika.mssharing.platform.model.testing.helper.Data;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ruslan Duboveckij
 */
public class UserManagementImplTest extends AbstractTest {

    private final DefaultUserInfo janos = new DefaultUserInfo("Janos", "Slint", "js@email.cml", "pwd", "/url/janos");
    private UserManagementTemplate.LabMembershipConfirmationUrlProvider DEFAULT_URL_PROVIDER = new UserManagementTemplate.LabMembershipConfirmationUrlProvider() {
        @Override
        public String getUrl(long user, long lab, long requestId, UserManagementTemplate.LabMembershipRequestActions action) throws URISyntaxException {
            return "";
        }
    };

    @Test
    public void testUpdatePersonAndSendEmailTestingLeaveLab() throws Exception {
        long kate = uc.createKateAndLab2();
        uc.addKateToLab3();
        uc.addKateToLab4();
        final long lab2Head = uc.getLab2();
        final long lab3Live = uc.getLab3();
        final long lab4Deleted = uc.getLab4();
        userManagement.updatePersonAndSendEmail(kate, Data.KATE_INFO, newHashSet(lab3Live), DEFAULT_URL_PROVIDER);
        //noinspection unchecked
        List<Long> checkLabs = newArrayList(Collections2.transform(labReader.readUserLabs(kate),
                new Function<LabReaderTemplate.LabLineTemplate, Long>() {
                    @Override
                    public Long apply(LabReaderTemplate.LabLineTemplate labLine) {
                        return labLine.id;
                    }
                }));

        Assert.assertTrue(checkLabs.contains(lab2Head), "Could not remove user from lab");
        Assert.assertTrue(checkLabs.contains(lab3Live), "Could not remove user from lab");
        Assert.assertTrue(!checkLabs.contains(lab4Deleted), "Could not remove user from lab");
    }

    @Test
    public void testCreateUser() {
        Long createdPerson = userManagement.createPerson(janos.toPersonInfo(), janos.password, new HashSet<Long>(), janos.verificationUrl);

        UserManagementTemplate.PersonInfo personInfo = userTestHelper.readPersonInfo(createdPerson);
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

        UserManagementTemplate.PersonInfo personInfo = userTestHelper.readPersonInfo(createdPerson);
        Assert.assertEquals(janos.toPersonInfo(), personInfo);
    }

    @Test
    public void testCreateUserWithGeneratedPassword() {
        long createdUser = userManagement.createUserWithGeneratedPassword(janos.toPersonInfo(), janos.verificationUrl);
        UserManagementTemplate.PersonInfo personInfo = userTestHelper.readPersonInfo(createdUser);
        Assert.assertEquals(janos.toPersonInfo(), personInfo);
    }

    @Test
    public void testUpdateUserData() {
        long john = uc.createJohnWithoutLab();
        uc.createLab2();
        Long lab2 = uc.getLab2();
        userManagement.updatePerson(john, janos.toPersonInfo(), newHashSet(lab2));

        UserManagementTemplate.PersonInfo johnInfoAfterUpdate = userTestHelper.readPersonInfo(john);
        Assert.assertEquals(janos.toPersonInfo(), johnInfoAfterUpdate);
    }

    @Test
    public void testUserCreationWhenInviteUser() {
        String invitationLink = userManagement.inviteUser(admin(), janos.email, getInvitationLink());
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
        userManagement.inviteUser(admin(), janos.email, getInvitationLink());
        Long savedUser = userManagement.saveInvited(janos.toPersonInfo(), passwordHash, newHashSet(lab2), janos.verificationUrl, DEFAULT_URL_PROVIDER);

        UserManagementTemplate.PersonInfo savedUserInfo = userTestHelper.readPersonInfo(savedUser);
        assertEquals(savedUserInfo, janos.toPersonInfo());
    }

    private String getInvitationLink() {
        return UUID.randomUUID().toString();
    }

    @Test
    public void testInvitationLinkIsDeletedAfterSavingInvitedUser() throws URISyntaxException {
        final String passwordHash = generateString();
        uc.createLab2();
        Long lab2 = uc.getLab2();
        String invitationLink = userManagement.inviteUser(admin(), janos.email, getInvitationLink());
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

    private Set<Long> getUserLabIds(Long user) {
        //noinspection unchecked
        return from(labReader.readUserLabs(user)).transform(new Function<LabReaderTemplate.LabLineTemplate, Long>() {
            @Override
            public Long apply(LabReaderTemplate.LabLineTemplate input) {
                return input.id;
            }
        }).toSet();
    }

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


}
