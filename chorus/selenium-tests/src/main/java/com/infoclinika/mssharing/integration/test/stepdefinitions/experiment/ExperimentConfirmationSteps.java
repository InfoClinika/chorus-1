package com.infoclinika.mssharing.integration.test.stepdefinitions.experiment;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class ExperimentConfirmationSteps extends AbstractPageSteps {

    //Dynamic locator
    private Label factorConfirmation(int row) {
        return new Label(By.xpath("//div[@class='limit-table']//tbody//tr[" + row + "]//td[2]"));
    }

    private static final Label NAME_LABEL = controlFactory().label(By.cssSelector("[ng-bind='experiment.info.name']"));
    private static final Label PROJECT_LABEL = controlFactory().label(By.cssSelector("[ng-bind='findById(projects, experiment.project).name']"));
    private static final Label LAB_LABEL = controlFactory().label(By.cssSelector("[ng-bind='getLabName(experiment.lab)']"));
    private static final Label SPECIES_LABEL = controlFactory().label(By.cssSelector("[ng-bind='findById(species, experiment.info.specie).name']"));
    private static final Label NUMBER_OF_FILES_LABEL = controlFactory().label(By.cssSelector("[ng-bind='selectedFilesLength']"));
    private static final Button CONFIRM_AND_CREATE_BUTTON = controlFactory().button(By.cssSelector("[ng-bind='\\'Confirm and \\' + actionCaption']"));
    private static final Button BACK_BUTTON = controlFactory().button(By.id("back"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("[ng-bind='closeWizardActionCaption']"));
    private static final Pane MODAL_DIALOG = controlFactory().pane(By.cssSelector("#experiment-modal-dialog > .modal-holder"));
    private static final Pane EXPERIMENT_NOT_TRANSLATED_DIALOG = controlFactory().pane(By.id("translate-experiment-confirm"));
    private static final Label CONFIRM_FILE_TABLE = controlFactory().label(By.xpath("//tr[contains(@ng-repeat, 'condition in conditions')]"));
    private static final Button POSTPONE_TRANSLATION_BUTTON = controlFactory().button(By.xpath("//div[@id='translate-experiment-confirm']//button[contains(text(),'Postpone')]"));

    public String getExperimentName() {
        return NAME_LABEL.getText();
    }

    public String getExperimentProject() {
        return PROJECT_LABEL.getText();
    }

    public String getExperimentLab() {
        return LAB_LABEL.getText();
    }

    public String getExperimentSpecie() {
        return SPECIES_LABEL.getText();
    }

    public int getExperimentNumberOfFiles() {
        return Integer.parseInt(NUMBER_OF_FILES_LABEL.getText());
    }

    public DashboardPageSteps pressConfirmAndCreate() {
        CONFIRM_AND_CREATE_BUTTON.click();
        MODAL_DIALOG.waitForElementToBeInvisible();
        return new DashboardPageSteps();
    }

    public DashboardPageSteps pressCreateAndPostponeForTranslation() {
        CONFIRM_AND_CREATE_BUTTON.click();
        POSTPONE_TRANSLATION_BUTTON.click();
        EXPERIMENT_NOT_TRANSLATED_DIALOG.waitForElementToBeInvisible();
        return new DashboardPageSteps();
    }

    public ExperimentDesignSteps pressBack() {
        BACK_BUTTON.click();
        return new ExperimentDesignSteps();
    }

    public DashboardPageSteps pressCancel() {
        CANCEL_BUTTON.click();
        return new DashboardPageSteps();
    }

    public int getNumberOfFilesInConfirmationList() {
        return CONFIRM_FILE_TABLE.getAllElements().size();
    }

    public String getConditionValue(int row) {
        return factorConfirmation(row).getText();
    }

    public boolean isCreateButtonEnabled() {
        return CONFIRM_AND_CREATE_BUTTON.isEnabled();
    }

}
