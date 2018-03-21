package com.infoclinika.mssharing.integration.test.stepdefinitions.experiment;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.DropdownList;
import com.infoclinika.mssharing.integration.test.components.WizardTable;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.data.experiment.ExperimentData;
import com.infoclinika.mssharing.integration.test.data.experiment.ExperimentDesignInfo;
import com.infoclinika.mssharing.integration.test.data.experiment.FactorType;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomInt;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeFileName;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Sergii Moroz
 */
public class ExperimentDesignSteps extends AbstractPageSteps {

    //Dynamic locators
    private WizardTable fractionNumberAnnotationCell(int numberOfRow) {
        return new WizardTable(By.xpath("//tr[" + numberOfRow + "]/td[@ng-show=\"experiment.is2dLc\"][1]"));
    }

    private WizardTable sampleIdAnnotationCell(int numberOfRow) {
        return new WizardTable(By.xpath("//tr[\" + numberOfRow + \"]/td[@ng-show=\"experiment.is2dLc\"][2]"));
    }

    private WizardTable factorCell(int numberOfRow, String factorName) {
        return new WizardTable(By.xpath("//tr[" + numberOfRow + "]//div[@title=\"Value of factor '" + factorName + "'\"]"));
    }

    private Button removeFactorButton(String factorName) {
        return new Button(By.xpath(".//*[text()='" + factorName + "']/../*[@ng-click=\"removeFactor(factor)\"]"));
    }

    private static final InputBox FACTOR_NAME_FIELD = controlFactory().inputBox(By.id("factorName"));
    private static final DropdownList FACTOR_TYPE_DROPDOWN = controlFactory().dropdownList(By.id("s2id_factorType"));
    private static final Button ADD_FACTOR_BUTTON = controlFactory().button(By.cssSelector("[ng-click='vm.addFactor()']"));
    private static final InputBox FACTOR_UNITS_FIELD = controlFactory().inputBox(By.id("factorUnits"));
    private static final Button BACK_BUTTON = controlFactory().button(By.id("back"));
    private static final Button NEXT_BUTTON = controlFactory().button(By.id("next"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("[ng-bind='closeWizardActionCaption']"));

    public ExperimentDesignSteps fillInFactors(ExperimentData experimentData) {
        int numberOfFiles = experimentData.getFileSelectionInfo().getNumberOfSelectedFiles();
        if (experimentData.getAnalysisInfo().is2Dlc()) {
            for (int i = 1; i <= numberOfFiles; i++) {
                fractionNumberAnnotationCell(i).fillIn(randomizeName("fraction"));
                sampleIdAnnotationCell(i).fillIn(randomizeName("sampleId"));
            }
        }
        List<ExperimentDesignInfo> experimentDesignInfoList = experimentData.getExperimentDesignInfo();
        if (!experimentDesignInfoList.isEmpty()) {
            for (ExperimentDesignInfo experimentDesignInfo : experimentDesignInfoList) {
                FACTOR_NAME_FIELD.fillIn(experimentDesignInfo.getFactorName());
                switch (experimentDesignInfo.getValueType()) {
                    case TEXT:
                        FACTOR_TYPE_DROPDOWN.select(FactorType.TEXT.getName());
                        ADD_FACTOR_BUTTON.click();
                        for (int i = 1; i <= numberOfFiles; i++) {
                            factorCell(i, experimentDesignInfo.getFactorName()).fillIn(randomizeName("factor"));
                        }
                        break;
                    case NUMBER:
                        FACTOR_TYPE_DROPDOWN.select(FactorType.NUMBER.getName());
                        FACTOR_UNITS_FIELD.fillIn(randomizeFileName("unit"));
                        ADD_FACTOR_BUTTON.click();
                        for (int i = 1; i <= numberOfFiles; i++) {
                            factorCell(i, experimentDesignInfo.getFactorName()).fillIn(randomInt());
                        }
                        break;
                }
            }
        }
        return this;
    }

    public ExperimentDesignSteps removeFactor(String factorName) {
        removeFactorButton(factorName).click();
        return this;
    }

    public DashboardPageSteps pressCancel() {
        CANCEL_BUTTON.click();
        return new DashboardPageSteps();
    }

    public ExperimentAnalysisSteps pressBack() {
        BACK_BUTTON.click();
        return new ExperimentAnalysisSteps();
    }

    public ExperimentConfirmationSteps pressNext() {
        NEXT_BUTTON.click();
        return new ExperimentConfirmationSteps();
    }

    public boolean isNextButtonEnabled() {
        return NEXT_BUTTON.isEnabled();
    }
}
