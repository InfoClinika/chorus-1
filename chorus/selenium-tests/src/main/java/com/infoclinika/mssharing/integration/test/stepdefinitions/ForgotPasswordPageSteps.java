package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class ForgotPasswordPageSteps extends AbstractPageSteps {

    private static final InputBox EMAIL_FIELD = controlFactory().inputBox(By.id("forgotPasswordEmail"));
    private static final Button SEND_INSTRUCTIONS_BUTTON = controlFactory().button(By.id("send-instructions"));
    private static final Label SUCCESS_MESSAGE_LABEL = controlFactory().label(By.className("success-message"));
    private static final Label ERROR_MESSAGE_LABEL = controlFactory().label(By.cssSelector(".login-error-message"));
    private static final Button BACK_TO_LOGIN_PAGE_BUTTON = controlFactory().button(By.cssSelector("[ng-show='instructionsSend']"));

    public ForgotPasswordPageSteps() {
        SEND_INSTRUCTIONS_BUTTON.waitForElementToBeVisible();
    }

    public ForgotPasswordPageSteps specifyForgotPasswordEmail(String email) {
        EMAIL_FIELD.fillIn(email);
        return this;
    }

    public ForgotPasswordPageSteps pressSendInstructionsButton() {
        SEND_INSTRUCTIONS_BUTTON.click();
        BACK_TO_LOGIN_PAGE_BUTTON.waitForElementToBeVisible(3);
        return this;
    }

    public ForgotPasswordPageSteps pressSendInstructionsButtonForIncorrectData() {
        SEND_INSTRUCTIONS_BUTTON.click();
        return this;
    }
}
