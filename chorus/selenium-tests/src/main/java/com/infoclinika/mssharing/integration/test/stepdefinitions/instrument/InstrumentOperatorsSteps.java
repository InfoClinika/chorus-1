package com.infoclinika.mssharing.integration.test.stepdefinitions.instrument;

import com.infoclinika.mssharing.integration.test.components.*;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class InstrumentOperatorsSteps extends AbstractPageSteps {

    private Label itemInAutocompleteList(String item) {
        return new Label(By.partialLinkText(item));
    }

    private Pane personInList(String name) {
        return new Pane(By.xpath("//tr[.//span[text()='" + name + "']]"));
    }

    private static final InputBox INVITE_PEOPLE_FIELD = controlFactory().inputBox(By.cssSelector(".dialog-text-input.add-users"));
    private static final AutoCompleteList MODAL_DIALOG_AUTOCOMPLETE_LIST = controlFactory().autoCompleteList(By.cssSelector(".ui-autocomplete.ui-menu"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector(".ng-dirty>.modal-footer>.secondary-action"));
    private static final Button CREATE_BUTTON = controlFactory().button(By.cssSelector(".ng-dirty>.modal-footer>.main-action"));
    private static final Pane MODAL_DIALOG = controlFactory().pane(By.cssSelector(".create-instrument > .modal-holder"));

    public InstrumentOperatorsSteps() {
        INVITE_PEOPLE_FIELD.waitForElementToBeClickable();
    }

    public InstrumentOperatorsSteps specifyOperatorToInvite(String name) {
        INVITE_PEOPLE_FIELD.fillIn(name);
        MODAL_DIALOG_AUTOCOMPLETE_LIST.waitForAppearing();
        itemInAutocompleteList(name).click();
        return this;
    }

    public boolean isOperatorAdded(String person) {
        return personInList(person).isPresent();
    }

    public InstrumentListSteps pressCreateButton() {
        CREATE_BUTTON.click();
        MODAL_DIALOG.waitForElementToBeInvisible();
        return new InstrumentListSteps();
    }

}
