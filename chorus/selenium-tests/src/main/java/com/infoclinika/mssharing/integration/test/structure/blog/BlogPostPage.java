package com.infoclinika.mssharing.integration.test.structure.blog;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class BlogPostPage {
    private static final By POST_TITLE = By.cssSelector("[ng-bind='post.title']");
    private static final By POST_BODY = By.cssSelector("[ng-bind-html-unsafe='post.content']");
    private static final By COMMENT_TEXTAREA = By.cssSelector("[ng-model='newComment.content']");
    private static final By POST_COMMENT_BUTTON = By.cssSelector(".comment-form .btn.main-action");
    private static final By BLOG_NAME_LINK = By.cssSelector("[ng-bind='post.blog.name']");
    private static final By REMOVE_POST_BUTTON = By.cssSelector("[ng-click='removeConfirmationDialog.showConfirmation()']");
    private static final By REMOVE_CONFIRMATION_BUTTON = By.cssSelector(".modal-frame .btn.btn-primary");

    public Label postTitle(){
        return new Label(POST_TITLE);
    }

    public Label postBody(){
        return new Label(POST_BODY);
    }

    public InputBox commentField() {
        return new InputBox(COMMENT_TEXTAREA);
    }

    public Button postCommentButton(){
        return new Button(POST_COMMENT_BUTTON);
    }

    public Button blogNameLink() {
        return new Button(BLOG_NAME_LINK);
    }

    public Button removeButton() {
        return new Button(REMOVE_POST_BUTTON);
    }

    public Button confirmationButton() {
        return new Button(REMOVE_CONFIRMATION_BUTTON);
    }
}
