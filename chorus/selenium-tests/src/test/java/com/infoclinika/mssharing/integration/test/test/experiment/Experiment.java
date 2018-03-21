package com.infoclinika.mssharing.integration.test.test.experiment;

import com.infoclinika.mssharing.integration.test.data.*;
import com.infoclinika.mssharing.integration.test.data.experiment.ExperimentData;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.HomePageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.experiment.*;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static com.infoclinika.mssharing.integration.test.utils.DownloadsHandler.testAnonymousDownload;
import static org.testng.Assert.*;

/**
 * @author Sergii Moroz
 */
public class Experiment extends BaseTest {

    @Test(dataProvider = "Create Experiment while staying on 'All Projects' Page", dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createExperimentStayingOnAllProjectsPage(ExperimentData experimentData) {
        dashboardPageSteps.createExperiment(experimentData);
        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentData.getGeneralInfo().getName()),
                "Experiment doesn't appear after creating");
        //clearing data
        dashboardPageSteps.getSidebarMenuSteps()
                .selectExperimentsItem()
                .deleteExperiment(experimentData.getGeneralInfo().getName());
    }

    @Test(dataProvider = "Create Experiment while staying on 'All Experiments' Page", dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createExperimentStayingOnAllExperimentsPage(ExperimentData experimentData) {
        dashboardPageSteps
                .getSidebarMenuSteps()
                .selectExperimentsItem();
        dashboardPageSteps.createExperiment(experimentData);
        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentData.getGeneralInfo().getName()),
                "Experiment doesn't appear after creating");
        //clearing data
        dashboardPageSteps
                .getExperimentListSteps()
                .deleteExperiment(experimentData.getGeneralInfo().getName());
    }

    @Test(dataProvider = "Create Experiment with factors", dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createExperimentWithFactors(ExperimentData experimentData) {
        dashboardPageSteps.createExperiment(experimentData);
        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentData.getGeneralInfo().getName()),
                "Experiment doesn't appear after creating");
        //clearing data
        dashboardPageSteps.getExperimentListSteps()
                .deleteExperiment(experimentData.getGeneralInfo().getName());
    }

    @Test(dataProvider = "Share and download Experiment", dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void shareAndDownloadExperiment(ProjectData projectData, ExperimentData experimentData, UserData userData) {
        dashboardPageSteps.createSharedProject(projectData)
                .createExperiment(experimentData);
        String shareLink = dashboardPageSteps.getExperimentListSteps()
                .getExperimentShareLink(experimentData.getGeneralInfo().getName());
        HomePageSteps homePageSteps = dashboardPageSteps.getHeader()
                .logout();
        assertTrue(testAnonymousDownload(shareLink), "Download isn't work");
        //clearing data
        homePageSteps.pressSignInButton()
                .login(userData)
                .getProjectsListSteps()
                .makeProjectPrivate(projectData.getName())
                .deletePrivateProject(projectData.getName());
    }

    @Test(dataProvider = "Edit Experiment", dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void editExperiment(ExperimentData experimentData, ExperimentData newExperimentData) {
        ExperimentFileSelectionSteps experimentFileSelection = dashboardPageSteps.createExperiment(experimentData)
                .getExperimentListSteps()
                .openExperimentDetails(experimentData.getGeneralInfo().getName())
                .specifyName(newExperimentData.getGeneralInfo().getName())
                .selectProject(newExperimentData.getGeneralInfo().getProject())
                .specifyDescription(newExperimentData.getGeneralInfo().getDescription())
                .pressNext()
                .selectFiles(newExperimentData.getFileSelectionInfo().getNumberOfSelectedFiles());
        ExperimentConfirmationSteps experimentConfirmation = experimentFileSelection.pressNext()
                .fillInAnalysisFormAndPressNext(newExperimentData.getAnalysisInfo())
                .pressNext().pressNext().pressNext();

        assertEquals(experimentConfirmation.getExperimentName(), newExperimentData.getGeneralInfo().getName());
        assertEquals(experimentConfirmation.getExperimentProject(), newExperimentData.getGeneralInfo().getProject());
        assertEquals(experimentConfirmation.getExperimentSpecie(), newExperimentData.getGeneralInfo().getSpecies());
        assertEquals(experimentConfirmation.getExperimentLab(), newExperimentData.getGeneralInfo().getLaboratory());
        assertEquals(experimentConfirmation.getExperimentNumberOfFiles(), newExperimentData.getFileSelectionInfo().getNumberOfSelectedFiles());
        assertEquals(experimentConfirmation.getNumberOfFilesInConfirmationList(), newExperimentData.getFileSelectionInfo().getNumberOfSelectedFiles(),
                "Number of files on the Confirmation Step are not equal to the number of files that were selected on the File Selection Step");

        experimentConfirmation.pressConfirmAndCreate();

        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(newExperimentData.getGeneralInfo().getName()),
                "Changes aren't saved");
        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentNotDisplayed(experimentData.getGeneralInfo().getName()),
                "Experiment with an old name still displayed");
        //clearing data
        dashboardPageSteps
                .getExperimentListSteps()
                .deleteExperiment(newExperimentData.getGeneralInfo().getName());
    }

    @Test(dataProvider = "Remove Experiment", dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void removeExperiment(ExperimentData experimentData) {
        ExperimentListSteps experimentList = dashboardPageSteps
                .getSidebarMenuSteps()
                .selectExperimentsItem();
        dashboardPageSteps.createExperiment(experimentData);
        experimentList.deleteExperiment(experimentData.getGeneralInfo().getName());
        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentNotDisplayed(experimentData.getGeneralInfo().getName()),
                "Experiment is still shown after removing");
    }

    @Test(dataProvider = "Verify, that alert message appears while attempt to create Experiment with the existing name",
            dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void shouldNotAllowToCreateExperimentsWithIdenticalNames(ExperimentData experimentData, ExperimentData newExperimentData) {
        dashboardPageSteps.createExperiment(experimentData);
        ExperimentGeneralInfoSteps experimentGeneralInfo = dashboardPageSteps.getTopPanelSteps()
                .openCreationMenu()
                .clickCreateExperiment();
        experimentGeneralInfo.fillInForm(newExperimentData.getGeneralInfo());
        assertFalse(experimentGeneralInfo.isNextButtonEnabled(), "User is able to create experiment with the existing name");
        experimentGeneralInfo
                .pressCancel()
                .getExperimentListSteps()
                .deleteExperiment(experimentData.getGeneralInfo().getName());
    }

    @Test(dataProvider = "Create Experiment with translation range", dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createExperimentWithTranslationRangeAndLockMz(ExperimentData experimentData) {
        dashboardPageSteps.createExperiment(experimentData);
        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentData.getGeneralInfo().getName()),
                "Experiment doesn't appear after creating");
        dashboardPageSteps.getSidebarMenuSteps()
                .selectExperimentsItem()
                .deleteExperiment(experimentData.getGeneralInfo().getName());
    }

    //disabled due to the issue CP-236 - Files are not selected on the 2nd step of the Experiment Wizard, while creating experiment from the selected files
    @Test(enabled=false, dataProvider = "Create Experiment with selected files", dataProviderClass = ExperimentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createExperimentWithSelectedFiles(ExperimentData experimentData){
        ExperimentGeneralInfoSteps experimentFromSelectedFiles = dashboardPageSteps.getSidebarMenuSteps().clickGenomeMachineItem().selectFiles(3).pressRunExperimentForSelectedFilesButton();
        assertTrue(experimentFromSelectedFiles.getSpecies().contains(experimentData.getGeneralInfo().getSpecies()),
                "Species, which is displayed in the dialog does not meet expected species");
        assertTrue(experimentFromSelectedFiles.getLaboratory().contains(experimentData.getGeneralInfo().getLaboratory()),
                "Laboratory, which is displayed in the dialog does not meet expected Laboratory");
        assertTrue(experimentFromSelectedFiles.getInstrument().contains(experimentData.getGeneralInfo().getInstrument()),
                "Instrument, which is displayed in the dialog does not meet expected instrument");
        ExperimentFileSelectionSteps experimentFileSelectionSteps = experimentFromSelectedFiles
                .specifyName(experimentData.getGeneralInfo().getName())
                .selectProject(experimentData.getGeneralInfo().getProject())
                .pressNext();
        assertEquals(experimentFileSelectionSteps.countCheckedFiles(), 3,
                "Files,which have been selected in the file list are not checked on the 2nd step of Experiment Wizard");
        experimentFileSelectionSteps
                .pressNext()
                .fillInAnalysisFormAndPressNext(experimentData.getAnalysisInfo())
                .pressNext()
                .pressNext()
                .fillInFactors(experimentData).pressNext()
                .pressConfirmAndCreate();
        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentData.getGeneralInfo().getName()),
                "Experiment doesn't appear after creating");
        //Test ends
        //Clean all data, created during running the test
        dashboardPageSteps.getSidebarMenuSteps()
                .selectExperimentsItem()
                .deleteExperiment(experimentData.getGeneralInfo().getName());
    }

}