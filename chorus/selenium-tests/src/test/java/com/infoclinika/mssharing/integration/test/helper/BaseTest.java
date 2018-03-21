package com.infoclinika.mssharing.integration.test.helper;

import com.infoclinika.mssharing.integration.test.data.EmailFolder;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.exception.FuncTestInfrastructureException;
import com.infoclinika.mssharing.integration.test.listeners.*;
import com.infoclinika.mssharing.integration.test.logging.Logger;
import com.infoclinika.mssharing.integration.test.preconditions.*;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.LoginPageSteps;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.awt.*;
import java.lang.reflect.Method;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.stopDriver;
import static com.infoclinika.mssharing.integration.test.utils.EmailService.connectAndRemoveAllMessagesFromFolder;
import static com.infoclinika.mssharing.integration.test.utils.EmailService.waitForEmailAndRemoveAllMessagesFromFolder;

/**
 * @author Alexander Orlov
 */
@Listeners(ChorusListener.class)
public class BaseTest extends AbstractDataProvider{

    private Log log = LogFactory.getLog(this.getClass());

    protected DashboardPageSteps dashboardPageSteps;
    protected LoginPageSteps loginPageSteps;

    @BeforeSuite
    public void getScreenResolution() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final double defaultWidth = screenSize.getWidth();
        final double defaultHeight = screenSize.getHeight();
        log.info("The Screen Resolution is: " + defaultWidth + "x" + defaultHeight);
        log.info("Current User is: " + System.getProperty("user.name"));
        Logger.title("The Screen Resolution is: " + defaultWidth + "x" + defaultHeight);
        Logger.title("Current User is: " + System.getProperty("user.name"));
    }

    @PrepareTest
    public void setUpConfig(Method method) {

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final double defaultWidth = screenSize.getWidth();
        final double defaultHeight = screenSize.getHeight();

        if (method == null) {
            throw new FuncTestInfrastructureException("'method' parameter in method setUpConfig is null ");
        }

        Logger.logMethodName(method);
        Logger.title("The Screen Resolution is: " + defaultWidth + "x" + defaultHeight);
        Logger.title("Current User is: " + System.getProperty("user.name"));

        if (method.isAnnotationPresent(CleanEmailBoxBefore.class)) {
            EmailFolder[] emailFolders = method.getAnnotation(CleanEmailBoxBefore.class).folderName();
            for (EmailFolder folder : emailFolders) {
                connectAndRemoveAllMessagesFromFolder(folder);
            }
        }

        if (method.isAnnotationPresent(Environment.class)) {
            String appURL = method.getAnnotation(Environment.class).url();
            startDriver(appURL);
        } else {
            startDriver();
        }

        loginPageSteps = new LoginPageSteps();

        if (method.isAnnotationPresent(LoginRequired.class)) {
            String email = method.getAnnotation(LoginRequired.class).email();
            String password = method.getAnnotation(LoginRequired.class).password();
            boolean isDisableBillingNotification = method.getAnnotation(LoginRequired.class).isDisableBillingNotification();
            dashboardPageSteps = loginPageSteps.login(new UserData.Builder().email(email).password(password).build());

            if (isDisableBillingNotification && dashboardPageSteps.isBillingNotificationDialogDisplayed()) {
                dashboardPageSteps.pressOkInBillingNotificationDialog();
            }

        }
    }

    @AfterTestComplete
    public void tearDownConfig(Method method) {
        if (method == null) {
            throw new FuncTestInfrastructureException("'method' parameter in method setUpConfig is null ");
        }

        if (method.isAnnotationPresent(CleanEmailBoxAfter.class)) {
            EmailFolder emailFolder = method.getAnnotation(CleanEmailBoxAfter.class).folderName();
            boolean isWaitForEmail = method.getAnnotation(CleanEmailBoxAfter.class).isWaitForEmail();
            if (isWaitForEmail) {
                waitForEmailAndRemoveAllMessagesFromFolder(emailFolder);
            } else {
                connectAndRemoveAllMessagesFromFolder(emailFolder);
            }
        }

        stopDriver();
    }
}
