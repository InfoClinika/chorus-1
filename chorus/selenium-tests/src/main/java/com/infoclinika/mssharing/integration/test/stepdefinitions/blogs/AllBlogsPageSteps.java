package com.infoclinika.mssharing.integration.test.stepdefinitions.blogs;

import com.infoclinika.mssharing.integration.test.structure.blog.BlogsGeneralPage;

/**
 * @author Sergii Moroz
 */
public class AllBlogsPageSteps {
    BlogsGeneralPage blogGeneralPage = new BlogsGeneralPage();

    public String getLatestPostProjectByIndex(int index){
        return blogGeneralPage.latestPostProject(index).getText();
    }

    public String getSmallLatestPostProjectByIndex(int index){
        return blogGeneralPage.smallLatestPostProject(index).getText();
    }

    public String getProjectNameInTableByIndex(int rowIndex) {
        //-1 in index is used to avoid '0' index in test for better clearance
        return blogGeneralPage.getBlogProjectNameFromBlogsTable(rowIndex-1).getText();
    }

    public String getPostTitleAndBodyInTableByIndex(int rowIndex) {
        //-1 in index is used to avoid '0' index in test for better clearance
        return blogGeneralPage.getBlogPostTitleAndBodyFromBlogsTable(rowIndex-1).getText().trim();
    }
}
