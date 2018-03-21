/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.test.organization;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.helper.Data;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.RequestsReader;
import com.infoclinika.mssharing.model.read.UserReader;
import com.infoclinika.mssharing.model.write.LabManagement;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.RequestAlreadyHandledException;
import com.infoclinika.mssharing.platform.model.helper.RegistrationHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRequestRepositoryTemplate;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
public class ManagingLabsTest extends AbstractTest {
    //When application started there is no labs available
    @Test
    public void testLabsListInitiallyEmpty() {
        assertEquals(Iterables.size(registrationHelper.availableLabs()), 0);
    }

    //Before activation it doesn't appear in labs list
    @Test
    public void testLabsIsntAvailableInListIfNotActivated() {
        uc.requestLab3creation();
        assertEquals(Iterables.size(registrationHelper.availableLabs()), 0);
    }

    @Test
    public void testLabsIsntAvailableAtDashboardListIfNotActivated() {
        uc.requestLab3creation();
        assertEquals(Iterables.size(dashboardReader.readAllLabs(admin())), 0);
    }

    @Test
    public void testRequestedLabPlacedInRequests() {
        final ImmutableSortedSet<RequestsReader.LabRequest> previousRequests = requestsReader.myLabsInbox(admin());
        for (RequestsReader.LabRequest previousRequest : previousRequests) {
            labManagement.rejectLabCreation(admin(), previousRequest.labRequest, "");
        }

        final long requestId = labManagement.requestLabCreation(Data.LAB_3_DATA, "a.a@com");
        final ImmutableSortedSet<RequestsReader.LabRequest> labRequests = requestsReader.myLabsInbox(admin());
        assertEquals(labRequests.size(), 1);
        final RequestsReader.LabRequest request = labRequests.first();
        assertEquals(request.labRequest, requestId);
        assertEquals(request.contactEmail, "a.a@com");
        assertEquals(request.labName, Data.LAB_3);
        assertNotNull(request.sent);
    }

    @Test
    public void testAfterLabRequestActivationRequestIsntPlacedAnymore() {
        final long request = uc.requestLab3creation();
        labManagement.rejectLabCreation(admin(), request, generateString());
        assertTrue(requestsReader.myLabsInbox(admin()).isEmpty());
    }

    @Test
    public void testRequesterReceivesEmailOnLabActivation() {
        final String requesterEmail = "a.a@com";
        final long request = labManagement.requestLabCreation(Data.LAB_3_DATA, requesterEmail);
        final long lab = labManagement.confirmLabCreation(admin(), request);

        verify(notificator()).labCreationApproved(eq(requesterEmail), eq(lab));
    }

    @Test
    public void testRequesterReceivesEmailOnLabCreationRejection() {
        final String requesterEmail = "a.a@com";
        final String rejectComment = generateString();
        final long lab = labManagement.requestLabCreation(Data.LAB_3_DATA, requesterEmail);
        labManagement.rejectLabCreation(admin(), lab, rejectComment);

        verify(notificator()).labCreationRejected(eq(requesterEmail), eq(rejectComment), eq(Data.LAB_3_DATA.labName));
    }

    @Test
    public void testFirstOneWinsRejectOnLabsRequestSecondGetsNotification() {
        final long request = uc.requestLab3creation();

        labManagement.rejectLabCreation(admin(), request, generateString());
        try {
            labManagement.confirmLabCreation(otherAdmin(), request);
            fail("Should not be able to confirum the lab creation");
        } catch (LabManagement.StaleLabCreationRequestException ignored) {
            //goes as planned
        }
        verify(notificator()).staleOnLabRequest(eq(otherAdmin()), eq(request));
    }

    @Test
    public void testFirstOneWinsOnApproveLabsRequestSecondGetsNotification() {
        final long request = uc.requestLab3creation();

        final long lab = labManagement.confirmLabCreation(otherAdmin(), request);
        labManagement.rejectLabCreation(admin(), request, generateString());

        assertTrue(labIsActive(lab));
        verify(notificator()).staleOnLabRequest(eq(admin()), eq(request));
    }

