package com.infoclinika.mssharing.integration.test.stepdefinitions;


import com.infoclinika.mssharing.integration.test.data.experiment.ExperimentData;
import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.stepdefinitions.experiment.ExperimentListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.filelist.FileListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.instrument.InstrumentListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.project.ProjectsListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.sharinggroup.SharingGroupsListSteps;
import com.infoclinika.mssharing.integration.test.structure.DashboardPage;

/**
 * @author Sergii Moroz
 */
public class DashboardPageSteps {
    private DashboardPage dashboardPage = new DashboardPage();
    private TopPanelSteps topPanelSteps;
    private SidebarMenuSteps sidebarMenuSteps;

    public DashboardPageSteps() {
//        waitForPageToLoad();
        this.topPanelSteps = new TopPanelSteps();
        this.sidebarMenuSteps = new SidebarMenuSteps();
    }

    public DashboardPageSteps waitForPageToLoad() {
        dashboardPage.sortingByName().waitForElementToBeClickable();
        return this;
    }

    public DashboardPageSteps searchFor(String term) {
        dashboardPage.searchBar().fillIn(term);
        dashboardPage.searchIcon().click();
        return this;
    }

    public TopPanelSteps getTopPanelSteps() {
        return topPanelSteps;
    }

    public ProjectsListSteps getProjectsListSteps() {
        return new ProjectsListSteps();
    }

    public InstrumentListSteps getInstrumentListSteps() {
        return new InstrumentListSteps();
    }

    public ExperimentListSteps getExperimentListSteps() {
        return new ExperimentListSteps();
    }

    public SidebarMenuSteps getSidebarMenuSteps() {
        return sidebarMenuSteps;
    }

    public SharingGroupsListSteps getSharingGroupListSteps() {
        return new SharingGroupsListSteps();
    }

    public FileListSteps getFileListSteps(){
        return new FileListSteps();
    }

    public HeaderMenuSteps getHeader(){
        return new HeaderMenuSteps();
    }

    public boolean isBillingNotificationDialogDisplayed(){
        return dashboardPage.billingNotificationDialog().isVisible();
    }

    public DashboardPageSteps pressOkInBillingNotificationDialog(){
        if (isBillingNotificationDialogDisplayed()) {
            dashboardPage.dontShowMeAgain().click();
            dashboardPage.ok().click();
        }
        return this;
    }

    //Complex methods
    public DashboardPageSteps createInstrument(InstrumentData instrumentData) {
        return getTopPanelSteps().openCreationMenu()
                .clickCreateInstrument()
                .fillFormAllFields(instrumentData)
                .pressCreateAndWait();
    }

    public DashboardPageSteps createExperiment(ExperimentData experimentData) {
        return getTopPanelSteps().openCreationMenu()
                .clickCreateExperiment()
                .fillInFormAndPressNext(experimentData.getGeneralInfo())
                .selectSeveralFilesAndPressNext(experimentData.getFileSelectionInfo())
                .fillInAnalysisFormAndPressNext(experimentData.getAnalysisInfo())
                .pressNext()
                .pressNext()
                .fillInFactors(experimentData).pressNext()
                .pressConfirmAndCreate();
    }

    public DashboardPageSteps createPrivateProject(ProjectData projectData) {
        return getTopPanelSteps().openCreationMenu().clickCreateProject()
                .specifyName(projectData.getName())
                .specifyLab(projectData.getLaboratory())
                .specifyArea(projectData.getArea())
                .specifyDescription(projectData.getDescription())
                .enableBlog(projectData.getHasBlog())
                .pressCreateButtonAndWait();
    }

    public DashboardPageSteps createSharedProject(ProjectData projectData) {
        return getTopPanelSteps().openCreationMenu().clickCreateProject()
                .specifyName(projectData.getName())
                .specifyLab(projectData.getLaboratory())
                .specifyArea(projectData.getArea())
                .specifyDescription(projectData.getDescription())
                .enableBlog(projectData.getHasBlog())
                .selectSharingTab()
                .specifyPersonToInvite(projectData.getPersonToInvite())
                .pressCreateButtonAndWait();
    }

    public DashboardPageSteps uploadFiles(FileData fileData, String instrumentName, int waitForUploadingTime){
        return getTopPanelSteps().pressUploadFilesButton()
                .selectInstrument(instrumentName)
                .pressNextButton()
                .pressBrowseButton()
                .selectAndAddFiles(fileData)
                .pressNextButton()
                .pressUploadButton()
                .waitForFinishUploading(fileData, waitForUploadingTime)
                .pressOkButton();
    }

    public DashboardPageSteps uploadFilesAndEnableAutoTranslateOption(FileData fileData, String instrumentName){
        return getTopPanelSteps().pressUploadFilesButton()
                .selectInstrument(instrumentName)
                .selectAutoTranslateCheckbox()
                .pressNextButton()
                .pressBrowseButton()
                .selectAndAddFiles(fileData)
                .pressNextButton()
                .pressUploadButton()
                .waitForFinishUploading(fileData, 300)
                .pressOkButton();
    }

    public DashboardPageSteps createSharingGroup(String sharingGroupName, String personToInvite){
        getTopPanelSteps().openCreationMenu()
                .clickCreateSharingGroup()
                .specifyName(sharingGroupName)
                .specifyPersonToInvite(personToInvite)
                .pressCreateButtonAndWait();
        return this;
    }

    public ProjectsListSteps openMyProjectsList() {
        getSidebarMenuSteps().clickMyProjectsItem();
        return new ProjectsListSteps();
    }

    public ExperimentListSteps openMyExperimentsList() {
        getSidebarMenuSteps().clickMyExperimentsItem();
        return new ExperimentListSteps();
    }
}
