package com.infoclinika.mssharing.integration.test.components;


import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class Label extends Element {

    public Label(By by) {
        super(by);
    }

    public void click() {
        composeWebElement().click();
    }


}
