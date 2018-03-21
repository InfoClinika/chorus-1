package com.infoclinika.mssharing.integration.test.stepdefinitions.experiment;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Checkbox;
import com.infoclinika.mssharing.integration.test.components.DropdownList;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.experiment.GeneralInfo;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class ExperimentGeneralInfoSteps extends AbstractPageSteps {

    private static final InputBox NAME_FIELD = controlFactory().inputBox(By.id("name"));
    private static final DropdownList PROJECT_DROPDOWN = controlFactory().dropdownList(By.id("s2id_project"));
    private static final DropdownList SPECIES_DROPDOWN = controlFactory().dropdownList(By.id("s2id_species"));
    private static final DropdownList LABORATORY_DROPDOWN = controlFactory().dropdownList(By.id("s2id_labs"));
    private static final DropdownList INSTRUMENT_MODEL_DROPDOWN = controlFactory().dropdownList(By.id("s2id_instrumentModel"));
    private static final DropdownList INSTRUMENT_DROPDOWN = controlFactory().dropdownList(By.id("s2id_instrument"));
    private static final InputBox DESCRIPTION_FIELD = controlFactory().inputBox(By.id("description"));
    private static final Button NEXT_BUTTON = controlFactory().button(By.id("next"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("[ng-bind='closeWizardActionCaption']"));
    private static final Checkbox ALL_LABS_CHECKBOX = controlFactory().checkbox(By.cssSelector("[ng-model='confirmSelectAllLabs']"));

    public ExperimentGeneralInfoSteps() {
        NAME_FIELD.waitForElementToBeClickable();
    }

    public ExperimentGeneralInfoSteps fillInForm(GeneralInfo generalInfo) {
        specifyName(generalInfo.getName());
        selectProject(generalInfo.getProject());
        selectSpecies(generalInfo.getSpecies());
        selectLaboratory(generalInfo.getLaboratory());
        selectInstrumentModel(generalInfo.getInstrumentModel());
        selectInstrument(generalInfo.getInstrument());
        specifyDescription(generalInfo.getDescription());
        return this;
    }

    public ExperimentFileSelectionSteps fillInFormAndPressNext(GeneralInfo generalInfo) {
        fillInForm(generalInfo);
        pressNext();
        return new ExperimentFileSelectionSteps();
    }

    public ExperimentGeneralInfoSteps specifyName(String name) {
        NAME_FIELD.fillIn(name);
        return this;
    }

    public ExperimentGeneralInfoSteps selectProject(String project) {
        PROJECT_DROPDOWN.select(project);
        return this;
    }

    public ExperimentGeneralInfoSteps selectSpecies(String species) {
        SPECIES_DROPDOWN.select(species);
        return this;
    }

    public ExperimentGeneralInfoSteps selectLaboratory(String lab) {
        if (lab.equals(SampleData.LAB_NO_LAB)) {
            ALL_LABS_CHECKBOX.click();
        } else {
            LABORATORY_DROPDOWN.select(lab);
        }
        return this;
    }

    public ExperimentGeneralInfoSteps selectInstrument(String instrument) {
        INSTRUMENT_DROPDOWN.select(instrument);
        return this;
    }

    public ExperimentGeneralInfoSteps selectInstrumentModel(String instrumentModel) {
        INSTRUMENT_MODEL_DROPDOWN.select(instrumentModel);
        return this;
    }

    public ExperimentGeneralInfoSteps specifyDescription(String description) {
        DESCRIPTION_FIELD.fillIn(description);
        return this;
    }

    public DashboardPageSteps pressCancel() {
        CANCEL_BUTTON.click();
        return new DashboardPageSteps();
    }

    public ExperimentFileSelectionSteps pressNext() {
        NEXT_BUTTON.waitForButtonToBeEnabled();
        NEXT_BUTTON.click();
        return new ExperimentFileSelectionSteps();
    }

    public ExperimentGeneralInfoSteps clearName() {
        NAME_FIELD.clear();
        return this;
    }

    public boolean isNextButtonEnabled() {
        return NEXT_BUTTON.isEnabled();
    }

    public String getSpecies() {
        return SPECIES_DROPDOWN.getText();
    }

    public String getLaboratory() {
        return LABORATORY_DROPDOWN.getText();
    }

    public String getInstrument() {
        return INSTRUMENT_DROPDOWN.getText();
    }

}
