package com.infoclinika.mssharing.integration.test.test.laboratorycreation;

import com.infoclinika.mssharing.integration.test.data.EmailFolder;
import com.infoclinika.mssharing.integration.test.data.LaboratoryData;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.CleanEmailBoxBefore;
import com.infoclinika.mssharing.integration.test.stepdefinitions.InboxListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.LaboratoryCreationRequestSteps;
import com.infoclinika.mssharing.integration.test.utils.EmailService;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.navigateToChorus;
import static org.testng.Assert.*;

/**
 * @author Alexander Orlov
 */
public class LaboratoryCreationRequest extends BaseTest {

    @Test(dataProvider = "labCreationRequestApprovingWithAlreadyRegisteredLabHead",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore(folderName = {EmailFolder.LAB_CREATION_APPROVED})
    public void labCreationRequestApprovingWithAlreadyRegisteredLabHead(LaboratoryData labData, UserData admin, UserData userData, String successAlert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps = loginPageSteps
                .registerNewUser(userData)
                .openRegistrationForm()
                .openLabRequestPage()
                .fillForm(labData);
        assertTrue(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User unable to send lab request, all required fields are filled in with valid info, though.");
        laboratoryCreationRequestSteps.pressSendButton();
        assertEquals(laboratoryCreationRequestSteps.getAlertSuccessText(), successAlert);
        navigateToChorus();
        InboxListSteps inboxListSteps = loginPageSteps
                .login(admin)
                .pressOkInBillingNotificationDialog()
                .getSidebarMenuSteps()
                .selectInbox();
        assertTrue(inboxListSteps.isLabRequestItemPresent(userData, labData),
                "Lab Request is not appeared in admin inbox");
        inboxListSteps.approveLabCreationRequest(userData, labData);
        assertTrue(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.LAB_CREATION_APPROVED).size() == 1,
                "Email about Lab Request approving is not received by user");
    }

    @Test(dataProvider = "labCreationRequestApprovingWithNotRegisteredLabHead",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore(folderName = {EmailFolder.LAB_CREATION_APPROVED, EmailFolder.LAB_CREATION_PASSWORD})
    public void labCreationRequestApprovingWithNotRegisteredLabHead(LaboratoryData labData, UserData admin, UserData userData, String successAlert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps =
                loginPageSteps
                        .openRegistrationForm()
                        .openLabRequestPage()
                        .fillForm(labData);
        assertTrue(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User unable to send lab request, all required fields are filled in with valid info, though.");
        laboratoryCreationRequestSteps.pressSendButton();
        assertEquals(laboratoryCreationRequestSteps.getAlertSuccessText(), successAlert);
        navigateToChorus();
        InboxListSteps inboxListSteps = loginPageSteps
                .login(admin)
                .pressOkInBillingNotificationDialog()
                .getSidebarMenuSteps()
                .selectInbox();
        assertTrue(inboxListSteps.isLabRequestItemPresent(userData, labData),
                "Lab Request is not appeared in admin inbox");
        inboxListSteps.approveLabCreationRequest(userData, labData);
        assertTrue(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.LAB_CREATION_APPROVED).size() == 1,
                "Email about Lab Request approving is not received by user");
        assertTrue(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.LAB_CREATION_PASSWORD).size() == 1,
                "Generated password is not received to not registered Lab Head after Lab Request approving");
    }

    @Test(dataProvider = "laboratoryCreationRequestRefusing",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore(folderName = {EmailFolder.LAB_CREATION_REFUSED})
    public void laboratoryCreationRequestRefusing(LaboratoryData labData, UserData admin, UserData userData, String successAlert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps =
                loginPageSteps
                        .openRegistrationForm()
                        .openLabRequestPage()
                        .fillForm(labData);
        assertTrue(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User unable to send lab request, all required fields are filled in with valid info, though.");
        laboratoryCreationRequestSteps.pressSendButton();
        assertEquals(laboratoryCreationRequestSteps.getAlertSuccessText(), successAlert);
        navigateToChorus();
        InboxListSteps inboxListSteps = loginPageSteps.login(admin).pressOkInBillingNotificationDialog().getSidebarMenuSteps().selectInbox();
        assertTrue(inboxListSteps.isLabRequestItemPresent(userData, labData),
                "Lab Request is not appeared in admin inbox");
        inboxListSteps.refuseLabCreationRequest(userData, labData);
        assertTrue(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.LAB_CREATION_REFUSED).size() == 1,
                "Email about Lab Request rejecting is not received by user");
    }

    @Test(dataProvider = "shouldNotAllowToCreateLabWithoutInstitutionURL",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateLabWithoutInstitutionURL(LaboratoryData labData, String alert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps =
                loginPageSteps
                        .openRegistrationForm()
                        .openLabRequestPage()
                        .fillForm(labData)
                        .clearInstitutionUrl();
        assertEquals(laboratoryCreationRequestSteps.getInstitutionURLAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User is able to create lab request without institution URL");
    }

    @Test(dataProvider = "shouldNotAllowToCreateLabWithoutLabName",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateLabWithoutLabName(LaboratoryData labData, String alert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps =
                loginPageSteps
                        .openRegistrationForm()
                        .openLabRequestPage()
                        .fillForm(labData)
                        .clearLaboratoryName();
        assertEquals(laboratoryCreationRequestSteps.getLabNameAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User is able to create lab request without Lab Name");
    }

    @Test(dataProvider = "shouldNotAllowToCreateLabWithoutContactEmail",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateLabWithoutContactEmail(LaboratoryData labData, String alert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps =
                loginPageSteps
                        .openRegistrationForm()
                        .openLabRequestPage();
        laboratoryCreationRequestSteps.fillForm(labData);
        laboratoryCreationRequestSteps.clearContactEmail();
        assertEquals(laboratoryCreationRequestSteps.getContactEmailAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User is able to create lab request without contact email");
    }

    @Test(dataProvider = "shouldNotAllowToCreateLabWithoutLabHeadFirstName",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateLabWithoutLabHeadFirstName(LaboratoryData labData, String alert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps =
                loginPageSteps
                        .openRegistrationForm()
                        .openLabRequestPage();
        laboratoryCreationRequestSteps.fillForm(labData);
        laboratoryCreationRequestSteps.clearLabHeadFirstName();
        assertEquals(laboratoryCreationRequestSteps.getLabHeadFirstNameAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User is able to create lab request without Lab Head First Name");
    }

    @Test(dataProvider = "shouldNotAllowToCreateLabWithoutLabHeadLastName",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateLabWithoutLabHeadLastName(LaboratoryData labData, String alert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps =
                loginPageSteps
                        .openRegistrationForm()
                        .openLabRequestPage();
        laboratoryCreationRequestSteps.fillForm(labData);
        laboratoryCreationRequestSteps.clearLabHeadLastName();
        assertEquals(laboratoryCreationRequestSteps.getLabHeadLastNameAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User is able to create lab request without Lab Head Last Name");
    }

    @Test(dataProvider = "shouldNotAllowToCreateLabWithoutLabHeadEmail",
            dataProviderClass = LaboratoryCreationRequestDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateLabWithoutLabHeadEmail(LaboratoryData labData, String alert) {
        LaboratoryCreationRequestSteps laboratoryCreationRequestSteps =
                loginPageSteps
                        .openRegistrationForm()
                        .openLabRequestPage();
        laboratoryCreationRequestSteps.fillForm(labData);
        laboratoryCreationRequestSteps.clearLabHeadEmail();
        assertEquals(laboratoryCreationRequestSteps.getLabHeadEmailAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(laboratoryCreationRequestSteps.isCreationEnabled(),
                "User is able to create lab request without Lab Head Email");
    }

}
