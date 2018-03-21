package com.infoclinika.mssharing.integration.test.structure.blog;

import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Sergii Moroz
 */
public class BlogsGeneralPage {
    private static final String LATEST_POST_TOP = "//div[@class='post ng-scope span6']";
    private static final String LATEST_POST_BOTTOM = "//div[@class='post ng-scope span3']";
    private static final String LATEST_POST_PROJECT = "//a[@ng-bind='post.blog.name']";
    private static final String LATEST_POST_TITLE = "//a[@ng-bind='post.title']";
    private static final String LATEST_POST_BODY = "//div[@class='post-content']/p";
    private static final String LATEST_BLOGS_UPDATES_TABLE = "//div[@class='content clearfix']";
    private static final String PROJECT_NAME_IN_TABLE = "//div[@class='cell cell-name']";
    private static final String POST_BODY_AND_TITLE_IN_TABLE = "//div[@class='cell cell-post']";

    public Label latestPostTitle(int index) {
        return new Label(By.xpath(LATEST_POST_TOP +"["+index+"]"+LATEST_POST_TITLE));
    }

    public Label latestPostBody(int index) {
        return new Label(By.xpath(LATEST_POST_TOP +"["+index+"]"+LATEST_POST_BODY));
    }

    public Label latestPostProject(int index) {
        return new Label(By.xpath(LATEST_POST_TOP +"["+index+"]"+LATEST_POST_PROJECT));
    }

    public Label smallLatestPostTitle(int index) {
        return new Label(By.xpath(LATEST_POST_BOTTOM +"["+index+"]"+LATEST_POST_TITLE));
    }

    public Label smallLatestPostBody(int index) {
        return new Label(By.xpath(LATEST_POST_BOTTOM +"["+index+"]"+LATEST_POST_BODY));
    }

    public Label smallLatestPostProject(int index) {
        return new Label(By.xpath(LATEST_POST_BOTTOM +"["+index+"]"+LATEST_POST_PROJECT));
    }

    public WebElement getBlogProjectNameFromBlogsTable(int rowIndex) {
        return getDriver().findElements(By.xpath(LATEST_BLOGS_UPDATES_TABLE + PROJECT_NAME_IN_TABLE)).get(rowIndex);
    }

    public WebElement getBlogPostTitleAndBodyFromBlogsTable(int rowIndex) {
        return getDriver().findElements(By.xpath(LATEST_BLOGS_UPDATES_TABLE + POST_BODY_AND_TITLE_IN_TABLE)).get(rowIndex);
    }
}
