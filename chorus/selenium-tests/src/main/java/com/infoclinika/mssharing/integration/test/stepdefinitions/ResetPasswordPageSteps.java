package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class ResetPasswordPageSteps extends AbstractPageSteps {

    private static final InputBox PASSWORD_FIELD = controlFactory().inputBox(By.id("password"));
    private static final InputBox CONFIRM_PASSWORD_FIELD = controlFactory().inputBox(By.id("confirmPassword"));
    private static final Button RESET_BUTTON = controlFactory().button(By.cssSelector(".btn.main-action"));
    public static final Label FORGOT_PASSWORD_TITLE = controlFactory().label(By.cssSelector("form > h2"));

    public ResetPasswordPageSteps() {
        FORGOT_PASSWORD_TITLE.waitForAppearing();
    }

    public ResetPasswordPageSteps specifyPassword(String password) {
        PASSWORD_FIELD.clearAndFill(password);
        return new ResetPasswordPageSteps();
    }

    public ResetPasswordPageSteps specifyPasswordConfirmation(String password) {
        CONFIRM_PASSWORD_FIELD.fillIn(password);
        return new ResetPasswordPageSteps();
    }

    public LoginPageSteps clickReset() {
        RESET_BUTTON.click();
        return new LoginPageSteps();
    }

}
