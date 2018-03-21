package com.infoclinika.mssharing.integration.test.test;

import com.infoclinika.mssharing.integration.test.data.EmailFolder;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.helper.TestManager;
import com.infoclinika.mssharing.integration.test.stepdefinitions.LoginPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.RegistrationPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.project.ProjectSharingTabSteps;
import com.infoclinika.mssharing.integration.test.utils.EmailService;
import com.infoclinika.mssharing.integration.test.utils.NavigationManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomInt;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Sergii Moroz
 */
public class NewUserTests extends TestManager{
    RegistrationPageSteps registration;
    LoginPageSteps loginPageSteps;
    UserData chorusTester;

    @BeforeMethod
    public void beforeMethod(Method method) {
        startDriver();
        logMethodName(method);
        chorusTesterAtGmail = new UserData.Builder()
                .email("chorus.tester+" + randomInt() + "@gmail.com")
                .password("Password123456")
                .firstName("Chorus")
                .lastName("Tester")
                .build();
        loginPageSteps = new LoginPageSteps();
        EmailService.connectAndRemoveAllMessagesFromFolder(EmailFolder.VERIFY_EMAIL);
        EmailService.connectAndRemoveAllMessagesFromFolder(EmailFolder.LAB_MEMBERSHIP_APPROVED);
    }

    @Test
    public void createRemoveSharingGroupByUserWithNoLab(){
        //issue #864
        //setup
        registration = loginPageSteps.openRegistrationForm();
        String sharingGroupName = randomizeName("Test Sharing Group");
        String personToInvite = SampleData.PERSON_NAME_KARREN_KOE;
        //test
        registration.fillForm(chorusTester)
                .specifyLab(SampleData.LAB_FIRST_CHORUS)
                .pressCreateButton();
        LoginPageSteps loginPageSteps = NavigationManager.navigateByActivationLink();
        dashboardPageSteps = loginPageSteps.login(chorusTester).pressOkInBillingNotificationDialog();
        dashboardPageSteps.createSharingGroup(sharingGroupName, personToInvite);
        assertTrue(dashboardPageSteps.getSharingGroupListSteps()
                .isSharingGroupDisplayed(sharingGroupName),
                "Sharing Group has not appear in list after creating");
        dashboardPageSteps.getSharingGroupListSteps()
                .deleteSharingGroup(sharingGroupName);
        assertFalse(dashboardPageSteps.getSharingGroupListSteps().isSharingGroupDisplayed(sharingGroupName),
                "Sharing Group has appear in list after removing");
    }

    @Test
     public void inviteNewUser(ProjectData projectData){
        //setup
//        ProjectData projectData = new ProjectData();
//        projectData.setLaboratory(SampleData.LAB_FIRST_CHORUS);
//        projectData.setPersonToInvite(chorusTester.getEmail());
        dashboardPageSteps = loginPageSteps.login(pavelKaplinAtGmail).pressOkInBillingNotificationDialog();
        ProjectSharingTabSteps projectSharingTabSteps = dashboardPageSteps.getTopPanelSteps()
                .openCreationMenu()
                .clickCreateProject()
                .specifyName(projectData.getName())
                .specifyArea(projectData.getArea())
                .specifyLab(projectData.getLaboratory())
                .specifyDescription(projectData.getDescription())
                .selectSharingTab()
                .specifyPersonToInvite(projectData.getPersonToInvite());
        //test
        projectSharingTabSteps.pressCreateAndInviteUnregisteredUsers();
        dashboardPageSteps.getHeader()
                .logout();
        RegistrationPageSteps registrationPageSteps = loginPageSteps.openRegistrationFormFromInvite();
        assertTrue(registrationPageSteps.getEmail().equals(chorusTester.getEmail()), "User email isn't predefined");
        assertTrue(registrationPageSteps.getConfirmationEmail().equals(projectData.getPersonToInvite()), "User email isn't predefined");
        registrationPageSteps.specifyFirstName(chorusTester.getFirstName())
                .specifyLastName(chorusTester.getLastName())
                .specifyPassword(chorusTester.getPassword())
                .confirmPassword(chorusTester.getPassword())
                .pressCreateButton();
        LoginPageSteps loginPageSteps = NavigationManager.navigateByActivationLink();
        assertEquals(loginPageSteps.getSuccessAlertText(), "Your email was verified. Please log in.");
        dashboardPageSteps = loginPageSteps.login(chorusTester);
        assertTrue(dashboardPageSteps.getHeader().isUserLoggedIn(chorusTester.getFullName()),
                "User is not logged in after registration");
    }

    @Test  (enabled = false)
    public void inviteNewUserThroughPassingCopy(ProjectData projectData){
        //setup
//        ProjectData projectData = new ProjectData();
//        projectData.setLaboratory(SampleData.LAB_FIRST_CHORUS);
//        projectData.setPersonToInvite(chorusTester.getEmail());
        dashboardPageSteps = loginPageSteps.login(pavelKaplinAtGmail);
        dashboardPageSteps.createPrivateProject(projectData)
                .getProjectsListSteps()
                .passACopyToUnregisteredUser(projectData.getName(), chorusTester.getEmail());
        //test
        dashboardPageSteps.getHeader()
                .logout();
        RegistrationPageSteps registrationPageSteps = loginPageSteps.openRegistrationFormFromInvite();
        assertTrue(registrationPageSteps.getEmail().equals(chorusTester.getEmail()), "User email isn't predefined");
        assertTrue(registrationPageSteps.getConfirmationEmail().equals(projectData.getPersonToInvite()), "User email isn't predefined");
        registrationPageSteps.specifyFirstName(chorusTester.getFirstName())
                .specifyLastName(chorusTester.getLastName())
                .specifyPassword(chorusTester.getPassword())
                .confirmPassword(chorusTester.getPassword())
                .pressCreateButton();
        LoginPageSteps loginPageSteps = NavigationManager.navigateByActivationLink();
        assertEquals(loginPageSteps.getSuccessAlertText(), "Your email was verified. Please log in.");
        dashboardPageSteps = loginPageSteps.login(chorusTester);
        assertTrue(dashboardPageSteps.getHeader().isUserLoggedIn(chorusTester.getFullName()),
                "User is not logged in after registration");
    }
}
