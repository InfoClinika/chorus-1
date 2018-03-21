package com.infoclinika.mssharing.integration.test.structure;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.data.LaboratoryData;
import com.infoclinika.mssharing.integration.test.data.UserData;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 *
 * Summary: This class describes elements which are located in Inbox List on Dashboard Page. This list could be opened by clicking
 * "Inbox" link in the Sidebar Menu (menu in the left side of the page).
 * Note: All actions which can be performed with the elements of the Inbox List are located in the appropriate
 * InboxListSteps class.
 */
public class InboxList {

    //Dynamic locators
    private static By itemInList(String user, String details) {
        return By.xpath("//div[@class='row clearfix ng-scope'][.//div[@title='" + user + "'] and .//div[contains(@title, '" + details + "')]]");
    }

    private static By approveButton(String user, String details) {
        return By.xpath("//div[@class='row clearfix ng-scope'][.//div[@title='" + user + "'] and .//div[contains(@title, '" + details + "')]]//button[contains(text(),'Approve')]");
    }

    private static By refuseButton(String user, String details) {
        return By.xpath("//div[@class='row clearfix ng-scope'][.//div[@title='" + user + "'] and .//div[contains(@title, '" + details + "')]]//button[contains(text(),'Refuse')]");
    }

    private static By infoButton(String user, String details) {
        return By.xpath("//div[@class='row clearfix ng-scope'][.//div[@title='" + user + "'] and .//div[contains(@title, '" + details + "')]]//button[@title='Show Request Details']");
    }

    private static final By REFUSE_CONFIRMATION_BUTTON = By.id("confirmRefuseButton");
    private static final By CONFIRM_PROJECT_COPYING_APPROVAL = By.xpath("//*[@id='copy-project-confirm']//button[@type='submit']");

    public Pane labCreationRequestItem(UserData userData, LaboratoryData laboratoryData) {
        return new Pane(itemInList(userData.getEmail(), laboratoryData.getLaboratoryName()));
    }

    public Button approveLabRequestButton(UserData userData, String laboratoryName) {
        return new Button(approveButton(userData.getEmail(), laboratoryName));
    }

    public Button refuseLabRequestButton(UserData userData, LaboratoryData laboratoryData) {
        return new Button(refuseButton(userData.getEmail(), laboratoryData.getLaboratoryName()));
    }

    public Button infoLabRequestButton(UserData userData, LaboratoryData laboratoryData) {
        return new Button(refuseButton(userData.getEmail(), laboratoryData.getLaboratoryName()));
    }

    public Pane labMembershipRequestItem(UserData userData, String laboratoryName) {
        return new Pane(itemInList(userData.getFullName(), laboratoryName));
    }

    public Button approveLabMembershipButton(UserData userData, String laboratoryName) {
        return new Button(approveButton(userData.getFullName(), laboratoryName));
    }

    public Pane itemInInboxList(String user, String details){
        return new Pane(itemInList(user, details));
    }

    public Button refuseConfirmationButton() {
        return new Button(REFUSE_CONFIRMATION_BUTTON);
    }

    public Button approveButtonForItem(String user, String details){
        return new Button(approveButton(user, details));
    }

    public Button refuseButtonForItem(String user, String details){
        return new Button(refuseButton(user, details));
    }

    public Button projectCopyingApprovalConfirmationButton(){
        return new Button(CONFIRM_PROJECT_COPYING_APPROVAL);
    }

}
