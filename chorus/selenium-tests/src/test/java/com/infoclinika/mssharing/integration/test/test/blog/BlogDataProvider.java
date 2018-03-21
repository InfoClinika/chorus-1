package com.infoclinika.mssharing.integration.test.test.blog;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.integration.test.data.BlogPostData;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class BlogDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] createNewBlogAndPost() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .hasBlog(true).build();
        BlogPostData blogPostData = new BlogPostData.Builder()
                .title(randomizeName("Post Title"))
                .body(randomizeName("Post Body")).build();
        return new Object[][]{{projectData, blogPostData}};
    }

    @DataProvider
    public static Object[][] editBlogPost() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .hasBlog(true).build();
        BlogPostData blogPostData = new BlogPostData.Builder()
                .title(randomizeName("Post Title"))
                .body(randomizeName("Post Body")).build();
        BlogPostData editedBlogPostData = new BlogPostData.Builder()
                .title(randomizeName("Edited Post Title"))
                .body(randomizeName("Edited Post Body")).build();
        return new Object[][]{{projectData, blogPostData, editedBlogPostData}};
    }

    @DataProvider
    public static Object[][] addBlogComments() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .hasBlog(true).build();
        BlogPostData blogPostData = new BlogPostData.Builder()
                .title(randomizeName("Post Title"))
                .body(randomizeName("Post Body"))
                .comments(Lists.newArrayList(randomizeName("Comment#1"), randomizeName("Comment#2"))).build();
        return new Object[][]{{projectData, blogPostData}};
    }

    @DataProvider
    public static Object[][] blogCommentsUsingLink() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .hasBlog(true).build();
        BlogPostData blogPostData = new BlogPostData.Builder()
                .title(randomizeName("Post Title"))
                .body(randomizeName("Post Body"))
                .comments(Lists.newArrayList(randomizeName("Comment#1"), randomizeName("Comment#2"))).build();
        return new Object[][]{{projectData, blogPostData}};
    }

    @DataProvider
    public static Object[][] removeBlogPost() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .hasBlog(true).build();
        BlogPostData blogPostData = new BlogPostData.Builder()
                .title(randomizeName("Post Title"))
                .body(randomizeName("Post Body")).build();
        return new Object[][]{{projectData, blogPostData}};
    }

    @DataProvider
    public static Object[][] latestBlogPostsBlock() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .hasBlog(true).build();
        BlogPostData blogPostData = new BlogPostData.Builder()
                .title(randomizeName("Post Title"))
                .body(randomizeName("Post Body")).build();
        return new Object[][]{{projectData, blogPostData}};
    }

    @DataProvider
    public static Object[][] allBlogsLatestTable() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .hasBlog(true).build();
        BlogPostData blogPostData = new BlogPostData.Builder()
                .title(randomizeName("Post Title"))
                .body(randomizeName("Post Body")).build();
        return new Object[][]{{projectData, blogPostData}};
    }

}
