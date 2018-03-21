package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.google.common.base.Function;
import com.infoclinika.mssharing.integration.test.components.ControlFactory;
import com.infoclinika.mssharing.integration.test.logging.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Alexander Orlov
 */
public abstract class AbstractPageSteps {

    private static ControlFactory controlFactory;

    public static ControlFactory controlFactory() {
        if (controlFactory == null) {
            return new ControlFactory();
        } else {
            return controlFactory;
        }
    }

    public void waitForPageToLoad() {
        Wait<WebDriver> wait = new WebDriverWait(getDriver(), 30);
        wait.until(new Function<WebDriver, Boolean>() {
            public Boolean apply(WebDriver driver) {
                Logger.log("Current Window State: "
                        + String.valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState")));
                return String
                        .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                        .equals("complete");
            }
        });
    }

    public void wait(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
