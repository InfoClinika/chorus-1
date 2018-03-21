package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class NewsPageSteps extends AbstractPageSteps {

    //Dynamic locators
    private Label firstNewsItemTitle() {
        return new Label(By.xpath("//div[@class='new-item clearfix ng-scope'][1]//h3"));
    }

    private Label firstNewsItemText() {
        return new Label(By.xpath("//div[@class='new-item clearfix ng-scope'][1]//pre"));
    }

    public String getFirstNewstTitle() {
        return firstNewsItemTitle().getText();
    }

    public String getFirstNewsText() {
        return firstNewsItemText().getText();
    }

    public HeaderMenuSteps getHeader() {
        return new HeaderMenuSteps();
    }
}
