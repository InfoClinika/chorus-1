package com.infoclinika.mssharing.integration.test.test.myprofile;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.CleanEmailBoxBefore;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.*;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.*;

/**
 * @author Alexander Orlov
 */
public class MyProfile extends BaseTest {

    @Test(enabled = false, dataProvider = "changeUserName", dataProviderClass = MyProfileDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore
    public void changeUserName(UserData userData, UserData userWithChangedName, String successAlert) {
        NotificationPopupSteps notificationPopupSteps = loginPageSteps
                .registerNewUser(userData)
                .login(userData)
                .pressOkInBillingNotificationDialog()
                .getHeader().openMyProfile()
                .specifyFirstName(userWithChangedName.getFirstName())
                .specifyLastName(userWithChangedName.getLastName())
                .pressSaveButtonForChangedData();
        assertEquals(notificationPopupSteps.getNotificationText(), successAlert);
        DashboardPageSteps dashboardPageSteps = notificationPopupSteps.pressOkButton();
        assertEquals(dashboardPageSteps.getHeader().getCurrentUser(), userWithChangedName.getFullName(),
                "User name, that is displayed in the top-right of the page does not correspond to the expected");
    }

    @Test(dataProvider = "shouldNotAllowToSaveProfileWithoutFirstName", dataProviderClass = MyProfileDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore
    @LoginRequired
    public void shouldNotAllowToSaveProfileWithoutFirstName(String alert) {
        MyProfileGeneralTabSteps myProfileGeneralTabSteps = dashboardPageSteps.getHeader().openMyProfile()
                .specifyFirstName("");
        assertEquals(myProfileGeneralTabSteps.getFirstNameAlert(), alert,
                "Actual alert message does not match with the expected");
        assertTrue(myProfileGeneralTabSteps.isTabErrorIconPresent(),
                "General Tab error icon is not present, First Name field was not filled in, though");
        assertFalse(myProfileGeneralTabSteps.isSaveButtonEnabled(),
                "'Save' button is enabled, First Name field was not filled in, though");
    }

    @Test(dataProvider = "shouldNotAllowToSaveProfileWithoutLastName", dataProviderClass = MyProfileDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore
    @LoginRequired
    public void shouldNotAllowToSaveProfileWithoutLastName(String alert) {
        MyProfileGeneralTabSteps myProfileGeneralTabSteps = dashboardPageSteps.getHeader().openMyProfile()
                .specifyLastName("");
        assertEquals(myProfileGeneralTabSteps.getLastNameAlert(), alert,
                "Actual alert message does not match with the expected");
        assertTrue(myProfileGeneralTabSteps.isTabErrorIconPresent(),
                "General Tab error icon is not present, Last Name field was not filled in, though");
        assertFalse(myProfileGeneralTabSteps.isSaveButtonEnabled(),
                "'Save' button is enabled, Last Name field was not filled in, though");
    }

    @Test(dataProvider = "changePassword", dataProviderClass = MyProfileDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore
    public void changePassword(UserData userData, UserData userWithChangedPassword, String successAlert) {
        NotificationPopupSteps notificationPopupSteps = loginPageSteps
                .registerNewUser(userData)
                .login(userData)
                .pressOkInBillingNotificationDialog()
                .getHeader().openMyProfile()
                .selectChangePasswordTab()
                .specifyOldPassword(userData.getPassword())
                .specifyNewPassword(userWithChangedPassword.getPassword())
                .specifyConfirmNewPassword(userWithChangedPassword.getPassword())
                .pressChangePasswordButtonForChangedData();
        assertEquals(notificationPopupSteps.getNotificationText(), successAlert);
        DashboardPageSteps dashboardPageSteps = notificationPopupSteps.pressOkButton()
                .getHeader()
                .logout()
                .pressSignInButton()
                .login(userWithChangedPassword);
        assertTrue(dashboardPageSteps.getHeader().isUserLoggedIn(userWithChangedPassword.getFullName()),
                "User is not able to sign in after password changing");
    }
}
