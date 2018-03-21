package com.infoclinika.mssharing.integration.test.test.forgotpassword;

import com.infoclinika.mssharing.integration.test.data.EmailFolder;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.CleanEmailBoxBefore;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.ResetPasswordPageSteps;
import com.infoclinika.mssharing.integration.test.utils.NavigationManager;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Orlov
 */
public class ForgotPassword extends BaseTest {

    @Test(dataProvider = "forgotPassword", dataProviderClass = ForgotPasswordDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore(folderName = {EmailFolder.VERIFY_EMAIL, EmailFolder.RESET_PASSWORD})
    public void forgotPassword(UserData userData, UserData userWithChangedPassword) {
        //register new account
        loginPageSteps.registerNewUser(userData);
        //reset password for newly created account
        loginPageSteps.pressForgotPassword()
                .specifyForgotPasswordEmail(userData.getEmail())
                .pressSendInstructionsButton();
        assertEquals(loginPageSteps.getSuccessAlertText(),
                "Reset password instructions have been sent. Check your email.");
        //proceed to the Chorus using Forgot Password link from email received
        ResetPasswordPageSteps resetPasswordPage = NavigationManager.navigateByLinkFromResetPasswordEmail();
        //define new password
        loginPageSteps = resetPasswordPage
                .specifyPassword(userWithChangedPassword.getPassword())
                .specifyPasswordConfirmation(userWithChangedPassword.getPassword())
                .clickReset();
        assertEquals(loginPageSteps.getSuccessAlertText(), "Your password has been reset.");
        //attempt to login with new password
        DashboardPageSteps appSteps = loginPageSteps.login(userWithChangedPassword);
        assertTrue(appSteps.getHeader().isUserLoggedIn(userWithChangedPassword.getFullName()),
                "User is not logged in after password has been changed");
    }

    @Test(dataProvider = "forgotPasswordForIncorrectCredentials", dataProviderClass = ForgotPasswordDataProvider.class, groups = {"staging"})
    public void forgotPasswordForIncorrectCredentials(String notRegisteredEmail) {
        loginPageSteps.pressForgotPassword()
                .specifyForgotPasswordEmail(notRegisteredEmail)
                .pressSendInstructionsButtonForIncorrectData();
        assertEquals(loginPageSteps.getLoginErrorText(), "Such email is not registered");
    }
}
