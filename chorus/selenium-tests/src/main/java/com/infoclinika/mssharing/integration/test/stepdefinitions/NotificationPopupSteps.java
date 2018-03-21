package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class NotificationPopupSteps extends AbstractPageSteps{

    private static final Label NOTIFICATION_POPUP_TEXT = controlFactory().label(By.id("userChangedDialog"));
    private static final Button NOTIFICATION_POPUP_OK_BUTTON = controlFactory().button(By.cssSelector("[type='button']"));

    public String getNotificationText(){
        NOTIFICATION_POPUP_TEXT.waitForElementToBeVisible();
        return NOTIFICATION_POPUP_TEXT.getText();
    }

    public DashboardPageSteps pressOkButton(){
        NOTIFICATION_POPUP_OK_BUTTON.click();
        return new DashboardPageSteps();
    }
}
