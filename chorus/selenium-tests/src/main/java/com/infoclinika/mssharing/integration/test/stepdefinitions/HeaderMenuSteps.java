package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class HeaderMenuSteps extends AbstractPageSteps {

    //Dynamic locator
    private Button userNameLink(String username) {
        return new Button(By.cssSelector(".u-name[title='" + username + "']"));
    }

    private static final Button NEWS_ITEM = controlFactory().button(By.cssSelector("#menu a[href='news.html']"));
    private static final Button APPLICATION_ITEM = controlFactory().button(By.cssSelector("#menu a[href='dashboard.html']"));
    private static final Button USER_LINK = controlFactory().button(By.cssSelector("a.u-name"));
    private static final Label FIRST_NAME_LABEL = controlFactory().label(By.cssSelector("[ng-bind='loggedInUser.firstName']"));
    private static final Label LAST_NAME_LABEL = controlFactory().label(By.cssSelector("[ng-bind='loggedInUser.lastName']"));
    private static final Button SIGN_IN_LINK = controlFactory().button(By.cssSelector(".user-name>a"));
    private static final Button HOME_LINK = controlFactory().button(By.cssSelector("[href='index.html']"));

    private static final Button MY_PROFILE_BUTTON = controlFactory().button(By.linkText("My Profile"));
    private static final Button SIGN_OUT_BUTTON = controlFactory().button(By.linkText("Sign out"));

    public NewsPageSteps selectNews() {
        NEWS_ITEM.click();
        return new NewsPageSteps();
    }

    public DashboardPageSteps selectApplication() {
        APPLICATION_ITEM.click();
        return new DashboardPageSteps();
    }

    public LoginPageSteps clickSignInLink() {
        SIGN_IN_LINK.click();
        return new LoginPageSteps();
    }

    public HomePageSteps pressHomeLink() {
        HOME_LINK.click();
        return new HomePageSteps();
    }

    public HomePageSteps logout() {
        //ToDo: find a better way to implement this wait
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        USER_LINK.click();
        SIGN_OUT_BUTTON.click();
        return new HomePageSteps();
    }

    public boolean isUserLoggedIn(String username) {
        return userNameLink(username).isPresent();
    }

    public String getCurrentUser() {
        return FIRST_NAME_LABEL.getText() + " " + LAST_NAME_LABEL.getText();
    }

    public MyProfileGeneralTabSteps openMyProfile() {
        USER_LINK.click();
        MY_PROFILE_BUTTON.click();
        return new MyProfileGeneralTabSteps();
    }

}
