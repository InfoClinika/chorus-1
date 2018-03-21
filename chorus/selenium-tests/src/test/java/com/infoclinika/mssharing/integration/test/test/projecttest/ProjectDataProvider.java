package com.infoclinika.mssharing.integration.test.test.projecttest;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.data.projectdata.PersonToInvite;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class ProjectDataProvider extends AbstractDataProvider {

    @DataProvider(name = "Verification, that project cannot be created without specifying all required fields")
    public static Object[][] shouldNotAllowToCreateProjectWithoutSpecifyingRequiredFields() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area")).build();
        return new Object[][]{{projectData}};
    }

    @DataProvider(name = "Create Private Project")
    public static Object[][] createPrivateProject() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .description("Description").build();
        return new Object[][]{{projectData}};
    }

    @DataProvider(name = "Create Shared Project")
    public static Object[][] createSharedProject() {
        List<PersonToInvite> personToInviteList = new ArrayList<>();
        personToInviteList.add(new PersonToInvite.Builder()
                .name(environmentSpecificData.karrenKoe.getFullName())
                .email(environmentSpecificData.karrenKoe.getEmail()).build());
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .description("Description")
                .personToInvite(personToInviteList).build();
        return new Object[][]{{projectData}};
    }

    @DataProvider(name = "Create Public Project")
         public static Object[][] createPublicProject() {
        List<PersonToInvite> personToInviteList = new ArrayList<>();
        personToInviteList.add(new PersonToInvite.Builder()
                .name(SampleData.ALL).build());
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .description("Description")
                .personToInvite(personToInviteList).build();
        return new Object[][]{{projectData}};
    }

    @DataProvider(name = "Edit Project")
    public static Object[][] editProject() {
        List<PersonToInvite> personToInviteList = new ArrayList<>();
        personToInviteList.add(new PersonToInvite.Builder()
                .name(environmentSpecificData.karrenKoe.getFullName())
                .email(environmentSpecificData.karrenKoe.getEmail()).build());
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .description("Description")
                .personToInvite(personToInviteList).build();
        ProjectData editedProjectData = new ProjectData.Builder()
                .name(randomizeName("Project - Edited"))
                .laboratory(projectData.getLaboratory())
                .area(randomizeName("Area - Edited"))
                .description("Description - Edited")
                .personToInvite(personToInviteList).build();
        return new Object[][]{{projectData, editedProjectData}};
    }

    @DataProvider(name = "Pass a copy of a project")
    public static Object[][] passACopy() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area")).build();
        UserData userData = new UserData.Builder()
                .email("karren.koe@gmail.com")
                .password("pwd").build();
        return new Object[][]{{projectData, userData}};
    }

}
