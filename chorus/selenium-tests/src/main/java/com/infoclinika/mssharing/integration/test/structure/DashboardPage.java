package com.infoclinika.mssharing.integration.test.structure;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Checkbox;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Pane;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 *
 * Summary: This class describes elements that are displayed on Dashboard Page but are not included into another classes
 * by some reason.
 * Note: All actions which can be performed on the Dashboard Page are located in the appropriate DashboardSteps
 * class.
 */
public class DashboardPage {

    private static final By SEARCH_BAR = By.id("inputIcon");
    private static final By SORTING_BY_NAME_HEADER = By.cssSelector(".user-name > div > a");
    private static final By SEARCH_ICON = By.cssSelector(".icon.search-icon");
    private static final By DONT_SHOW_ME_AGAIN_BILLING_NOTIFICATION_CHECKBOX = By.id("billing-login-notification-hide");
    private static final By OK_BILLING_NOTIFICATION_BUTTON = By.cssSelector("[ng-click=\"billingLoginNotification.ok()\"]");
    private static final By BILLING_NOTIFICATION_DIALOG = By.id("billing-login-notification");

    public InputBox searchBar() {
        return new InputBox(SEARCH_BAR);
    }

    public Button searchIcon(){
        return new Button(SEARCH_ICON);
    }

    public Button sortingByName() {
        return new Button(SORTING_BY_NAME_HEADER);
    }

    public Checkbox dontShowMeAgain(){
        return new Checkbox(DONT_SHOW_ME_AGAIN_BILLING_NOTIFICATION_CHECKBOX);
    }

    public Button ok(){
        return new Button(OK_BILLING_NOTIFICATION_BUTTON);
    }

    public Pane billingNotificationDialog(){
        return new Pane(BILLING_NOTIFICATION_DIALOG);
    }
}
