package com.infoclinika.mssharing.integration.test.components;

import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class RadioButton extends Element{

    public RadioButton(By by) {
        super(by);
    }

    public void click() {
        waitForElementToBeVisible();
        composeWebElement().click();
    }
}
