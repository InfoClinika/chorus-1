package com.infoclinika.mssharing.integration.test.components;

import org.openqa.selenium.By;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Alexander Orlov
 */
public class Frame extends Element {

    public Frame(By by) {
        super(by);
    }

    public void switchToFrame(){
        getDriver().switchTo().frame(composeWebElement());
    }

    public void backOutOfFrame(){
        getDriver().switchTo().defaultContent();
    }
}
