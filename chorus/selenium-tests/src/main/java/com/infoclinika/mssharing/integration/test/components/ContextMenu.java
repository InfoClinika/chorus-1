package com.infoclinika.mssharing.integration.test.components;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Alexander Orlov
 */
public class ContextMenu extends Element {

    private By itemOfContextMenu;

    public ContextMenu(Pane element, Button itemOfContextMenu) {
        super(element.getBy());
        this.itemOfContextMenu = itemOfContextMenu.getBy();
    }

//    public void hoverAndClick() {
//        WebElement element = composeWebElement().findElement(itemOfContextMenu);
//        JavascriptExecutor js = (JavascriptExecutor) getDriver();
//        js.executeScript("arguments[0].click();", element);
//    }

    public void hoverAndClick(){
        Actions builder = new Actions(getDriver());
        builder.moveToElement(composeWebElement())
                .contextClick(composeWebElement())
                .click(composeWebElement().findElement(itemOfContextMenu))
                .build()
                .perform();
    }
}
