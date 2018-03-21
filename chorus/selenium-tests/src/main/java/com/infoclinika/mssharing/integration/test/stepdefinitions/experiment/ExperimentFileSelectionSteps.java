package com.infoclinika.mssharing.integration.test.stepdefinitions.experiment;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Checkbox;
import com.infoclinika.mssharing.integration.test.data.experiment.FileSelectionInfo;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class ExperimentFileSelectionSteps extends AbstractPageSteps {

    //Dynamic locator
    private static Checkbox fileCheckbox(int number) {
        return new Checkbox(By.xpath("//tbody/tr[" + number + "]"));
    }

    private static final Checkbox SELECT_ALL_FILES_CHECKBOX = controlFactory().checkbox(By.cssSelector("[ng-model='allFilesSelected']"));
    private static final Checkbox FILE_CHECKBOX = controlFactory().checkbox(By.cssSelector("tbody > tr"));
    private static final Checkbox FILE_CHECKED_CHECKBOX = controlFactory().checkbox(By.xpath("//tbody//input[@type='checkbox' and @checked='checked']"));
    private static final Button BACK_BUTTON = controlFactory().button(By.id("back"));
    private static final Button NEXT_BUTTON = controlFactory().button(By.id("next"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("[ng-bind='closeWizardActionCaption']"));

    public ExperimentAnalysisSteps selectAllFilesAndPressNext() {
        selectAllFiles();
        pressNext();
        return new ExperimentAnalysisSteps();
    }

    public ExperimentAnalysisSteps selectSeveralFilesAndPressNext(FileSelectionInfo fileSelectionInfo) {
        selectFiles(fileSelectionInfo.getNumberOfSelectedFiles());
        pressNext();
        return new ExperimentAnalysisSteps();
    }

    public ExperimentFileSelectionSteps selectAllFiles() {
        SELECT_ALL_FILES_CHECKBOX.click();
        return this;
    }

    public DashboardPageSteps pressCancel() {
        CANCEL_BUTTON.click();
        return new DashboardPageSteps();
    }

    public ExperimentGeneralInfoSteps pressBack() {
        BACK_BUTTON.click();
        return new ExperimentGeneralInfoSteps();
    }

    public ExperimentAnalysisSteps pressNext() {
        NEXT_BUTTON.waitForButtonToBeEnabled();
        NEXT_BUTTON.click();
        return new ExperimentAnalysisSteps();
    }

    public int countFilesNumber() {
        return FILE_CHECKBOX.getNumberOfAll();
    }

    public int countCheckedFiles() {
        return FILE_CHECKED_CHECKBOX.getNumberOfAll();
    }

    public ExperimentFileSelectionSteps selectFiles(int numberOfFiles) {
        for (int i = 1; i <= numberOfFiles; i++) {
            fileCheckbox(i).click();
        }
        return this;
    }

    public boolean isNextButtonEnabled() {
        return NEXT_BUTTON.isEnabled();
    }
}
