package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.data.LaboratoryData;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.structure.InboxList;

/**
 * @author Alexander Orlov
 */
public class InboxListSteps {

    private InboxList inboxList = new InboxList();

    public boolean isLabMembershipRequestItemPresent(UserData userData, String laboratoryName){
        return inboxList.labMembershipRequestItem(userData, laboratoryName).isPresent();
    }

    public InboxListSteps pressApproveLabMembershipButton(UserData userData, String laboratoryName){
        inboxList.approveLabMembershipButton(userData, laboratoryName).scrollAndClick();
        return this;
    }

    public boolean isLabRequestItemPresent(UserData userData, LaboratoryData laboratoryData){
        return inboxList.labCreationRequestItem(userData, laboratoryData).isPresent();
    }

    public InboxListSteps approveLabCreationRequest(UserData userData, LaboratoryData laboratoryData){
        inboxList.approveLabRequestButton(userData, laboratoryData.getLaboratoryName()).click();
        inboxList.labCreationRequestItem(userData, laboratoryData).waitForElementToDisappear();
        return this;
    }

    public InboxListSteps refuseLabCreationRequest(UserData userData, LaboratoryData laboratoryData){
        inboxList.refuseLabRequestButton(userData, laboratoryData).click();
        inboxList.refuseConfirmationButton().waitForElementToBeClickable();
        inboxList.refuseConfirmationButton().click();
        return this;
    }

    public boolean isItemPresentInInboxList(String user, String details){
        return inboxList.itemInInboxList(user, details).isPresent();
    }

    public InboxListSteps pressApproveButtonForItem(String user, String details){
        inboxList.approveButtonForItem(user, details).scrollAndClick();
        return this;
    }

    public InboxListSteps approveProjectCopying(){
        inboxList.projectCopyingApprovalConfirmationButton().waitForElementToBeClickable();
        inboxList.projectCopyingApprovalConfirmationButton().click();
        inboxList.projectCopyingApprovalConfirmationButton().waitForElementToBeInvisible();
        return this;
    }

}
