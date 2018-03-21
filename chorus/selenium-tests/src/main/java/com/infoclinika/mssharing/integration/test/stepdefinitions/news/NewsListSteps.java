package com.infoclinika.mssharing.integration.test.stepdefinitions.news;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.ContextMenu;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class NewsListSteps extends AbstractPageSteps{

    //Dynamic locators
    private Pane newsInList(String newsTitle) {
        return new Pane(By.xpath("//div[@class='row clearfix ng-scope'][.//div[text()='" + newsTitle + "']]"));
    }

    private static final Button DELETE_BUTTON = controlFactory().button(By.cssSelector("[ng-click='showRemoveConfirmation(n)']"));
    private static final Button DETAILS_BUTTON = controlFactory().button(By.cssSelector(".details-link-name"));
    private static final Button REMOVE_CONFIRMATION_BUTTON = controlFactory().button(By.cssSelector("[ng-click='confirmation.removeItem()']"));

    private ContextMenu newsDeleteButton(String newsTitle){
        return new ContextMenu(newsInList(newsTitle), DELETE_BUTTON);
    }

    private ContextMenu newsDetailsButton(String newsTitle){
        return new ContextMenu(newsInList(newsTitle), DETAILS_BUTTON);
    }

    public boolean isNewsDisplayed(String newsTitle){
        return newsInList(newsTitle).isPresent();
    }

    public NewsListSteps deleteNews(String newsTitle){
        newsInList(newsTitle).scrollToElement();
        newsDeleteButton(newsTitle).hoverAndClick();
        REMOVE_CONFIRMATION_BUTTON.click();
        newsInList(newsTitle).waitForElementToDisappear();
        return this;
    }

}
