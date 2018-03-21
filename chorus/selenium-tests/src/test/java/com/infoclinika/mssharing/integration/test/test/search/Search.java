package com.infoclinika.mssharing.integration.test.test.search;

import com.infoclinika.mssharing.integration.test.data.experiment.ExperimentData;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Orlov
 */
public class Search extends BaseTest {

    @Test(dataProvider = "searchForProjectByTitle", dataProviderClass = SearchDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void searchForProjectByTitle(ProjectData projectData) {
        dashboardPageSteps
                .createPrivateProject(projectData)
                .searchFor(projectData.getName());
        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(projectData.getName()),
                "Project is not shown in Search Results list");
        dashboardPageSteps.getProjectsListSteps().deletePrivateProject(projectData.getName());
    }

    @Test(dataProvider = "searchForExperimentByTitle", dataProviderClass = SearchDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void searchForExperimentByTitle(ExperimentData experimentData) {
        String experimentName = experimentData.getGeneralInfo().getName();
        dashboardPageSteps
                .createExperiment(experimentData)
                .searchFor(experimentData.getGeneralInfo().getName());
        assertTrue(dashboardPageSteps.getExperimentListSteps().isExperimentDisplayed(experimentName),
                "Experiment is not shown in Search Results list");
        //clearing data
        dashboardPageSteps.getExperimentListSteps()
                .deleteExperiment(experimentName);
    }

    @Test(dataProvider = "searchForInstrumentByTitle", dataProviderClass = SearchDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void searchForInstrumentByTitle(InstrumentData instrumentData) {
        dashboardPageSteps
                .createInstrument(instrumentData)
                .searchFor(instrumentData.getName());
        assertTrue(dashboardPageSteps.getInstrumentListSteps().isInstrumentDisplayed(instrumentData.getName()),
                "Instrument is not shown in Search Results list");
        //clearing data
        dashboardPageSteps.getInstrumentListSteps()
                .deleteInstrument(instrumentData.getName());
    }

    @Test(dataProvider = "searchForFileByFileName", dataProviderClass = SearchDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void searchForFileByFileName(FileData fileData) {
        dashboardPageSteps
                .searchFor(fileData.getFiles()[0]);
        assertTrue(dashboardPageSteps.getFileListSteps().isFilePresent(fileData.getFiles()[0]),
                "File is not shown in Search Results list");
    }
}
