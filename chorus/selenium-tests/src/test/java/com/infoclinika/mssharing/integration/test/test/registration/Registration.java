package com.infoclinika.mssharing.integration.test.test.registration;

import com.infoclinika.mssharing.integration.test.data.EmailFolder;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.CleanEmailBoxBefore;
import com.infoclinika.mssharing.integration.test.stepdefinitions.*;
import com.infoclinika.mssharing.integration.test.utils.EmailService;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Sergii Moroz
 */
public class Registration extends BaseTest {

    @Test(dataProvider = "registerNewUser", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore
    public void registerNewUser(UserData user, String successAlert) {
        loginPageSteps.registerNewUser(user);
        assertEquals(loginPageSteps.getSuccessAlertText(), successAlert);
        dashboardPageSteps = loginPageSteps.login(user);
        assertTrue(dashboardPageSteps.getHeader().isUserLoggedIn(user.getFullName()),
                "User is not logged in after registration");
    }

    @Test(dataProvider = "registerNewUserWithLab", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore(folderName = {EmailFolder.VERIFY_EMAIL, EmailFolder.LAB_MEMBERSHIP_APPROVED})
    public void registerNewUserWithLab(UserData user, String laboratory, String successAlertText, UserData labHead) {
        loginPageSteps.registerNewUser(user, laboratory);
        assertEquals(loginPageSteps.getSuccessAlertText(), successAlertText);
        dashboardPageSteps = loginPageSteps.login(labHead).pressOkInBillingNotificationDialog();
        InboxListSteps inboxListSteps = dashboardPageSteps.getSidebarMenuSteps()
                .selectInbox();

        assertTrue(inboxListSteps.isLabMembershipRequestItemPresent(user, laboratory),
                "Laboratory request does not appear in the Lab Head's inbox");

        inboxListSteps.pressApproveLabMembershipButton(user, laboratory);
        SidebarMenuSteps sidebarMenuSteps = dashboardPageSteps
                .getHeader()
                .logout()
                .pressSignInButton()
                .login(user)
                .getSidebarMenuSteps();

        assertTrue(sidebarMenuSteps.isInstrumentsPresent(),
                "Laboratory is not appeared for user after Lab Head has approved user's membership");
        assertTrue(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.LAB_MEMBERSHIP_APPROVED).size() == 1,
                "Email about Lab Membership approving is not received by user");

    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithoutFirstName", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithoutFirstName(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps.openRegistrationForm()
                .fillForm(userData)
                .clearFirstName();
        assertEquals(registrationPageSteps.getFirstNameAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, First Name field is not filled in, though");
    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithoutLastName", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithoutLastName(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps.openRegistrationForm()
                .fillForm(userData)
                .clearLastName();
        assertEquals(registrationPageSteps.getLastNameAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, Last Name field is not filled in, though");
    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithoutEmail", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithoutEmail(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps.openRegistrationForm()
                .fillForm(userData)
                .clearEmail();
        assertEquals(registrationPageSteps.getEmailAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, Email field is not filled in, though");
    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithoutEmailConfirmation", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithoutEmailConfirmation(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps.openRegistrationForm()
                .fillForm(userData)
                .clearConfirmEmail();
        assertEquals(registrationPageSteps.getConfirmEmailAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, Confirm Email field is not filled in, though");
    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithoutPassword", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithoutPassword(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps.openRegistrationForm()
                .fillForm(userData)
                .clearPassword();
        assertEquals(registrationPageSteps.getPasswordAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, Password field is not filled in, though");
    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithoutPasswordConfirmation", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithoutPasswordConfirmation(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps.openRegistrationForm()
                .fillForm(userData)
                .clearConfirmPassword();
        assertEquals(registrationPageSteps.getConfirmPasswordAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, Confirm Password field is not filled in, though");
    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithDifferentEmailAndEmailConfirmation", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithDifferentEmailAndEmailConfirmation(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps
                .openRegistrationForm()
                .fillForm(userData);
        assertEquals(registrationPageSteps.getConfirmEmailAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, values in the Email And Confirm Email fields are not match, though");
    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithDifferentPasswordAndPasswordConfirmation", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithDifferentPasswordAndPasswordConfirmation(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps
                .openRegistrationForm()
                .fillForm(userData);
        assertEquals(registrationPageSteps.getConfirmPasswordAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, values in the Password And Confirm Password fields are not match, though");
    }

    @Test(dataProvider = "shouldNotAllowToCreateAccountWithAlreadyRegisteredEmail", dataProviderClass = RegistrationDataProvider.class, groups = {"staging"})
    public void shouldNotAllowToCreateAccountWithAlreadyRegisteredEmail(UserData userData, String alert) {
        RegistrationPageSteps registrationPageSteps = loginPageSteps
                .openRegistrationForm()
                .fillForm(userData);
        assertEquals(registrationPageSteps.getEmailAlert(), alert,
                "Actual alert message does not match with the expected");
        assertFalse(registrationPageSteps.isCreateButtonEnabled(),
                "'Create' button is enabled, Email field is filled in with already registered email, though");
    }
}
