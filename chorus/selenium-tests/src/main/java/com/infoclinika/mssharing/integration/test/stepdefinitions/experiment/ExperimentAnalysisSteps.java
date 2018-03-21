package com.infoclinika.mssharing.integration.test.stepdefinitions.experiment;

import com.infoclinika.mssharing.integration.test.components.*;
import com.infoclinika.mssharing.integration.test.data.experiment.AnalysisInfo;
import com.infoclinika.mssharing.integration.test.data.experiment.LockMz;
import com.infoclinika.mssharing.integration.test.data.experiment.TranslationRange;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

import java.util.List;

/**
 * @author Alexander Orlov
 */
public class ExperimentAnalysisSteps extends AbstractPageSteps {

    private static Pane lockMassItem(String value) {
        return new Pane(By.xpath("//div[@ng-repeat='val in values'][.//span[text()='" + value + "']]"));
    }

    private static final DropdownList EXPERIMENT_TYPE_DROPDOWN = controlFactory().dropdownList(By.id("s2id_technologyType"));
    private static final DropdownList WORKFLOW_TYPE_DROPDOWN = controlFactory().dropdownList(By.id("s2id_workflowType"));
    private static final DropdownList IS_2DLC_DROPDOWN = controlFactory().dropdownList(By.id("is2dLc"));
    private static final Button NEXT_BUTTON = controlFactory().button(By.id("next"));
    private static final InputBox LOCK_MZ_FIELD = controlFactory().inputBox(By.cssSelector(".lock-masses input[ng-model='value.lockMass']"));
    private static final Button LOCK_MZ_ADD_BUTTON = controlFactory().button(By.cssSelector(".lock-masses button"));
    private static final DropdownList CHARGE_DROPDOWN = controlFactory().dropdownList(By.id("s2id_autogen2"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("[ng-bind='closeWizardActionCaption']"));

    public ExperimentAnalysisSteps selectExperimentType(String experimentType) {
        EXPERIMENT_TYPE_DROPDOWN.select(experimentType);
        return this;
    }

    public ExperimentAnalysisSteps selectWorkflowType(String workflowType) {
        WORKFLOW_TYPE_DROPDOWN.select(workflowType);
        return this;
    }

    public ExperimentAnalysisSteps fillInAnalysisForm(AnalysisInfo analysisInfo) {
        selectExperimentType(analysisInfo.getExperimentType());
        if (analysisInfo.is2Dlc()) {
            IS_2DLC_DROPDOWN.select("Yes");
        }
        return this;
    }

    public ExperimentAnalysisSteps addLockMzValues(TranslationRange translationRange) {
        List<LockMz> lockMzList = translationRange.getLockMzList();
        if (!lockMzList.isEmpty()) {
            for (LockMz lockMz : lockMzList) {
                LOCK_MZ_FIELD.fillIn(lockMz.getLockMass());
                CHARGE_DROPDOWN.select(lockMz.getCharge());
                LOCK_MZ_ADD_BUTTON.click();
            }
        }
        return this;
    }

    public ExperimentFileToPrepSteps pressNext() {
        NEXT_BUTTON.click();
        return new ExperimentFileToPrepSteps();
    }

    public ExperimentFileToPrepSteps fillInAnalysisFormAndPressNext(AnalysisInfo analysisInfo) {
        return fillInAnalysisForm(analysisInfo).pressNext();
    }

    public boolean isNextButtonEnabled() {
        return NEXT_BUTTON.isEnabled();
    }

    public DashboardPageSteps pressCancel() {
        CANCEL_BUTTON.click();
        return new DashboardPageSteps();
    }
}
