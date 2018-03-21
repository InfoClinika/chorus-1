package com.infoclinika.mssharing.integration.test.helper;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.listeners.CaptureScreenShotOnFailureListener;
import com.infoclinika.mssharing.integration.test.logging.Logger;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.LoginPageSteps;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.awt.*;
import java.lang.reflect.Method;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.destroyAllRunningChromeDrivers;
import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.stopDriver;


/**
 * @author Sergii Moroz
 */

@Listeners(CaptureScreenShotOnFailureListener.class)
public class TestManager extends TestData {
    protected DashboardPageSteps dashboardPageSteps;
    protected UserData chorusTesterAtGmail;

    @BeforeSuite
    public void getScreenResolution() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final double defaultWidth = screenSize.getWidth();
        final double defaultHeight = screenSize.getHeight();
        Logger.title("The Screen Resolution is: " + defaultWidth + "x" + defaultHeight);
        Logger.title("Current User is: " + System.getProperty("user.name"));
    }


    @AfterSuite
    public void destroyDriver() {
        destroyAllRunningChromeDrivers();
    }

    @AfterMethod
    public void finish() {
        stopDriver();
    }

    public void logMethodName(Method method) {
        Logger.title("Test method: " + method.getName());
    }

    protected DashboardPageSteps setup(UserData userData) {
        LoginPageSteps loginPageSteps = new LoginPageSteps();
        DashboardPageSteps dashboardPageSteps = loginPageSteps.login(userData);
        if (dashboardPageSteps.isBillingNotificationDialogDisplayed()) {
            dashboardPageSteps.pressOkInBillingNotificationDialog();
        }
        return dashboardPageSteps;
    }

    protected DashboardPageSteps setup(){
        return setup(pavelKaplinAtGmail);
    }
}

