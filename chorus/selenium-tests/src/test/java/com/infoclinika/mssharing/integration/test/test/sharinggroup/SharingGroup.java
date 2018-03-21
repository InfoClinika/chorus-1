package com.infoclinika.mssharing.integration.test.test.sharinggroup;

import com.infoclinika.mssharing.integration.test.data.SharingGroupData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.sharinggroup.CreateSharingGroupDialogSteps;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.*;

/**
 * @author Alexander Orlov
 */
public class SharingGroup extends BaseTest {

    @Test(dataProvider = "createRemoveSharingGroup", dataProviderClass = SharingGroupDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createRemoveSharingGroup(SharingGroupData sharingGroupData) {
        String sharingGroupName = sharingGroupData.getPersonToInviteName();
        dashboardPageSteps.getTopPanelSteps().openCreationMenu()
                .clickCreateSharingGroup()
                .specifyName(sharingGroupName)
                .specifyPersonToInvite(sharingGroupData.getPersonToInviteName())
                .pressCreateButtonAndWait();
        assertTrue(dashboardPageSteps.getSharingGroupListSteps().isSharingGroupDisplayed(sharingGroupName),
                "Sharing Group is not appeared in list after creating");
        dashboardPageSteps.getSharingGroupListSteps()
                .deleteSharingGroup(sharingGroupName);
        assertFalse(dashboardPageSteps.getSharingGroupListSteps().isSharingGroupDisplayed(sharingGroupName),
                "Sharing Group is not appeared in list after creating");
    }

    @Test(dataProvider = "shouldNotAllowToCreateSharingGroupWithExistingName", dataProviderClass = SharingGroupDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void shouldNotAllowToCreateSharingGroupWithExistingName(SharingGroupData sharingGroupData, String alert) {
        String sharingGroupName = sharingGroupData.getName();
        CreateSharingGroupDialogSteps createSharingGroupDialogSteps = dashboardPageSteps.getTopPanelSteps().openCreationMenu()
                .clickCreateSharingGroup()
                .specifyName(sharingGroupName)
                .specifyPersonToInvite(sharingGroupData.getPersonToInviteName())
                .pressCreateButtonAndWait()
                .getTopPanelSteps()
                .openCreationMenu()
                .clickCreateSharingGroup()
                .specifyName(sharingGroupName)
                .specifyPersonToInvite(sharingGroupData.getPersonToInviteName())
                .pressCreateButtonForIncorrectForm();
        assertEquals(createSharingGroupDialogSteps.getNameDuplicationAlert(), alert);
        //clearing data
        createSharingGroupDialogSteps
                .pressCancelButton()
                .getSharingGroupListSteps()
                .deleteSharingGroup(sharingGroupName);
    }

    @Test(dataProvider = "shouldNotAllowToCreateSharingGroupWithoutSpecifyingRequiredFields", dataProviderClass = SharingGroupDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void shouldNotAllowToCreateSharingGroupWithoutSpecifyingRequiredFields(SharingGroupData sharingGroupData) {
        CreateSharingGroupDialogSteps createSharingGroupDialogSteps = dashboardPageSteps.getTopPanelSteps()
                .openCreationMenu()
                .clickCreateSharingGroup();
        assertFalse(createSharingGroupDialogSteps.isCreateButtonEnabled(),
                "Create Button is enabled, all required fields were not specified, though.");
        createSharingGroupDialogSteps.specifyName(sharingGroupData.getName());
        assertFalse(createSharingGroupDialogSteps.isCreateButtonEnabled(),
                "Create Button is enabled, person to invite was not specified, though.");
        createSharingGroupDialogSteps
                .specifyName("")
                .specifyPersonToInvite(sharingGroupData.getPersonToInviteName());
        assertFalse(createSharingGroupDialogSteps.isCreateButtonEnabled(),
                "Create Button is enabled, Sharing Group name was not specified, though.");
    }
}
