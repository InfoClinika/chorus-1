package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Image;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class MyProfileGeneralTabSteps extends AbstractPageSteps {

    private static final InputBox FIRST_NAME_FIELD = controlFactory().inputBox(By.id("firstName"));
    private static final InputBox LAST_NAME_FIELD = controlFactory().inputBox(By.id("lastName"));
    private static final Label FIRST_NAME_ALERT = controlFactory().label(By.cssSelector("[for='firstName']"));
    private static final Label LAST_NAME_ALERT = controlFactory().label(By.cssSelector("[for=lastName]"));
    private static final Image GENERAL_TAB_ERROR_ICON = controlFactory().image(By.xpath("//tab-heading[contains(text(),'General')]/span"));
    private static final Button SAVE_BUTTON = controlFactory().button(By.cssSelector(".btn.main-action.ng-binding"));
    private static final Button CHANGE_PASSWORD_TAB = controlFactory().button(By.xpath("//tab-heading[contains(text(),'Change Password')]"));

    public MyProfileGeneralTabSteps specifyFirstName(String firstName) {
        FIRST_NAME_FIELD.clearAndFill(firstName);
        return this;
    }

    public MyProfileGeneralTabSteps specifyLastName(String lastName) {
        LAST_NAME_FIELD.clearAndFill(lastName);
        return this;
    }

    public NotificationPopupSteps pressSaveButtonForChangedData() {
        SAVE_BUTTON.click();
        return new NotificationPopupSteps();
    }

    public DashboardPageSteps pressSaveButtonWithoutChanges() {
        SAVE_BUTTON.click();
        return new DashboardPageSteps();
    }

    public boolean isSaveButtonEnabled() {
        return SAVE_BUTTON.isEnabled();
    }

    public String getFirstNameAlert() {
        return FIRST_NAME_ALERT.getText();
    }

    public String getLastNameAlert() {
        return LAST_NAME_ALERT.getText();
    }

    public boolean isTabErrorIconPresent() {
        return GENERAL_TAB_ERROR_ICON.isPresent();
    }

    public MyProfileChangePasswordTabSteps selectChangePasswordTab() {
        CHANGE_PASSWORD_TAB.waitForElementToBeVisible();
        CHANGE_PASSWORD_TAB.click();
        return new MyProfileChangePasswordTabSteps();
    }

}
