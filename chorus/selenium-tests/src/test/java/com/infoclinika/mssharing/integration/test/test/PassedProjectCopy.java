package com.infoclinika.mssharing.integration.test.test;

import com.infoclinika.mssharing.integration.test.data.BlogPostData;
import com.infoclinika.mssharing.integration.test.data.projectdata.PersonToInvite;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.helper.TestManager;
import com.infoclinika.mssharing.integration.test.stepdefinitions.blogs.BlogPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.project.ProjectEditGeneralTabSteps;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;
import static org.testng.Assert.*;

/**
 * @author Sergii Moroz
 */
public class PassedProjectCopy extends TestManager {
    ProjectData projectData;
    private String textFactorName = "text factor";
    private String numberFactorName = "number factor";
    private String text_condition_value1 = "value1";
    private String text_condition_value2 = "value2";
    private String text_condition_value3 = "value3";
    private String text_condition_value4 = "value4";
    private String text_condition_value5 = "value5";
    private String text_condition_value6 = "value5";
    private String number_condition_value1 = "123";
    private String number_condition_value2 = "456";
    private String number_condition_units = "some unit";

    @BeforeMethod
    public void beforeMethod(Method method) {
        startDriver();
        List<PersonToInvite> personToInviteList = new ArrayList<>();
        personToInviteList.add(new PersonToInvite.Builder()
                .name(SampleData.PERSON_NAME_KARREN_KOE)
                .email(SampleData.PERSON_EMAIL_KARREN_KOE).build());
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .description("Description")
                .personToInvite(personToInviteList).build();
        dashboardPageSteps = setup();
        logMethodName(method);
    }

//    @Test
//    public void passProjectCopy(ExperimentData experimentData) {
        //setup
//        ExperimentData experimentData = new ExperimentData(
//                randomizeName("Experiment"),
//                projectData.getName(),
//                SampleData.SPECIES_ARABIDOPSIS_THALIANA,
//                SampleData.EXPERIMENT_TYPE_METABOLOMICS,
//                SampleData.WORKFLOW_TYPE_LABEL_FREE_DIFFERENTIAL,
//                false,
//                SampleData.LAB_FIRST_CHORUS,
//                SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS,
//                SampleData.INSTRUMENT_CELL_MACHINE,
//                "Some Description"
//        );
//        experimentData.setRtMzValues(1, 2, 1, 2, 5.1);
        //test
//        dashboardPageSteps.createPrivateProject(projectData)
//                .createExperiment(experimentData)
//                .openMyProjectsList()
//                .passACopy(projectData.getName(), pavelKaplinAtTeamdev);
//        HomePageSteps homePageSteps = dashboardPageSteps.getTopPanelSteps()
//                .logout();
//        homePageSteps.pressSignInButton()
//                .login(pavelKaplinAtTeamdev)
//                .getSidebarMenuSteps()
//                .selectInbox()
//                .pressApproveButtonForItem(pavelKaplinAtGmail.getFullName(), projectData.getName())
//                .approveProjectCopying();
//        dashboardPageSteps.openMyProjectsList();
//        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(projectData.getName()),
//                "Project copy is not appeared");
//        dashboardPageSteps.openMyExperimentsList();
//        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentData.getName()),
//                "Experiment copy is not appeared after creating");
//        //clearing data
//        dashboardPageSteps.openMyProjectsList()
//                .deletePrivateProject(projectData.getName());
//        dashboardPageSteps.getTopPanelSteps()
//                .logout()
//                .pressSignInButton()
//                .login(pavelKaplinAtGmail)
//                .getProjectsListSteps()
//                .deletePrivateProject(projectData.getName());
//    }

//    @Test(enabled = false)
//    public void passProjectCopyWithExperimentWithFactorsWith2DLCoption() {
//        //setup
//        ExperimentData experimentData = new ExperimentData(
//                randomizeName("Experiment"),
//                projectData.getName(),
//                SampleData.SPECIES_ARABIDOPSIS_THALIANA,
//                SampleData.EXPERIMENT_TYPE_METABOLOMICS,
//                SampleData.WORKFLOW_TYPE_LABEL_FREE_DIFFERENTIAL,
//                false,
//                SampleData.LAB_FIRST_CHORUS,
//                SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS,
//                SampleData.INSTRUMENT_CELL_MACHINE,
//                "Some Description"
//        );
//        experimentData.set2DLC(true);
//        int numberOfFiles = 2;
//        String textFactorName = "text factor";
//        String numberFactorName = "number factor";
//        //test
//        dashboardPageSteps.createPrivateProject(projectData);
//        dashboardPageSteps
//                .getTopPanelSteps()
//                .openCreationMenu()
//                .clickCreateExperiment()
//                .fillInFormAndPressNext(experimentData)
//                .selectSeveralFilesAndPressNext(numberOfFiles)
//                .fillInAnalysisFormAndPressNext(experimentData)
//                .addTextFactor(textFactorName)
//                .addNumberFactor(numberFactorName, number_condition_units)
//                .fillInExactFactorValue(1, 1, text_condition_value1, experimentData.getIs2dlc())
//                .fillInExactFactorValue(1, 2, text_condition_value2, experimentData.getIs2dlc())
//                .fillInExactFactorValue(1, 3, text_condition_value3, experimentData.getIs2dlc())
//                .fillInExactFactorValue(1, 4, number_condition_value1, experimentData.getIs2dlc())
//                .fillInExactFactorValue(2, 1, text_condition_value4, experimentData.getIs2dlc())
//                .fillInExactFactorValue(2, 2, text_condition_value5, experimentData.getIs2dlc())
//                .fillInExactFactorValue(2, 3, text_condition_value6, experimentData.getIs2dlc())
//                .fillInExactFactorValue(2, 4, number_condition_value2, experimentData.getIs2dlc())
//                .pressNext()
//                .pressConfirmAndCreate();
//        dashboardPageSteps.openMyProjectsList()
//                .passACopy(projectData.getName(), pavelKaplinAtTeamdev);
//        HomePageSteps homePageSteps = dashboardPageSteps.getTopPanelSteps()
//                .logout();
//        homePageSteps.pressSignInButton()
//                .login(pavelKaplinAtTeamdev);
//        dashboardPageSteps.openMyProjectsList();
//        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(projectData.getName()),
//                "Project copy is not appeared");
//        dashboardPageSteps.openMyExperimentsList();
//        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentData.getName()),
//                "Experiment copy is not appeared after creating");
//        ExperimentConfirmationSteps experimentConfirmationSteps = dashboardPageSteps.getExperimentListSteps()
//                .openExperimentDetails(experimentData.getName())
//                .pressNext()
//                .pressNext()
//                .pressNext()
//                .pressNext();
//        assertEquals(experimentConfirmationSteps.getNumberOfFilesInConfirmationList(), numberOfFiles);
//        assertEquals(experimentConfirmationSteps.getConditionValue(1), textFactorName + ":" + text_condition_value3 + ", " +
//                numberFactorName + ":" + number_condition_value1 + "(" + number_condition_units + ")");
//        assertEquals(experimentConfirmationSteps.getConditionValue(2), textFactorName + ":" + text_condition_value6 + ", " +
//                numberFactorName + ":" + number_condition_value2 + "(" + number_condition_units + ")");
//        experimentConfirmationSteps.pressConfirmAndCreate();
//        //clearing data
//        dashboardPageSteps.openMyProjectsList()
//                .deletePrivateProject(projectData.getName());
//        dashboardPageSteps.getTopPanelSteps()
//                .logout()
//                .pressSignInButton()
//                .login(pavelKaplinAtGmail)
//                .getProjectsListSteps()
//                .deletePrivateProject(projectData.getName());
//    }
//
//    @Test(enabled = false)
//    public void passProjectCopyWithExperimentWithFactorsWithout2DLCoption() {
//        //setup
//        ExperimentData experimentData = new ExperimentData(
//                randomizeName("Experiment"),
//                projectData.getName(),
//                SampleData.SPECIES_ARABIDOPSIS_THALIANA,
//                SampleData.EXPERIMENT_TYPE_METABOLOMICS,
//                SampleData.WORKFLOW_TYPE_LABEL_FREE_DIFFERENTIAL,
//                false,
//                SampleData.LAB_FIRST_CHORUS,
//                SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS,
//                SampleData.INSTRUMENT_CELL_MACHINE,
//                "Some Description"
//        );
//        int numberOfFiles = 2;
//        //test
//        dashboardPageSteps.createPrivateProject(projectData);
//        dashboardPageSteps
//                .getTopPanelSteps()
//                .openCreationMenu()
//                .clickCreateExperiment()
//                .fillInFormAndPressNext(experimentData)
//                .selectSeveralFilesAndPressNext(numberOfFiles)
//                .fillInAnalysisFormAndPressNext(experimentData)
//                .addTextFactor(textFactorName)
//                .addNumberFactor(numberFactorName, number_condition_units)
//                .fillInExactFactorValue(1, 1, text_condition_value1, experimentData.getIs2dlc())
//                .fillInExactFactorValue(1, 2, number_condition_value1, experimentData.getIs2dlc())
//                .fillInExactFactorValue(2, 1, text_condition_value2, experimentData.getIs2dlc())
//                .fillInExactFactorValue(2, 2, number_condition_value2, experimentData.getIs2dlc())
//                .pressNext()
//                .pressConfirmAndCreate();
//        dashboardPageSteps.openMyProjectsList()
//                .passACopy(projectData.getName(), pavelKaplinAtTeamdev);
//        HomePageSteps homePageSteps = dashboardPageSteps.getTopPanelSteps()
//                .logout();
//        homePageSteps.pressSignInButton()
//                .login(pavelKaplinAtTeamdev);
//        dashboardPageSteps.openMyProjectsList();
//        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(projectData.getName()),
//                "Project copy is not appeared");
//        dashboardPageSteps.openMyExperimentsList();
//        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentData.getName()),
//                "Experiment copy is not appeared after creating");
//        ExperimentConfirmationSteps experimentConfirmationSteps = dashboardPageSteps.getExperimentListSteps()
//                .openExperimentDetails(experimentData.getName())
//                .pressNext()
//                .pressNext()
//                .pressNext()
//                .pressNext();
//        assertEquals(experimentConfirmationSteps.getNumberOfFilesInConfirmationList(), numberOfFiles);
//        assertEquals(experimentConfirmationSteps.getConditionValue(1), textFactorName + ":" + text_condition_value1 + ", " +
//                numberFactorName + ":" + number_condition_value1 + "(" + number_condition_units + ")");
//        assertEquals(experimentConfirmationSteps.getConditionValue(2), textFactorName + ":" + text_condition_value2 + ", " +
//                numberFactorName + ":" + number_condition_value2 + "(" + number_condition_units + ")");
//        experimentConfirmationSteps.pressConfirmAndCreate();
//        //clearing data
//        dashboardPageSteps.openMyProjectsList()
//                .deletePrivateProject(projectData.getName());
//        dashboardPageSteps.getTopPanelSteps()
//                .logout()
//                .pressSignInButton()
//                .login(pavelKaplinAtGmail)
//                .getProjectsListSteps()
//                .deletePrivateProject(projectData.getName());
//    }

