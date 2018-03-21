package com.infoclinika.mssharing.integration.test.stepdefinitions.blogs;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.data.BlogPostData;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.HeaderMenuSteps;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class BlogPageSteps extends AbstractPageSteps {

    private static final Button ADD_NEW_POST_BUTTON = controlFactory().button(By.linkText("Add New Post"));

    private Button editButtonForPost(String postTitle) {
        return controlFactory().button(By.xpath("//*[text()='" + postTitle + "']/../../a"));
    }

    private Button leaveACommentLink(String postTitle) {
        return controlFactory().button(By.xpath("//div[contains(@class, 'post')][.//text()='" + postTitle + "']//a[text()='Leave a comment']"));
    }

    private Button postTitleLink(String title) {
        return controlFactory().button(By.xpath("//div[contains(@class, 'project-blog')]//div[@ng-repeat='post in posts']//a[text()='" + title + "']"));
    }

    public AddNewPostOrEditPageSteps clickAddNewPostButton() {
        ADD_NEW_POST_BUTTON.click();
        return new AddNewPostOrEditPageSteps();
    }

    public BlogPostSteps clickLeaveACommentLinkForPost(BlogPostData blogPostData) {
        leaveACommentLink(blogPostData.getTitle()).click();
        return new BlogPostSteps();
    }

    public BlogPostSteps clickOnPostTitle(BlogPostData blogPostData) {
        postTitleLink(blogPostData.getTitle()).click();
        return new BlogPostSteps();
    }

    public HeaderMenuSteps getHeaderMenu() {
        return new HeaderMenuSteps();
    }

    public AddNewPostOrEditPageSteps openBlogPostForEditing(String title) {
        editButtonForPost(title).scrollAndClick();
        return new AddNewPostOrEditPageSteps();
    }

    public boolean isPostDisplayed(BlogPostData blogPostData){
        return postTitleLink(blogPostData.getTitle()).isPresent();
    }

    //complex methods
    public BlogPageSteps createBlogPost(BlogPostData blogPostData) {
        return clickAddNewPostButton().fillInBlogPostForm(blogPostData).savePost();
    }

}
