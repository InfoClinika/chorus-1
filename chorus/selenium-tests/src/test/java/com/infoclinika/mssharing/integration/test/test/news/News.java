package com.infoclinika.mssharing.integration.test.test.news;

import com.infoclinika.mssharing.integration.test.data.NewsData;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.HomePageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.NewsPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.news.NewsListSteps;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Orlov
 */
public class News extends BaseTest {

    @Test(dataProvider = "createNewsAndViewThemAsNotLoggedInUser", dataProviderClass = NewsDataProvider.class, groups = {"staging"})
    public void createNewsAndViewThemAsNotLoggedInUser(UserData admin, NewsData newsData) {
        DashboardPageSteps dashboardPageSteps = loginPageSteps.login(admin).pressOkInBillingNotificationDialog();
        NewsListSteps newsListSteps = dashboardPageSteps.getTopPanelSteps().openCreationMenu()
                .clickCreateNews()
                .specifyNewsTitle(newsData.getNewsTitle())
                .specifyCreatorEmail(newsData.getCreatorEmail())
                .specifyIntroduction(newsData.getIntroduction())
                .specifyText(newsData.getText())
                .pressCreateButton();
        assertTrue(newsListSteps.isNewsDisplayed(newsData.getNewsTitle()),
                "Created News do not appear in the News List");
        HomePageSteps homePageSteps = dashboardPageSteps.getHeader().logout();
        assertEquals(homePageSteps.getFirstNewsTitle(), newsData.getNewsTitle());
        assertEquals(homePageSteps.getFirstNewsIntroduction(), newsData.getIntroduction());
        NewsPageSteps newsPageSteps = homePageSteps.selectFirstNews();
        assertEquals(newsPageSteps.getFirstNewstTitle(), newsData.getNewsTitle());
        assertEquals(newsPageSteps.getFirstNewsText(), newsData.getText());
        //clearing data
        newsPageSteps.getHeader()
                .clickSignInLink()
                .login(admin)
                .getSidebarMenuSteps()
                .selectNews()
                .deleteNews(newsData.getNewsTitle());
    }

    @Test(dataProvider = "createNewsAndViewThemAsNotLoggedInUser", dataProviderClass = NewsDataProvider.class, groups = {"staging"})
    public void createNewsAndViewThemAsLoggedInUser(UserData admin, NewsData newsData) {
        DashboardPageSteps dashboardPageSteps = loginPageSteps.login(admin).pressOkInBillingNotificationDialog();
        NewsListSteps newsListSteps = dashboardPageSteps
                .getTopPanelSteps().openCreationMenu()
                .clickCreateNews()
                .specifyNewsTitle(newsData.getNewsTitle())
                .specifyCreatorEmail(newsData.getCreatorEmail())
                .specifyIntroduction(newsData.getIntroduction())
                .specifyText(newsData.getText())
                .pressCreateButton();
        assertTrue(newsListSteps.isNewsDisplayed(newsData.getNewsTitle()),
                "Created News do not appear in the News List");
        HomePageSteps homePageSteps = dashboardPageSteps.getHeader().pressHomeLink();
        assertEquals(homePageSteps.getFirstNewsTitle(), newsData.getNewsTitle());
        assertEquals(homePageSteps.getFirstNewsIntroduction(), newsData.getIntroduction());
        NewsPageSteps newsPageSteps = homePageSteps.selectFirstNews();
        assertEquals(newsPageSteps.getFirstNewstTitle(), newsData.getNewsTitle());
        assertEquals(newsPageSteps.getFirstNewsText(), newsData.getText());
        //clearing data
        newsPageSteps.getHeader()
                .selectApplication()
                .getSidebarMenuSteps()
                .selectNews()
                .deleteNews(newsData.getNewsTitle());
    }
}