    @Test
    public void passACopyWithBlog(BlogPostData blogPostData) {
        //setup
//        projectData.setHasBlog(true);
        dashboardPageSteps.createPrivateProject(projectData)
                .getProjectsListSteps()
                .openProjectBlog(projectData.getName())
                .clickAddNewPostButton()
                .fillInBlogPostForm(blogPostData)
                .savePost()
                .getHeaderMenu().selectApplication();
        //test
        dashboardPageSteps.getProjectsListSteps()
                .passACopy(projectData.getName(), pavelKaplinAtTeamdev);
        dashboardPageSteps.getHeader()
                .logout()
                .pressSignInButton()
                .login(pavelKaplinAtTeamdev)
                .getSidebarMenuSteps()
                .selectInbox()
                .pressApproveButtonForItem(pavelKaplinAtGmail.getFullName(), projectData.getName())
                .approveProjectCopying();
        dashboardPageSteps.openMyProjectsList();
        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(projectData.getName()),
                "Project copy is not appeared");
        ProjectEditGeneralTabSteps projectGeneralTabSteps = dashboardPageSteps.getProjectsListSteps()
                .openProjectForEditing(projectData.getName());
        assertFalse(projectGeneralTabSteps.isBlogEnabled(), "Blog option is enabled for passed copy");
        BlogPageSteps blogGeneralSteps = projectGeneralTabSteps.enableBlog(projectData.getHasBlog())
                .pressSaveButtonAndWait()
                .getProjectsListSteps()
                .openProjectBlog(projectData.getName());
//        assertTrue(blogGeneralSteps.isBlogEmpty(), "Unexpected posts appear in blog for passed copy");
    }
}
