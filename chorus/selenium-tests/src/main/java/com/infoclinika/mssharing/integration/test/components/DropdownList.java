package com.infoclinika.mssharing.integration.test.components;

import com.infoclinika.mssharing.integration.test.utils.ConfigurationManager;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;
import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.setImplicitlyWait;

/**
 * @author Alexander Orlov
 */
public class DropdownList extends Element {

    public static By dropDownItemComplex(String value) {
        return By.xpath("//div[@class='select2-result-label']/span[contains(text(),\"" + value + "\")]");
    }

    public static By dropDownItemGeneral(String value) {
        return By.xpath("//div[@class='select2-result-label'][text()[contains(.,\"" + value + "\")]]");
    }

    public DropdownList(By by) {
        super(by);
    }

//    public void select(String item) {
//
//        if (composeWebElement().getTagName().equals("select")) {
//            Select realSelect = new Select(composeWebElement());
//            realSelect.selectByValue(item);
//        } else {
//            //select2 implementation
//            waitForDropDownToBeEnabled();
//            //this "if" needed because of issue with dropdown for factor type, which do not allow to select items after scrolling
//            //todo[alexander orlov]: fix issue with dropdown for factor type, which do not allow to select items after scrolling
//            if (!item.equalsIgnoreCase("Text") && !item.equalsIgnoreCase("Number")) {
//                scrollToElement();
//            }
//            click();
//            try {
//                Thread.sleep(300);
//            } catch (InterruptedException ignored) {
//
//            }
//            while (!composeWebElement().getAttribute("class").contains("select2-dropdown-open")) {
//                click();
//                try {
//                    Thread.sleep(300);
//                } catch (InterruptedException ignored) {
//
//                }
//            }
//            By locatorForItemInDropdown = dropDownItemGeneral(item);
//            if (getNumberOfItems(locatorForItemInDropdown) == 0) {
//                locatorForItemInDropdown = dropDownItemComplex(item);
//            }
//            click(getDriver().findElement(locatorForItemInDropdown));
//        }
//    }

    public void select(String item) {
        waitForElementToBeVisible();
        if (composeWebElement().getTagName().equals("select")) {
            Select realSelect = new Select(composeWebElement());
            realSelect.selectByValue(item);
        } else {
            clickUntilAttributeWillContainsValue(this, "class", "select2-dropdown-open");
        }
        By locatorForItemInDropdown = dropDownItemGeneral(item);
        if (getNumberOfItems(locatorForItemInDropdown) == 0) {
            locatorForItemInDropdown = dropDownItemComplex(item);
        }
        click(getDriver().findElement(locatorForItemInDropdown));
    }

    public boolean isEnabled() {
        waitForElementToBeVisible();
        return !composeWebElement().getAttribute("class").contains("disabled");
    }

    private void waitForDropDownToBeEnabled() {
        new WebDriverWait(getDriver(), 10).ignoring(StaleElementReferenceException.class).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return isEnabled();
            }
        });
    }

    private int getNumberOfItems(By locator) {
        setImplicitlyWait(200);
        int numberOfItems = getDriver().findElements(locator).size();
        setImplicitlyWait(ConfigurationManager.DEFAULT_WAIT);
        return numberOfItems;
    }

    public String getText() {
        waitForElementToBeVisible();
        return composeWebElement().getText().trim();
    }

}
