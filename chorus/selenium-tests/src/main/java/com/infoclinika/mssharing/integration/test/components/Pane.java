package com.infoclinika.mssharing.integration.test.components;

import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class Pane extends Element {

    public Pane(By by) {
        super(by);
    }

    public int getNumberOfAll(){
        return getAllElements().size();
    }



}
