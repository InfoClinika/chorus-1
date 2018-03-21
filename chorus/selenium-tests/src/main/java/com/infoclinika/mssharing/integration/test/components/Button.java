package com.infoclinika.mssharing.integration.test.components;

import com.google.common.base.Function;
import com.infoclinika.mssharing.integration.test.utils.ConfigurationManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Sergii Moroz
 */
public class Button extends Element {

    public Button(By by) {
        super(by);
    }

    public void doubleClick() {
        waitForElementToBeClickable();
        new Actions(getDriver())
                .doubleClick(composeWebElement())
                .build()
                .perform();
    }

    public void scrollAndClick() {
        scrollToElement();
        click();
    }

    public boolean isEnabled() {
        try {
            waitForButtonToBeEnabled();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public Button waitForButtonToBeEnabled() {
        final boolean[] isEnabled = new boolean[1];
        ConfigurationManager.setImplicitlyWait(0);
        log.info("Attempt to wait until button found " + getBy() + " will be enabled");
        new FluentWait<WebDriver>(getDriver())
                .withTimeout(3000, TimeUnit.MILLISECONDS)
                .pollingEvery(200, TimeUnit.MILLISECONDS)
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Button found by " + getBy() + " is still disabled, but should not be")
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        isEnabled[0] = !("true").equals(composeWebElement().getAttribute("disabled"));
                        if (isEnabled[0]) {
                            log.info("- - Ok! Button found " + getBy() + " became enabled");
                        }
                        return isEnabled[0];
                    }
                });
        ConfigurationManager.setImplicitlyWait(ConfigurationManager.DEFAULT_WAIT);
        return this;
    }

    public Button waitForButtonToBeDisabled() {
        ConfigurationManager.setImplicitlyWait(0);
        log.info("Attempt to wait until button found " + getBy() + " will be disabled");
        new FluentWait<WebDriver>(getDriver())
                .withTimeout(5000, TimeUnit.MILLISECONDS)
                .pollingEvery(200, TimeUnit.MILLISECONDS)
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Button found by " + getBy() + " is still enabled, but should not be")
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        boolean isEnabled = isEnabled();
                        if (!isEnabled) {
                            log.info("- - Ok! Button found " + getBy() + " became disabled");
                        }
                        return !isEnabled;
                    }
                });
        ConfigurationManager.setImplicitlyWait(ConfigurationManager.DEFAULT_WAIT);
        return this;
    }

    public List<Button> getAll() {
        List<Button> linkList = new ArrayList<>();
        int numberOfElements = getAllElements().size();
        for (int i = 0; i < numberOfElements; i++) {
            linkList.add(this);
        }
        return linkList;
    }

}
