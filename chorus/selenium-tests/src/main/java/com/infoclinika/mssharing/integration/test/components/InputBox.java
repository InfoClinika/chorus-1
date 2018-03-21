package com.infoclinika.mssharing.integration.test.components;

import com.google.common.base.Function;
import com.infoclinika.mssharing.integration.test.utils.ConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.concurrent.TimeUnit;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.DEFAULT_WAIT;
import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Sergii Moroz
 */
public class InputBox extends Element {

    public InputBox(By by) {
        super(by);
    }

    public void fillIn(String text) {
        waitForElementToBeClickable();
        if (text != null) {
            composeWebElement().clear();
            composeWebElement().sendKeys(text);
        }
    }

    public void fillInAndWaitForTextAppearing(String text){
            ConfigurationManager.setImplicitlyWait(0);
            new FluentWait<WebDriver>(getDriver())
                    .withTimeout(5000, TimeUnit.MILLISECONDS)
                    .pollingEvery(1000, TimeUnit.MILLISECONDS)
                    .ignoring(StaleElementReferenceException.class)
                    .ignoring(NoSuchElementException.class)
                    .withMessage("Fail! '" + text + "' text does not appear after 5 seconds waiting. Text area selector: " + getBy())
                    .until(new Function<WebDriver, Boolean>() {
                        public Boolean apply(WebDriver driver) {
                            fillIn(text);
                            boolean isCorrectText = getValue().equals(text);
                            if (isCorrectText) {
                                log.info("- - Ok! '" + text + "' text appears in the text area " + getBy());
                            } else {
                                fillIn(text);
                            }
                            return isCorrectText;
                        }
                    });
            ConfigurationManager.setImplicitlyWait(DEFAULT_WAIT);
    }

    public void click() {
        waitForAppearing();
        composeWebElement().click();
    }

    public void fillIn(int value) {
        if (value != 0) {
            fillIn("" + value);
        }
    }

    public void fillIn(double value){
        if (value != 0) {
            fillIn("" + value);
        }
    }

    public InputBox clear() {
        waitForAppearing();
        composeWebElement().clear();
        composeWebElement().sendKeys(" ");
        // "\u0008" - is backspace char
        composeWebElement().sendKeys("\u0008");
        return this;
    }

    public void clearAndFill(String text) {
        if (!StringUtils.isEmpty(composeWebElement().getAttribute("value"))) {
            clear();
        }
        composeWebElement().sendKeys(text);
    }

    public String getValue() {
        waitForAppearing();
        return composeWebElement().getAttribute("value");
    }

    public void pressTabKey() {
        composeWebElement().sendKeys(Keys.TAB);
    }

    public void pressEnterKey() {
        composeWebElement().sendKeys(Keys.ENTER);
    }

    public String getText(){
        waitForAppearing();
        return composeWebElement().getText();
    }


}

