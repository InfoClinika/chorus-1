package com.infoclinika.mssharing.integration.test.components;

import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class WizardTable extends Element{

    public WizardTable(By by) {
        super(by);
    }

    public void fillIn(String value){
        composeWebElement().click();
        composeWebElement().sendKeys(value);
    }

    public void fillIn(int value){
        fillIn(String.valueOf(value));
    }

    public void clear(){
        composeWebElement().getText();
        while (!composeWebElement().getText().equals("")) {
            composeWebElement().sendKeys("\b");
        }
    }

}
