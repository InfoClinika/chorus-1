package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.data.NewsData;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class HomePageSteps extends AbstractPageSteps {

    //Dynamic locator
    private Pane newsItem(String title) {
        return new Pane(By.xpath("//div[@class='new-item ng-scope'][.//a[text()='" + title + "']]"));
    }

    private static final Label FIRST_NEWS_ITEM_TITLE = controlFactory().label(By.xpath("//div[@class='new-item ng-scope'][1]//a"));
    private static final Label FIRST_NEWS_ITEM_INTRODUCTION = controlFactory().label(By.xpath("//div[@class='new-item ng-scope'][1]//p"));

    public LoginPageSteps pressSignInButton() {
        getHeader().clickSignInLink();
        return new LoginPageSteps();
    }

    public String getFirstNewsTitle() {
        return FIRST_NEWS_ITEM_TITLE.getText();
    }

    public String getFirstNewsIntroduction() {
        return FIRST_NEWS_ITEM_INTRODUCTION.getText();
    }

    public boolean isNewsPresent(NewsData newsData) {
        return newsItem(newsData.getNewsTitle()).isPresent();
    }

    public NewsPageSteps selectFirstNews() {
        FIRST_NEWS_ITEM_TITLE.scrollToElementWitAdditionalScrolling(-50);
        FIRST_NEWS_ITEM_TITLE.click();
        return new NewsPageSteps();
    }

    public HeaderMenuSteps getHeader() {
        return new HeaderMenuSteps();
    }
}
