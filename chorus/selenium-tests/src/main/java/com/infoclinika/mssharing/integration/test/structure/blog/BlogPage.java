package com.infoclinika.mssharing.integration.test.structure.blog;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;
import java.util.NoSuchElementException;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Sergii Moroz
 * Summary: This class describes elements that are displayed on blog pages
 */
public class BlogPage {
    private static final By ADD_NEW_POST_BUTTON = By.linkText("Add New Post");
    private static final By POST_TITLE_INPUT = By.id("post-title");
    private static final By POST_BODY_INPUT = By.id("tinymce");
    private static final By POST_BODY_IFRAME = By.cssSelector("iframe");
    private static final By SAVE_POST_BUTTON = By.cssSelector(".btn.main-action");
    private static final By EDIT_POST_BUTTON = By.cssSelector(".btn.secondary-action");
    private static final By COMMENT_CONTENT = By.className("comment-content");
    private static final By LEAVE_COMMENT_LINK = By.className("link");
    private static final By POST_ITEM = By.cssSelector(".post");
    private static final By GENERAL_BLOGS_LINK = By.linkText("Blogs");

    public Button addNewPostButton(){
        return new Button(ADD_NEW_POST_BUTTON);
    }

    public InputBox postTitleInput(){
        return new InputBox(POST_TITLE_INPUT);
    }

    public void clearAndFillInPostBodyInput(String postBody){
        WebElement iframe = getDriver().findElement(POST_BODY_IFRAME);
        WebElement inputBox = getDriver().switchTo().frame(iframe).findElement(POST_BODY_INPUT);
        inputBox.clear();
        inputBox.sendKeys(postBody);
        getDriver().switchTo().defaultContent();
    }

    public Button savePostButton(){
        return new Button(SAVE_POST_BUTTON);
    }

    public Button postLink(String title) {
        return new Button(By.linkText(title));
    }

    public WebElement editLastPostButton() {
        return getDriver().findElements(EDIT_POST_BUTTON).get(0);
    }

    public List<WebElement> readComments() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
        }
        return getDriver().findElements(COMMENT_CONTENT);
    }

    public Button leaveCommentLink() {
        return new Button(LEAVE_COMMENT_LINK);
    }

    public boolean isBlogEmpty() {
        List<WebElement> result;
        try {
            result = getDriver().findElements(POST_ITEM);
        }catch (NoSuchElementException e){
            return true;
        }
        return result.size()==0;

    }

    public Button generalBlogsPageLink() {
        return new Button(GENERAL_BLOGS_LINK);
    }
}
