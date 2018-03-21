package com.infoclinika.mssharing.integration.test.components;

import com.google.common.base.Function;
import com.infoclinika.mssharing.integration.test.utils.ConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.DEFAULT_WAIT;
import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Sergii Moroz
 */
public abstract class Element {

    private By by;

    protected Log log = LogFactory.getLog(this.getClass());

    public Element(By by) {
        this.by = by;
    }

    public By getBy() {
        return by;
    }

    protected WebElement composeWebElement() {
        return getDriver().findElement(by);
    }

    public boolean isPresent() {
        try {
            composeWebElement().isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public boolean isVisible(){
        return composeWebElement().isDisplayed();
    }

    public void click() {
        try {
            click(composeWebElement());
        } catch (WebDriverException e) {
            clickWithJS();
        }
    }

    public void clickWithJS() {
        executeJavaScript("arguments[0].click();");
    }

    protected void click(final WebElement element) {
        new FluentWait<>(getDriver())
                .withTimeout(2000, TimeUnit.MILLISECONDS)
                .pollingEvery(200, TimeUnit.MILLISECONDS)
                .ignoring(WebDriverException.class)
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        element.click();
                        return true;
                    }
                });
    }

    public String getText() {
        return new FluentWait<WebDriver>(getDriver())
                .withTimeout(10000, TimeUnit.MILLISECONDS)
                .pollingEvery(200, TimeUnit.MILLISECONDS)
                .ignoring(StaleElementReferenceException.class)
                .until(new Function<WebDriver, String>() {
                    public String apply(WebDriver driver) {
                        return composeWebElement().getText().trim();
                    }
                });
    }

    public String getAttribute(String attribute) {
        return composeWebElement().getAttribute(attribute);
    }

    public void waitForAppearing() {
        WebDriverWait wait = new WebDriverWait(getDriver(), 15);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public void waitForElementToBeVisible(int secondsToWait) {
        WebDriverWait wait = new WebDriverWait(getDriver(), secondsToWait);
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitForElementToBeInvisible() {
        WebDriverWait wait = new WebDriverWait(getDriver(), 30);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    public void waitForElementToBeVisible() {
        WebDriverWait wait = new WebDriverWait(getDriver(), 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitForElementToBeClickable() {
        WebDriverWait wait = new WebDriverWait(getDriver(), 10);
        wait.ignoring(WebDriverException.class).until(ExpectedConditions.elementToBeClickable(by));
    }

    public void scrollToElement() {
        try {
            ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", composeWebElement());
        } catch (StaleElementReferenceException ignore) {
            //ignore this exception
        }
    }

    public void scrollToElementWitAdditionalScrolling(int additionalScroll) {
        try {
            scrollToElement();
            ((JavascriptExecutor) getDriver()).executeScript("window.scrollBy(0," + additionalScroll + ");");
        } catch (StaleElementReferenceException ignore) {

        }
    }

    public void waitForElementToDisappear() {
        ConfigurationManager.setImplicitlyWait(0);
        log.info("Attempt to wait until element found " + by + " will be disappeared");
        new FluentWait<WebDriver>(getDriver())
                .withTimeout(3000, TimeUnit.MILLISECONDS)
                .pollingEvery(200, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class)
                .withMessage("Element found by " + by + " is still visible, but should not be")
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        boolean isPresent = getDriver().findElements(by).size() == 0;
                        if (isPresent) {
                            log.info("- - Ok! Element found " + by + " has disappeared");
                        }
                        return isPresent;
                    }
                });
        ConfigurationManager.setImplicitlyWait(ConfigurationManager.DEFAULT_WAIT);
    }

    public List<WebElement> getAllElements() {
        return getDriver().findElements(by);
    }

    private void executeJavaScript(String script) {
        ((JavascriptExecutor) getDriver()).executeScript(script, composeWebElement());
    }

    public void clickUntilAttributeWillHaveExactValue(final Element element, final String attribute, final String value) {
        ConfigurationManager.setImplicitlyWait(0);
        new FluentWait<WebDriver>(getDriver())
                .withTimeout(5000, TimeUnit.MILLISECONDS)
                .pollingEvery(1000, TimeUnit.MILLISECONDS)
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Fail! Element's " + element.getBy() + " attribute value does not match with " + value + ".\nCurrent value: " + element.getAttribute(attribute))
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        boolean hasValue = element.getAttribute(attribute).equals(value);
                        if (hasValue) {
                            log.info("- - Ok! Element's " + element.getBy() + " attribute value matches with " + value);
                        } else {
                            click();
                        }
                        return hasValue;
                    }
                });
        ConfigurationManager.setImplicitlyWait(DEFAULT_WAIT);
    }

    public void clickUntilAttributeWillContainsValue(final Element element, final String attribute, final String value) {
        ConfigurationManager.setImplicitlyWait(0);
        new FluentWait<WebDriver>(getDriver())
                .withTimeout(5000, TimeUnit.MILLISECONDS)
                .pollingEvery(1000, TimeUnit.MILLISECONDS)
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Fail! Element's " + element.getBy() + " attribute value does not match with " + value + ".\nCurrent value: " + element.getAttribute(attribute))
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        boolean hasValue = element.getAttribute(attribute).contains(value);
                        if (hasValue) {
                            log.info("- - Ok! Element's " + element.getBy() + " attribute value matches with " + value);
                        } else {
                            click();
                        }
                        return hasValue;
                    }
                });
        ConfigurationManager.setImplicitlyWait(DEFAULT_WAIT);
    }

    public boolean waitUntilAttributeWillEqualsValue(final String attribute, final String value){
        final boolean[] hasValue = {false};
        ConfigurationManager.setImplicitlyWait(0);
        new FluentWait<>(getDriver())
                .withTimeout(5000, TimeUnit.MILLISECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NoSuchElementException.class)
                .withMessage("Fail! Element's " + getBy() + " attribute value does not match with " + value + ".\nCurrent value: " + composeWebElement().getAttribute(attribute))
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        hasValue[0] = composeWebElement().getAttribute(attribute).equals(value);
                        if (hasValue[0]) {
                            log.info("- - Ok! Element's " + getBy() + " attribute value matches with " + value);
                        }
                        return hasValue[0];
                    }
                });
        ConfigurationManager.setImplicitlyWait(DEFAULT_WAIT);
        return hasValue[0];
    }

    public void clickUntilElementWillDisappear(final Element element) {
        ConfigurationManager.setImplicitlyWait(0);
        new FluentWait<WebDriver>(getDriver())
                .withTimeout(5000, TimeUnit.MILLISECONDS)
                .pollingEvery(500, TimeUnit.MILLISECONDS)
                .ignoring(StaleElementReferenceException.class)
                .withMessage("Fail! Element " + element.getBy() + " does not disappear.")
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        boolean isPresent = element.isPresent();
                        if (!isPresent) {
                            log.info("- - Ok! Element " + element.getBy() + " disappeared");
                        } else {
                            clickWithJS();
                        }
                        return !isPresent;
                    }
                });
        ConfigurationManager.setImplicitlyWait(DEFAULT_WAIT);
    }

}