    @Test
    public void testLabRequestsSortedBySentDate() throws InterruptedException {
        uc.requestLab3creation();
        Thread.sleep(1000);
        uc.requestLab2creation();

        final ImmutableSortedSet<RequestsReader.LabRequest> labRequests = requestsReader.myLabsInbox(admin());
        assertTrue(labRequests.first().sent.after(labRequests.last().sent));
    }

    @Test
    public void testOnlyAdminsCanSeeLabRequests() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        uc.requestLab2creation();
        assertTrue(requestsReader.myLabsInbox(bob).isEmpty());
    }

    //Admins can edit lab details before activation
    @Test
    public void testEditingByAdmin() {
        final long request = labManagement.requestLabCreation(Data.LAB_2_DATA, "some");
        final String newLabName = "other name";
        labManagement.editLabRequestInfo(admin(), request, new LabManagementTemplate.LabInfoTemplate(Data.HARVARD_URL, Data.L_PAUL_INFO, newLabName));

        labManagement.confirmLabCreation(admin(), request);
        assertTrue(any(registrationHelper.availableLabs(), new Predicate<RegistrationHelperTemplate.LabItem>() {
            @Override
            public boolean apply(RegistrationHelperTemplate.LabItem input) {
                return input.name.equals(newLabName);
            }
        }));
    }

    //Only admins can perform such editing.
    @Test(expectedExceptions = AccessDenied.class)
    public void testEditingByRegularUser() {
        final long bob = uc.createLab3AndBob();
        final long request = labManagement.requestLabCreation(Data.LAB_2_DATA, "some");
        labManagement.editLabRequestInfo(bob, request, new LabManagementTemplate.LabInfoTemplate(Data.HARVARD_URL, Data.L_PAUL_INFO, "other name"));
    }

    //Lab creation requested
    @Test(expectedExceptions = AccessDenied.class)
    public void testOnlyAdminsCanActivateLabs() {
        final long request = uc.requestLab2creation();
        final long bob = uc.createLab3AndBob();
        labManagement.confirmLabCreation(bob, request);
    }

    @Test
    public void testAdminCanRejectLabActivation() {
        final long request = uc.requestLab2creation();
        labManagement.rejectLabCreation(admin(), request, generateString());
        assertEquals(Iterables.size(registrationHelper.availableLabs()), 0);
    }

    @Test
    public void testNewLabHeadIsOperatorOfAllInstrumentsInLab() {
        final long paul = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3());
        assertEquals(size(dashboardReader.readInstrumentsWhereUserIsOperator(paul)), 1);
        labManagement.editLab(admin(), uc.getLab3(), new LabManagementTemplate.LabInfoTemplate(generateString(), new UserManagementTemplate.PersonInfo(generateString(), generateString(), "jjj@j.com"), generateString()));
        assertEquals(size(dashboardReader.readInstrumentsWhereUserIsOperator(joe)), 1);
    }

    @Test(enabled = false) //TODO[Alexander Serebriyan]: investigate failure
    public void testEditLab() {
        final String newLabName = "new brave lab";
        final String newInstitutionUrl = "new brave url";
        long john = uc.createJohnWithoutLab();
        PersonInfo labHeadInfo = userReader.shortForm(john).toPersonInfo();

        LabReaderTemplate.LabLineTemplate originalLab = dashboardReader.readLab(uc.createLab4());
        LabManagementTemplate.LabInfoTemplate labInfoTemplate = new LabManagementTemplate.LabInfoTemplate(newInstitutionUrl, labHeadInfo, newLabName);
        labManagement.editLab(admin(), originalLab.id, labInfoTemplate);

        LabReaderTemplate.LabLineTemplate labAfterEdit = dashboardReader.readLab(originalLab.id);

        assertEquals(labAfterEdit.name, newLabName);
        assertEquals(labAfterEdit.institutionUrl, newInstitutionUrl);
        assertEquals(labAfterEdit.labHead, john);
    }

    @Test
    public void testCreateLab() {
        final String newLabName = "new brave lab";
        final String newInstitutionUrl = "new brave url";
        final String contactEmail = "lab@mail.com";
        long john = uc.createJohnWithoutLab();
        PersonInfo labHeadInfo = userReader.shortForm(john).toPersonInfo();
        LabManagementTemplate.LabInfoTemplate labInfoTemplate = new LabManagementTemplate.LabInfoTemplate(newInstitutionUrl, labHeadInfo, newLabName);
        Long lab = labManagement.createLab(admin(), labInfoTemplate, contactEmail);

        LabReaderTemplate.LabLineTemplate createdLab = dashboardReader.readLab(lab);

        assertEquals(createdLab.name, newLabName);
        assertEquals(createdLab.institutionUrl, newInstitutionUrl);
        assertEquals(createdLab.labHead, john);
    }

    //Lab creation confirmed
    //Test we can create system users in activated labs

    @Test
    public void testLabsAvailableOnDashboard() {
        final long lab3 = uc.createLab3();
        final ImmutableSet<LabReaderTemplate.LabLineTemplate> all = dashboardReader.readAllLabs(admin());
        assertEquals(all.size(), 1);
        final LabReaderTemplate.LabLineTemplate lab = getFirst(all, null);
        assertEquals(lab.name, Data.LAB_3);
        assertEquals(lab.id, lab3);
    }

    @Test(dependsOnMethods = "testLabsAvailableOnDashboard", expectedExceptions = AccessDenied.class)
    public void testLabsAvailableOnDashboardOnlyForAdmins() {
        final long lab3 = uc.createLab3();
        final long poll = uc.createPaul();
        dashboardReader.readAllLabs(poll);
    }

    @Test
    public void testLabsAvailableInListAfterActivation() {
        final long request = uc.requestLab3creation();
        labManagement.confirmLabCreation(admin(), request);
        assertEquals(Iterables.size(registrationHelper.availableLabs()), 1);
    }

    @Test
    public void testReadLabDetails() {
        final long lab3 = uc.createLab3();
        final DetailsReaderTemplate.LabItemTemplate details = detailsReader.readLab(admin(), lab3);
        assertEquals(details.id, lab3);
        checkNotNull(details.modified);
    }

    @Test(dependsOnMethods = "testReadLabDetails", expectedExceptions = AccessDenied.class)
    public void testOnlyAdminsCanReadLabDetails() {
        final long lab3 = uc.createLab3();
        final long poll = uc.createPaul();
        detailsReader.readLab(poll, lab3);
    }

    @Test
    public void testRemovingUserFromLaboratory() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();//labHead
        assertEquals(dashboardReader.readUsersByLab(poll, uc.getLab3()).size(), 2);
        labHeadManagement.removeUserFromLab(poll, uc.getLab3(), bob);
        final ImmutableSet<DashboardReader.UserLine> usersInLabAfterRemoving = dashboardReader.readUsersByLab(poll, uc.getLab3());
        assertEquals(usersInLabAfterRemoving.size(), 1);
        assertEquals(usersInLabAfterRemoving.iterator().next().labHead, true);
    }

    @Test(dependsOnMethods = "testRemovingUserFromLaboratory", expectedExceptions = AccessDenied.class)
    public void testOnlyLabHeadCanRemoveUserFromLaboratory() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();     //labHead
        assertEquals(dashboardReader.readUsersByLab(bob, uc.getLab3()).size(), 2);
        labHeadManagement.removeUserFromLab(bob, uc.getLab3(), poll);
    }

    @Test(dependsOnMethods = "testOnlyLabHeadCanRemoveUserFromLaboratory")
    public void testRemoveUserFromLaboratoryIfHeHasNoLabProjectsAndExperiments() {
        setProteinSearch(true);
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long project = createPublicProject(bob);
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long experiment = createExperiment(bob, project);
        //attachFileToExperiment(bob,experiment);
        labHeadManagement.removeUserFromLab(poll, uc.getLab3(), bob);
    }

    @Test
    public void testGeneratedPasswordIsSentToNewlyCreatedLabHead() {
        PersonInfo labHead = new PersonInfo("New", "Head", "newhead@nasa.gov");
        LabManagementTemplate.LabInfoTemplate labInfo = new LabManagementTemplate.LabInfoTemplate("http://nasa.gov", labHead, "Nasa Lab");
        labManagement.createLab(admin(), labInfo, "someguy@nasa.gov");
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificator()).sendGeneratedPassword(userIdCaptor.capture(), passwordCaptor.capture());
        UserReader.UserShortForm user = userReader.shortForm(userIdCaptor.getValue());
        assertEquals("newhead@nasa.gov", user.email);
        String password = passwordCaptor.getValue();
        assertTrue(password.length() > 5 && password.length() < 15, "Password length between 5 and 15, but was " + password.length());
    }

    @Test
    public void testHandleApproveLabMembershipRequest() {
        uc.createPaul();                           //create lab3 & Paul - lab3's head
        final long john = uc.createJohnWithoutLab();                // crate john - the user with no lab
        UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>> repo = getUserLabMemebershipRequestRepository();

        assertEquals(repo.findPendingByUser(john).size(), 0);              //no requests
        assertEquals(securityHelper.getUserDetails(john).labs.size(), 0);  //no labs
        uc.requestJohnLab3Membership();                               //create request from John to lab3
        assertEquals(repo.findPendingByUser(john).size(), 1);         //one request
        long requestId = repo.findPendingByUser(john).get(0).getId();
        userManagement.handleLabMembershipRequest(uc.getLab3(), requestId, uc.getApprove());  //handle request
        assertEquals(repo.findPendingByUser(john).size(), 0);                             //no requests
        assertEquals(securityHelper.getUserDetails(john).labs.size(), 1);                 //one lab
    }

    @Test
    public void testHandleRefuseLabMembershipRequest() {
        uc.createPaul();
        final long john = uc.createJohnWithoutLab();
        UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>> repo = getUserLabMemebershipRequestRepository();

        uc.requestJohnLab3Membership();
        assertEquals(repo.findPendingByUser(john).size(), 1);
        long requestId = repo.findPendingByUser(john).get(0).getId();
        userManagement.handleLabMembershipRequest(uc.getLab3(), requestId, uc.getRefuse());
        assertEquals(repo.findPendingByUser(john).size(), 0);
        assertEquals(securityHelper.getUserDetails(john).labs.size(), 0);
    }

    @Test(expectedExceptions = RequestAlreadyHandledException.class)
    public void testHandleApproveLabMembershipRequestWhichAlreadyHandled() {
        uc.createPaul();
        final long john = uc.createJohnWithoutLab();
        UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>> repo = getUserLabMemebershipRequestRepository();

        uc.requestJohnLab3Membership();
        assertEquals(repo.findPendingByUser(john).size(), 1);
        long requestId = repo.findPendingByUser(john).get(0).getId();
        userManagement.handleLabMembershipRequest(uc.getLab3(), requestId, uc.getApprove());
        userManagement.handleLabMembershipRequest(uc.getLab3(), requestId, uc.getApprove());
        fail("Exception was expected");
    }

    @Test(expectedExceptions = ObjectNotFoundException.class)
    public void testHandleWrongLabMembershipRequest() {
        uc.createPaul();
        final long john = uc.createJohnWithoutLab();
        UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>> repo = getUserLabMemebershipRequestRepository();

        uc.requestJohnLab3Membership();
        assertEquals(repo.findPendingByUser(john).size(), 1);
        long requestId = repo.findPendingByUser(john).get(0).getId();
        userManagement.handleLabMembershipRequest(uc.getLab3(), requestId, null);
        fail("Exception was expected");
    }

    @Test
    public void testCheckRequest(){
        uc.createPaul();
        final long john = uc.createJohnWithoutLab();
        uc.requestJohnLab3Membership();
        UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>> repo = getUserLabMemebershipRequestRepository();
        long requestId = repo.findPendingByUser(john).get(0).getId();
        userManagement.checkRequest(requestId);
    }

    @Test
    public void testUpdatePersonAndSendEmail() throws URISyntaxException {
        long labHead = uc.createPaul();                             //create lab3 & Paul - lab3's head
        final long john = uc.createJohnWithoutLab();                // crate john - the user with no lab

        UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>> repo = getUserLabMemebershipRequestRepository();
        assertEquals(repo.findPendingByUser(john).size(), 0);       //no requests

        final String approveUrl = "testUrl";
        UserManagement.LabMembershipConfirmationUrlProvider urlProvider = new UserManagement.LabMembershipConfirmationUrlProvider() {
            @Override
            public String getUrl(long user, long lab, long requestId, UserManagement.LabMembershipRequestActions action) throws URISyntaxException {
                return approveUrl;
            }
        };

        userManagement.updatePersonAndSendEmail(john, new PersonInfo("Joe", "J", "jjj@j.com"), ImmutableSet.of(uc.getLab3()), urlProvider);
        Notifier notifier = notificator();
        verify(notifier).sendLabMembershipRequest(eq(labHead), Matchers.any(String.class), eq(john), eq(approveUrl), Matchers.any(String.class));

        assertEquals(repo.findPendingByUser(john).size(), 1);       //one request
    }

    @Test
    public void testUserIsLabHead() {
        long kateAndLab2 = uc.createKateAndLab2();
        long johnWithoutLab = uc.createJohnWithoutLab();
        boolean isKateLabHead = labHeadManagement.isLabHead(kateAndLab2);
        boolean isJohnLabHead = labHeadManagement.isLabHead(johnWithoutLab);
        assertTrue(isKateLabHead);
        assertFalse(isJohnLabHead);
    }

    @Test
    public void testFindingLabsForLabHead() {
        long john = uc.createJohnWithoutLab();
        PersonInfo labHeadInfo = userReader.shortForm(john).toPersonInfo();
        LabManagementTemplate.LabInfoTemplate labOneInfo = new LabManagementTemplate.LabInfoTemplate("lab1", labHeadInfo, "lab1");
        LabManagementTemplate.LabInfoTemplate labTwoInfo = new LabManagementTemplate.LabInfoTemplate("lab2", labHeadInfo, "lab2");
        Long lab1 = labManagement.createLab(admin(), labOneInfo, "email1");
        Long lab2 = labManagement.createLab(admin(), labTwoInfo, "email2");

        Collection<Long> johnLab = labHeadManagement.findLabsForLabHead(john);

        assertTrue(johnLab.size() == 2);
        assertTrue(johnLab.containsAll(newArrayList(lab1, lab2)));
    }

    @Test
    public void testReadingLabByName() {
        String labName = "Hudson Lab";
        long john = uc.createJohnWithoutLab();
        PersonInfo labHeadInfo = userReader.shortForm(john).toPersonInfo();
        LabManagementTemplate.LabInfoTemplate labOneInfo = new LabManagementTemplate.LabInfoTemplate("lab1", labHeadInfo, labName);
        Long createdLabId = labManagement.createLab(admin(), labOneInfo, "email1");

        LabReaderTemplate.LabLineTemplate createdLabInfo = dashboardReader.readLabByName(labName);
        assertTrue(createdLabId.equals(createdLabInfo.id));
    }

    private boolean labIsActive(long lab) {
        try {
            userManagement.createPersonAndApproveMembership(Data.PAUL_INFO, "", lab, null);
            return true;
        } catch (AccessDenied e) {
            return false;
        }
    }
}