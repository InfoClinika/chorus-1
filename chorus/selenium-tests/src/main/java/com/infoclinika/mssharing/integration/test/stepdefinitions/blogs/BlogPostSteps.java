package com.infoclinika.mssharing.integration.test.stepdefinitions.blogs;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.data.BlogPostData;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.fail;

/**
 * @author Sergii Moroz
 */
public class BlogPostSteps extends AbstractPageSteps {

    private static final Label POST_TITLE = controlFactory().label(By.cssSelector("h2[ng-bind='post.title']"));
    private static final Label POST_BODY = controlFactory().label(By.cssSelector(".post-content"));
    private static final InputBox LEAVE_A_COMMENT_FIELD = controlFactory().inputBox(By.xpath("//*[@ng-model='newComment.content']"));
    private static final Button POST_COMMENT_BUTTON = controlFactory().button(By.xpath("//*[@id='commentForm']//button"));
    private static final Pane COMMENT_SECTION = controlFactory().pane(By.xpath("//*[@class='comment-holder']//div[contains(@class, 'comment-item')]"));
    private static final Button REMOVE_POST_BUTTON = controlFactory().button(By.linkText("Remove"));
    private static final Button REMOVE_POST_CONFIRMATION_BUTTON = controlFactory().button(By.xpath("//*[@id='remove-post-confirmation']//button[@type='submit']"));

    private Label commentTextByIndex(int index) {
        return new Label(By.xpath("//*[@class='comment-holder']/div[" + index + "]//*[@ng-bind-html-unsafe='comment.content']"));
    }

    private Pane commentSectionByText(String text){
        return new Pane(By.xpath("//*[@class='comment-holder']//div[contains(text(), '" + text + "')]"));
    }

    public String getTitle() {
        POST_TITLE.waitForElementToBeVisible();
        return POST_TITLE.getText();
    }

    public String getBody() {
        return POST_BODY.getText();
    }

    public BlogPostSteps leaveAComment(String comment) {
        LEAVE_A_COMMENT_FIELD.fillInAndWaitForTextAppearing(comment);
        POST_COMMENT_BUTTON.waitForButtonToBeEnabled();
        POST_COMMENT_BUTTON.click();
        wait(1);
        return this;
    }

    public List<String> readComments(){
        int numberOfComments = COMMENT_SECTION.getNumberOfAll();
        List<String> allComments = new ArrayList<>();
        for (int i=1; i<numberOfComments+1; i++){
            allComments.add(commentTextByIndex(i).getText());
        }
        return allComments;
    }

    public BlogPageSteps deleteBlogPost(){
        REMOVE_POST_BUTTON.click();
        REMOVE_POST_CONFIRMATION_BUTTON.waitForElementToBeClickable();
        REMOVE_POST_CONFIRMATION_BUTTON.click();
        return new BlogPageSteps();
    }

}
