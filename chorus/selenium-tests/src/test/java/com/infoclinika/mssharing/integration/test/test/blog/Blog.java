package com.infoclinika.mssharing.integration.test.test.blog;

import com.infoclinika.mssharing.integration.test.data.BlogPostData;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.blogs.AddNewPostOrEditPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.blogs.BlogPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.blogs.BlogPostSteps;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Sergii Moroz
 */
public class Blog extends BaseTest {

    @Test(dataProvider = "createNewBlogAndPost", dataProviderClass = BlogDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createNewBlogAndPost(ProjectData projectData, BlogPostData blogPostData) {
        BlogPageSteps blogPageSteps = dashboardPageSteps
                .createPrivateProject(projectData)
                .getProjectsListSteps()
                .openProjectBlog(projectData.getName())
                .clickAddNewPostButton()
                .fillInBlogPostForm(blogPostData)
                .savePost();
        BlogPostSteps blogPostSteps = blogPageSteps.clickOnPostTitle(blogPostData);
        assertEquals(blogPostSteps.getTitle(), blogPostData.getTitle());
        assertEquals(blogPostSteps.getBody(), blogPostData.getBody());
        //clearing data
        dashboardPageSteps = blogPageSteps.getHeaderMenu().selectApplication();
        dashboardPageSteps.getProjectsListSteps()
                .deletePrivateProject(projectData.getName());
    }

    @Test(dataProvider = "editBlogPost", dataProviderClass = BlogDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void editBlogPost(ProjectData projectData, BlogPostData blogPostData, BlogPostData editedBlogPostData) {
        AddNewPostOrEditPageSteps addNewPostOrEditPageSteps = dashboardPageSteps
                .createPrivateProject(projectData)
                .getProjectsListSteps()
                .openProjectBlog(projectData.getName())
                .createBlogPost(blogPostData)
                .openBlogPostForEditing(blogPostData.getTitle())
                .fillInBlogPostForm(editedBlogPostData);
        addNewPostOrEditPageSteps.savePost();
        BlogPageSteps blogPageSteps = new BlogPageSteps();
        BlogPostSteps blogPostSteps = blogPageSteps.clickOnPostTitle(editedBlogPostData);
        assertEquals(blogPostSteps.getTitle(), editedBlogPostData.getTitle());
        assertEquals(blogPostSteps.getBody(), editedBlogPostData.getBody());
        //clearing data
        dashboardPageSteps = blogPageSteps.getHeaderMenu().selectApplication();
        dashboardPageSteps.getProjectsListSteps()
                .deletePrivateProject(projectData.getName());
    }

    @Test(dataProvider = "addBlogComments", dataProviderClass = BlogDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void addBlogComments(ProjectData projectData, BlogPostData blogPostData) {
        BlogPostSteps blogPostSteps = dashboardPageSteps
                .createPrivateProject(projectData)
                .getProjectsListSteps()
                .openProjectBlog(projectData.getName()).createBlogPost(blogPostData)
                .clickOnPostTitle(blogPostData)
                .leaveAComment(blogPostData.getComments().get(0))
                .leaveAComment(blogPostData.getComments().get(1));
        List<String> actualComments = blogPostSteps.readComments();
        assertEquals(actualComments.size(), blogPostData.getComments().size(), "Number of comments are not equal to the expected value");
        assertTrue(actualComments.containsAll(blogPostData.getComments()), "Comments on the page are not equal to the expected ones");
        //clearing data
        dashboardPageSteps.getHeader().selectApplication();
        dashboardPageSteps.getProjectsListSteps()
                .deletePrivateProject(projectData.getName());
    }

    @Test(dataProvider = "blogCommentsUsingLink", dataProviderClass = BlogDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void blogCommentsUsingLink(ProjectData projectData, BlogPostData blogPostData) {
        BlogPostSteps blogPostSteps = dashboardPageSteps
                .createPrivateProject(projectData)
                .getProjectsListSteps()
                .openProjectBlog(projectData.getName()).createBlogPost(blogPostData)
                .clickLeaveACommentLinkForPost(blogPostData)
                .leaveAComment(blogPostData.getComments().get(0))
                .leaveAComment(blogPostData.getComments().get(1));
        List<String> actualComments = blogPostSteps.readComments();
        assertEquals(actualComments.size(), blogPostData.getComments().size(), "Number of comments are not equal to the expected value");
        assertTrue(actualComments.containsAll(blogPostData.getComments()), "Comments on the page are not equal to the expected ones");
        //clearing data
        dashboardPageSteps.getHeader().selectApplication();
        dashboardPageSteps.getProjectsListSteps()
                .deletePrivateProject(projectData.getName());
    }

    @Test(dataProvider = "removeBlogPost", dataProviderClass = BlogDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void removeBlogPost(ProjectData projectData, BlogPostData blogPostData) {
        BlogPostSteps blogPostSteps = dashboardPageSteps
                .createPrivateProject(projectData)
                .getProjectsListSteps()
                .openProjectBlog(projectData.getName()).createBlogPost(blogPostData)
                .clickOnPostTitle(blogPostData);
        assertEquals(blogPostSteps.getTitle(), blogPostData.getTitle());
        assertEquals(blogPostSteps.getBody(), blogPostData.getBody());
        BlogPageSteps blogPageSteps = blogPostSteps.deleteBlogPost();
        assertFalse(blogPageSteps.isPostDisplayed(blogPostData), "Blog Post isn't deleted");
        //clearing data
        dashboardPageSteps = dashboardPageSteps.getHeader().selectApplication();
        dashboardPageSteps.getProjectsListSteps()
                .deletePrivateProject(projectData.getName());
    }
//
//    @Test(dataProvider = "latestBlogPostsBlock", dataProviderClass = BlogDataProvider.class, groups = {"staging"})
//    @LoginRequired
//    public void latestBlogPostsBlock() {
//        //setup
//        BlogPostData blogPostData2 = new BlogPostData();
//        BlogPostData blogPostData3 = new BlogPostData();
//        BlogPostData blogPostData4 = new BlogPostData();
//        blogGeneralSteps.createBlogPost(blogPostData);
//        blogGeneralSteps.createBlogPost(blogPostData2);
//        blogGeneralSteps.createBlogPost(blogPostData3);
//        blogGeneralSteps.createBlogPost(blogPostData4);
//        //test
//        BlogsPageSteps blogsPageSteps = blogGeneralSteps.openBlogsGeneralPage();
//        assertEquals(blogsPageSteps.getLatestPostProjectByIndex(1), projectData.getName(), "Project name is incorrect");
//        assertTrue(blogsPageSteps.getSpecifiedLatestPostData(1).equals(blogPostData4), "Latest post is incorrect");
//        assertEquals(blogsPageSteps.getLatestPostProjectByIndex(2), projectData.getName(), "Project name is incorrect");
//        assertTrue(blogsPageSteps.getSpecifiedLatestPostData(2).equals(blogPostData3), "Latest post is incorrect");
//        assertEquals(blogsPageSteps.getSmallLatestPostProjectByIndex(1), projectData.getName(), "Project name is incorrect");
//        assertTrue(blogsPageSteps.getSpecifiedSmallLatestPostData(1).equals(blogPostData2), "Latest post is incorrect");
//        assertEquals(blogsPageSteps.getSmallLatestPostProjectByIndex(2), projectData.getName(), "Project name is incorrect");
//        assertTrue(blogsPageSteps.getSpecifiedSmallLatestPostData(2).equals(blogPostData), "Latest post is incorrect");
//        //clearing data
//        dashboardPageSteps = blogGeneralSteps.returnToApplication();
//        dashboardPageSteps.getProjectsListSteps()
//                .deletePrivateProject(projectData.getName());
//    }
//
//    @Test(dataProvider = "latestBlogPostsBlock", dataProviderClass = BlogDataProvider.class, groups = {"staging"})
//    @LoginRequired
//    public void allBlogsLatestTable(ProjectData projectData2, ProjectData projectData3) {
//        //setup
//        blogGeneralSteps.returnToApplication();
////        ProjectData projectData2 = new ProjectData();
////        projectData2.setHasBlog(true);
////        ProjectData projectData3 = new ProjectData();
////        projectData3.setHasBlog(true);
//        BlogPostData blogPostData2 = new BlogPostData();
//        BlogPostData blogPostData3 = new BlogPostData();
//        dashboardPageSteps.createPrivateProject(projectData2);
//        dashboardPageSteps.createPrivateProject(projectData3);
//        //test
//        blogGeneralSteps = dashboardPageSteps.getProjectsListSteps()
//                .openProjectBlog(projectData.getName());
//        blogGeneralSteps.createBlogPost(blogPostData)
//                .returnToApplication();
//        blogGeneralSteps = dashboardPageSteps.getProjectsListSteps()
//                .openProjectBlog(projectData2.getName());
//        blogGeneralSteps.createBlogPost(blogPostData2)
//                .returnToApplication();
//        blogGeneralSteps = dashboardPageSteps.getProjectsListSteps()
//                .openProjectBlog(projectData3.getName());
//        BlogsPageSteps blogsPageSteps = blogGeneralSteps.createBlogPost(blogPostData3)
//                .openBlogsGeneralPage();
//        assertEquals(blogsPageSteps.getProjectNameInTableByIndex(1), projectData3.getName(), "Project order is incorrect");
//        assertEquals(blogsPageSteps.getProjectNameInTableByIndex(2), projectData2.getName(), "Project order is incorrect");
//        assertEquals(blogsPageSteps.getProjectNameInTableByIndex(3), projectData.getName(), "Project order is incorrect");
//        assertEquals(blogsPageSteps.getPostTitleAndBodyInTableByIndex(1), blogPostData3.getTitle() + " " + blogPostData3.getBody(), "Project order is incorrect");
//        assertEquals(blogsPageSteps.getPostTitleAndBodyInTableByIndex(2), blogPostData2.getTitle() + " " + blogPostData2.getBody(), "Project order is incorrect");
//        assertEquals(blogsPageSteps.getPostTitleAndBodyInTableByIndex(3), blogPostData.getTitle() + " " + blogPostData.getBody(), "Project order is incorrect");
//        //clearing data
//        dashboardPageSteps = blogGeneralSteps.returnToApplication();
//        dashboardPageSteps.getProjectsListSteps()
//                .deleteProjects(projectData, projectData2, projectData3);
//    }
}
