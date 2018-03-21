package com.infoclinika.mssharing.integration.test.stepdefinitions.filelist;

import com.infoclinika.mssharing.integration.test.components.*;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class BulkEditLabelsDialogSteps extends AbstractPageSteps {

    private static final RadioButton APPEND_EXISTING_LABELS_RADIOBUTTON = controlFactory().radioButton(By.id("labels-append-radio"));
    private static final RadioButton REPLACE_LABELS_RADIOBUTTON = controlFactory().radioButton(By.id("labels-replace-radio"));
    private static final InputBox ENTER_NEW_LABELS_TEXTAREA = controlFactory().inputBox(By.id("edit-labels-area"));
    private static final Button SAVE_BUTTON = controlFactory().button(By.cssSelector("[ng-bind='options.success.caption']"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("[ng-switch='options.type'] > div > .btn.secondary-action"));
    private static final Checkbox EDIT_LABELS_CHECKBOX = controlFactory().checkbox(By.cssSelector("input[ng-model='model.editLabels']"));
    private static final Checkbox EDIT_SPECIES_CHECKBOX = controlFactory().checkbox(By.cssSelector("[ng-model='model.editSpecies']"));
    private static final DropdownList SPECIES_DROPDOWN = controlFactory().dropdownList(By.id("s2id_specie"));

    public BulkEditLabelsDialogSteps selectAppendExistingLabels() {
        APPEND_EXISTING_LABELS_RADIOBUTTON.waitForElementToBeClickable();
        APPEND_EXISTING_LABELS_RADIOBUTTON.click();
        return this;
    }

    public BulkEditLabelsDialogSteps selectReplaceLabels() {
        REPLACE_LABELS_RADIOBUTTON.click();
        return this;
    }

    public BulkEditLabelsDialogSteps specifyNewLabels(String text) {
        ENTER_NEW_LABELS_TEXTAREA.fillIn(text);
        return this;
    }

    public FileListSteps pressSave() {
        SAVE_BUTTON.click();
        wait(1);
        return new FileListSteps();
    }

    public DashboardPageSteps pressCancel() {
        CANCEL_BUTTON.click();
        return new DashboardPageSteps();
    }

    public BulkEditLabelsDialogSteps selectEditLabelsPart() {
        EDIT_LABELS_CHECKBOX.waitForElementToBeClickable();
        EDIT_LABELS_CHECKBOX.click();
        return this;
    }

    public BulkEditLabelsDialogSteps selectEditSpeciesPart() {
        EDIT_SPECIES_CHECKBOX.waitForElementToBeClickable();
        EDIT_SPECIES_CHECKBOX.click();
        return this;
    }

    public BulkEditLabelsDialogSteps selectSpecies(String speciesName) {
        SPECIES_DROPDOWN.select(speciesName);
        return this;
    }
}
