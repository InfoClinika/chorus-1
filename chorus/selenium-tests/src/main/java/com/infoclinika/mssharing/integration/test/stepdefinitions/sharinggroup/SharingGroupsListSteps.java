package com.infoclinika.mssharing.integration.test.stepdefinitions.sharinggroup;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.ContextMenu;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class SharingGroupsListSteps extends AbstractPageSteps {

    //Dynamic locators
    private Pane sharingGroupInList(String sharingGroupName) {
        return new Pane(By.xpath("//div[@class='row clearfix ng-scope'][.//div[text()='" + sharingGroupName + "']]"));
    }

    private static final Button ITEM_DELETION_BUTTON = controlFactory().button(By.cssSelector("[title='Remove sharing group']"));
    private static final Button REMOVE_CONFIRMATION_BUTTON = controlFactory().button(By.cssSelector("#remove-sharing-group-confirmation>.modal-holder>.modal-frame>.modal-footer>.main-action"));

    private ContextMenu sharingGroupDeleteButton(String sharingGroupName) {
        return new ContextMenu(sharingGroupInList(sharingGroupName), ITEM_DELETION_BUTTON);
    }

    public boolean isSharingGroupDisplayed(String sharingGroupName) {
        return sharingGroupInList(sharingGroupName).isPresent();
    }

    public SharingGroupsListSteps deleteSharingGroup(String sharingGroupName) {
        sharingGroupDeleteButton(sharingGroupName).hoverAndClick();
        REMOVE_CONFIRMATION_BUTTON.waitForElementToBeClickable();
        REMOVE_CONFIRMATION_BUTTON.click();
        sharingGroupInList(sharingGroupName).waitForElementToDisappear();
        return this;
    }

}
