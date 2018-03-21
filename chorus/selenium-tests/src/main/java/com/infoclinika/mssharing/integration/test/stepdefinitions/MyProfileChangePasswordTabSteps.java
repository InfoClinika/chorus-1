package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Image;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class MyProfileChangePasswordTabSteps extends AbstractPageSteps{

    private static final InputBox OLD_PASSWORD_FIELD = controlFactory().inputBox(By.id("oldPassword"));
    private static final InputBox NEW_PASSWORD_FIELD = controlFactory().inputBox(By.id("password"));
    private static final InputBox CONFIRM_NEW_PASSWORD_FIELD = controlFactory().inputBox(By.id("confirmPassword"));
    private static final Label OLD_PASSWORD_ALERT = controlFactory().label(By.cssSelector("[for='oldPassword']"));
    private static final Label NEW_PASSWORD_ALERT = controlFactory().label(By.cssSelector("[for='password']"));
    private static final Label CONFIRM_NEW_PASSWORD_ALERT = controlFactory().label(By.cssSelector("[for='confirmPassword']"));
    private static final Button CHANGE_PASSWORD_BUTTON = controlFactory().button(By.xpath("//button[text()='Change Password']"));
    private static final Image CHANGE_PASSWORD_TAB_ERROR_ICON = controlFactory().image(By.xpath("//tab-heading[contains(text(),'Change Password')]/span"));

    public MyProfileChangePasswordTabSteps() {
        OLD_PASSWORD_FIELD.waitForElementToBeClickable();
    }

    public MyProfileChangePasswordTabSteps specifyOldPassword(String oldPassword) {
        OLD_PASSWORD_FIELD.clearAndFill(oldPassword);
        return this;
    }

    public MyProfileChangePasswordTabSteps specifyNewPassword(String newPassword) {
        NEW_PASSWORD_FIELD.clearAndFill(newPassword);
        return this;
    }

    public MyProfileChangePasswordTabSteps specifyConfirmNewPassword(String confirmNewPassword) {
        CONFIRM_NEW_PASSWORD_FIELD.clearAndFill(confirmNewPassword);
        return this;
    }

    public NotificationPopupSteps pressChangePasswordButtonForChangedData() {
        CHANGE_PASSWORD_BUTTON.click();
        return new NotificationPopupSteps();
    }

    public DashboardPageSteps pressChangePasswordButtonWithoutChanges() {
        CHANGE_PASSWORD_BUTTON.click();
        return new DashboardPageSteps();
    }

    public boolean isChangePasswordButtonEnabled() {
        CHANGE_PASSWORD_BUTTON.waitForButtonToBeEnabled();
        return CHANGE_PASSWORD_BUTTON.isEnabled();
    }

    public String getOldPasswordAlert() {
        return OLD_PASSWORD_ALERT.getText();
    }
}
