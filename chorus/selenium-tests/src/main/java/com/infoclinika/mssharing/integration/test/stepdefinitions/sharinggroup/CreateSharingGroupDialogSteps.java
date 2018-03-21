package com.infoclinika.mssharing.integration.test.stepdefinitions.sharinggroup;

import com.infoclinika.mssharing.integration.test.components.*;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class CreateSharingGroupDialogSteps extends AbstractPageSteps {

    //Dynamic locators
    private Pane itemInAutocompleteList(String item) {
        return new Pane(By.xpath("//*[contains(@id, 'ui-id') and contains(text(), '" + item + "')]"));
    }

    private Pane personInSharingList(String person) {
        return new Pane(By.xpath(".//tr[td/span[text()=\"" + person + "\"]]"));
    }

    private static final InputBox NAME_FIELD = controlFactory().inputBox(By.id("createGroupName"));
    private static final Button CREATE_BUTTON = controlFactory().button(By.cssSelector("form[name='createGroupForm'] .main-action"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("form[name='createGroupForm'] .secondary-action"));
    private static final Label NAME_DUPLICATION_ALERT = controlFactory().label(By.cssSelector("[for='createGroupName']>span[ng-show='isNameDuplicated()']"));
    private static final AutoCompleteList AUTOCOMPLETE_LIST = controlFactory().autoCompleteList(By.cssSelector(".ui-autocomplete.ui-menu"));
    private static final InputBox INVITE_PEOPLE_FIELD = controlFactory().inputBox(By.cssSelector(".dialog-text-input.add-users"));
    private static final Pane MODAL_HOLDER = controlFactory().pane(By.cssSelector(".modal-holder"));

    public CreateSharingGroupDialogSteps specifyName(String name) {
        NAME_FIELD.clearAndFill(name);
        return this;
    }

    public CreateSharingGroupDialogSteps specifyPersonToInvite(String person) {
        INVITE_PEOPLE_FIELD.fillIn(person);
        AUTOCOMPLETE_LIST.waitForElementToBeVisible();
        itemInAutocompleteList(person).click();
        return this;
    }

    public DashboardPageSteps pressCreateButtonAndWait() {
        CREATE_BUTTON.click();
        MODAL_HOLDER.waitForElementToBeInvisible();
        return new DashboardPageSteps();
    }

    public CreateSharingGroupDialogSteps pressCreateButtonForIncorrectForm() {
        CREATE_BUTTON.click();
        return this;
    }

    public boolean isCreateButtonEnabled() {
        CREATE_BUTTON.waitForElementToBeVisible();
        return CREATE_BUTTON.isEnabled();
    }

    public DashboardPageSteps pressCancelButton() {
        CANCEL_BUTTON.click();
        return new DashboardPageSteps();
    }

    /* Alerts */

    public String getNameDuplicationAlert() {
        return "Name " + NAME_DUPLICATION_ALERT.getText();
    }
}
