package com.infoclinika.mssharing.integration.test.stepdefinitions.blogs;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Frame;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.data.BlogPostData;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class AddNewPostOrEditPageSteps extends AbstractPageSteps {

    private static final InputBox POST_TITLE_INPUT = controlFactory().inputBox(By.id("post-title"));
    private static final InputBox POST_BODY_INPUT = controlFactory().inputBox(By.id("tinymce"));
    private static final Frame POST_BODY_IFRAME = controlFactory().frame(By.cssSelector("iframe"));
    private static final Button SAVE_POST_BUTTON = controlFactory().button(By.xpath("//*[text()='Save Post' and not(@disabled)]"));

    public AddNewPostOrEditPageSteps fillInBlogPostTitle(String title) {
        POST_TITLE_INPUT.fillIn(title);
        return this;
    }

    public AddNewPostOrEditPageSteps fillInBlogPostBody(String postBody) {
        POST_BODY_IFRAME.switchToFrame();
        POST_BODY_INPUT.fillIn(postBody);
        POST_BODY_IFRAME.backOutOfFrame();
        return this;
    }

    public BlogPageSteps savePost() {
        SAVE_POST_BUTTON.scrollAndClick();
        return new BlogPageSteps();
    }

    public AddNewPostOrEditPageSteps fillInBlogPostForm(BlogPostData blogPostData) {
        fillInBlogPostTitle(blogPostData.getTitle());
        fillInBlogPostBody(blogPostData.getBody());
        if ("".equals(POST_TITLE_INPUT.getText())) {
            fillInBlogPostTitle(blogPostData.getTitle());
        }
        return this;
    }


}
