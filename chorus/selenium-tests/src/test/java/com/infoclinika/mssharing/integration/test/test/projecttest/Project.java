package com.infoclinika.mssharing.integration.test.test.projecttest;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.HomePageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.InboxListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.project.ProjectGeneralTabSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.project.ProjectsListSteps;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * @author Alexander Orlov
 */
public class Project extends BaseTest {

    @Test(dataProvider = "Verification, that project cannot be created without specifying all required fields",
            dataProviderClass = ProjectDataProvider.class, groups = {"staging", "production"})
    @LoginRequired
    public void shouldNotAllowToCreateProjectWithoutSpecifyingRequiredFields(ProjectData projectData) {
        //leave all fields empty
        ProjectGeneralTabSteps projectGeneralTabSteps = dashboardPageSteps.getTopPanelSteps().openCreationMenu()
                .clickCreateProject();
        assertFalse(projectGeneralTabSteps.isCreateButtonEnabled(),
                "Create Button is enabled though all required fields were not specified.");
        //fill in only Project Name and leave all other required fields empty
        projectGeneralTabSteps.specifyName(projectData.getName());
        assertFalse(projectGeneralTabSteps.isCreateButtonEnabled(),
                "Create Button is enabled though all required fields, except name were not specified.");
        //clear Project Name, fill in Area, so all required fields except area are blank
        projectGeneralTabSteps
                .specifyName("")
                .specifyArea(projectData.getArea());
        assertFalse(projectGeneralTabSteps.isCreateButtonEnabled(),
                "Create Button is enabled though all required fields, except area were not specified.");
        //clear Area, specify Lab only
        projectGeneralTabSteps
                .specifyArea("")
                .specifyLab(projectData.getLaboratory());
        assertFalse(projectGeneralTabSteps.isCreateButtonEnabled(),
                "Create Button is enabled though all required fields, except lab were not specified.");
    }

    @Test(dataProvider = "Create Private Project", dataProviderClass = ProjectDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createPrivateProject(ProjectData projectData) {
        dashboardPageSteps.createPrivateProject(projectData);
        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(projectData.getName()),
                "Project is not appeared after creating");
        //clearing data
        dashboardPageSteps.getProjectsListSteps()
                .deletePrivateProject(projectData.getName());
    }

    @Test(dataProvider = "Create Shared Project", dataProviderClass = ProjectDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createSharedProject(ProjectData projectData) {
        dashboardPageSteps.createSharedProject(projectData);
        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(projectData.getName()),
                "Project is not appeared after creating");
        //clearing data
        dashboardPageSteps.getProjectsListSteps()
                .deleteSharedProjectWithSharingGroups(projectData.getName(), projectData.getPersonToInvite());
    }

    @Test(dataProvider = "Create Public Project", dataProviderClass = ProjectDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createPublicProject(ProjectData projectData) {
        dashboardPageSteps.createSharedProject(projectData);
        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(projectData.getName()),
                "Project is not appeared after creating");
        //clearing data
        dashboardPageSteps.getProjectsListSteps()
                .deleteSharedProjectWithSharingGroups(projectData.getName(), projectData.getPersonToInvite());
    }

    @Test(dataProvider = "Edit Project", dataProviderClass = ProjectDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void editProject(ProjectData projectData, ProjectData editedProjectData) {
        dashboardPageSteps.createSharedProject(projectData)
                .getProjectsListSteps()
                .openProjectForEditing(projectData.getName())
                .editName(editedProjectData.getName())
                .editArea(editedProjectData.getArea())
                .editDescription(editedProjectData.getDescription())
                .pressSaveButtonAndWait();
        assertTrue(dashboardPageSteps.getProjectsListSteps().isProjectDisplayed(editedProjectData.getName()),
                "Project with changed name is not shown in Projects list after editing");
        ProjectData actualProjectData = dashboardPageSteps
                .getProjectsListSteps()
                .openProjectForEditing(editedProjectData.getName())
                .readProjectData();
        assertTrue(editedProjectData.equals(actualProjectData), "Data from UI does not correspond to the expected.\nExpected: "
                + editedProjectData + "\nActual: " + actualProjectData);
        //clearing data
        dashboardPageSteps.getProjectsListSteps()
                .deleteSharedProjectWithSharingGroups(editedProjectData.getName(), editedProjectData.getPersonToInvite());
    }

    @Test(dataProvider = "Pass a copy of a project", dataProviderClass = ProjectDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void passACopy(ProjectData projectData, UserData userData){
        dashboardPageSteps.createPrivateProject(projectData)
                .getProjectsListSteps()
                .passACopy(projectData.getName(), userData);
        HomePageSteps homePageSteps = dashboardPageSteps.getHeader().logout();
        InboxListSteps inboxListSteps = homePageSteps.pressSignInButton().login(userData).pressOkInBillingNotificationDialog()
                .getSidebarMenuSteps()
                .selectInbox();
        assertTrue(inboxListSteps.isItemPresentInInboxList("Pavel Kaplin", projectData.getName()));
        inboxListSteps.pressApproveButtonForItem("Pavel Kaplin", projectData.getName()).approveProjectCopying();
        ProjectsListSteps projectsListSteps = dashboardPageSteps.getSidebarMenuSteps().clickMyProjectsItem();
        assertTrue(projectsListSteps.isProjectDisplayed(projectData.getName()));
        //clearing data
        dashboardPageSteps.getProjectsListSteps().deletePrivateProject(projectData.getName());
    }


}
