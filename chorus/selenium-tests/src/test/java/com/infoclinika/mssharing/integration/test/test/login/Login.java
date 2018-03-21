package com.infoclinika.mssharing.integration.test.test.login;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.CleanEmailBoxAfter;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/*
* @author Sergii Moroz
*/

public class Login extends BaseTest {

    @Test(dataProvider = "Login as Admin", dataProviderClass = LoginDataProvider.class, groups = {"staging", "production"})
    public void loginAsAdmin(UserData admin) {
        dashboardPageSteps = loginPageSteps.login(admin).pressOkInBillingNotificationDialog();
        assertTrue(dashboardPageSteps.getHeader().isUserLoggedIn(admin.getFullName()),
                "Admin not logged in");
    }

    @Test(dataProvider = "Login as regular User", dataProviderClass = LoginDataProvider.class, groups = {"staging", "production"})
    public void loginAsRegularUser(UserData user) {
        dashboardPageSteps = loginPageSteps.login(user).pressOkInBillingNotificationDialog();
        assertTrue(dashboardPageSteps.getHeader().isUserLoggedIn(user.getFullName()), "User is not logged in");
    }

    @Test(dataProvider = "Login as User, that has invalid credentials", dataProviderClass = LoginDataProvider.class, groups = {"staging", "production"})
    public void loginWithIncorrectCredentials(UserData userWithInvalidCredentials) {
        loginPageSteps.enterCredentialsAndSignIn(userWithInvalidCredentials);
        assertEquals(loginPageSteps.getLoginErrorText(), "Bad credentials");
    }

    @Test(dataProvider = "Login as User, that has valid login, but invalid password", dataProviderClass = LoginDataProvider.class, groups = {"staging", "production"})
    public void loginWithPartiallyCorrectCredentials(UserData user) {
        loginPageSteps.enterCredentialsAndSignIn(user);
        assertEquals(loginPageSteps.getLoginErrorText(), "Bad credentials");
    }

    @Test(dataProvider = "Login as not activated User", dataProviderClass = LoginDataProvider.class, groups = {"staging"})
    @CleanEmailBoxAfter(isWaitForEmail = true)
    public void loginWithNotActivatedUser(UserData user) {
        loginPageSteps
                .openRegistrationForm()
                .fillForm(user)
                .pressCreateButton();
        assertEquals(loginPageSteps.getSuccessAlertText(), "You were successfully registered. Please check your email");
        loginPageSteps.enterCredentialsAndSignIn(user);
        assertEquals(loginPageSteps.getLoginErrorText(), "Your email is not verified");
    }
}