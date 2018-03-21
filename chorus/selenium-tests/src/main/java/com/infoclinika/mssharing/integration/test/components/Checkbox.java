package com.infoclinika.mssharing.integration.test.components;

import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class Checkbox extends Element {

    public Checkbox(By by) {
        super(by);
    }

    public void check(){
        if (!isSelected()){
            composeWebElement().click();
        }
    }

    public void setValue(boolean value){
        if(isSelected() != value){
            composeWebElement().click();
        }
    }

    public boolean isSelected() {
        return composeWebElement().isSelected();
    }

    public int getNumberOfAll(){
        return getAllElements().size();
    }
}
