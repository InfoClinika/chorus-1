package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Checkbox;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.utils.NavigationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class LoginPageSteps extends AbstractPageSteps {

    protected Log log = LogFactory.getLog(this.getClass());

    private static final InputBox EMAIL_FIELD = controlFactory().inputBox(By.id("email"));
    private static final InputBox PASSWORD_FIELD = controlFactory().inputBox(By.id("password"));
    private static final Checkbox REMEMBER_ME_CHECKBOX = controlFactory().checkbox(By.name("_spring_security_remember_me"));
    private static final Button FORGOT_PASSWORD_LINK = controlFactory().button(By.linkText("Forgot password?"));
    private static final Button SIGN_IN_BUTTON = controlFactory().button(By.id("sign-in"));
    private static final Button REGISTRATION_LINK = controlFactory().button(By.linkText("Create an account"));
    private static final Label LOGIN_ERROR_LABEL = controlFactory().label(By.cssSelector(".login-error-message"));
    private static final Label ALERT_SUCCESS = controlFactory().label(By.className("success-message"));

    public LoginPageSteps() {
        waitForPageToLoad();
    }

    public DashboardPageSteps login(UserData userData) {
        EMAIL_FIELD.fillIn(userData.getEmail());
        PASSWORD_FIELD.fillIn(userData.getPassword());
        SIGN_IN_BUTTON.click();
        return new DashboardPageSteps();
    }

    public String getLoginErrorText() {
        LOGIN_ERROR_LABEL.waitForElementToBeVisible();
        return LOGIN_ERROR_LABEL.getText();
    }

    public String getSuccessAlertText() {
        ALERT_SUCCESS.waitForElementToBeVisible();
        return ALERT_SUCCESS.getText();
    }

    public LoginPageSteps enterCredentialsAndSignIn(UserData userData) {
        EMAIL_FIELD.fillIn(userData.getEmail());
        PASSWORD_FIELD.fillIn(userData.getPassword());
        SIGN_IN_BUTTON.click();
        return this;
    }

    public RegistrationPageSteps openRegistrationForm() {
        REGISTRATION_LINK.click();
        return new RegistrationPageSteps();
    }

    public ForgotPasswordPageSteps pressForgotPassword() {
        FORGOT_PASSWORD_LINK.click();
        return new ForgotPasswordPageSteps();
    }

    // Complex methods
    public LoginPageSteps registerNewUser(UserData userData) {
        openRegistrationForm().fillForm(userData).pressCreateButton();
        return NavigationManager.navigateByActivationLink();
    }

    public LoginPageSteps registerNewUser(UserData userData, String laboratory){
        openRegistrationForm().fillForm(userData).specifyLab(laboratory).pressCreateButton();
        return NavigationManager.navigateByActivationLink();
    }

    public RegistrationPageSteps openRegistrationFormFromInvite() {
        NavigationManager.navigateByInviteLink();
        return new RegistrationPageSteps();
    }
}
