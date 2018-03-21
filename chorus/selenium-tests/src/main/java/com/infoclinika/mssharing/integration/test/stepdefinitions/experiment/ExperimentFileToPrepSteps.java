package com.infoclinika.mssharing.integration.test.stepdefinitions.experiment;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.WizardTable;
import com.infoclinika.mssharing.integration.test.data.experiment.ExperimentData;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractApplicationPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class ExperimentFileToPrepSteps extends AbstractPageSteps {

    private WizardTable prepCell(int numberOfRow) {
        return new WizardTable(By.xpath("(//table[@id='preps-table']//td[@class='annotation-value'])[" + numberOfRow + "]/div"));
    }

    private static final Button NEXT_BUTTON = controlFactory().button(By.id("next"));


    public ExperimentFileToPrepSteps fillInPreps(ExperimentData experimentData){
        int numberOfFiles = experimentData.getFileSelectionInfo().getNumberOfSelectedFiles();
        for (int i = 1; i <= numberOfFiles; i++) {
            prepCell(i).clear();
            prepCell(i).fillIn(experimentData.getFileToPrepInfo().getPrepsList().get(i));
        }
        return this;
    }

    public ExperimentFileToPrepSteps clearTheFirstPrep(){
        prepCell(1).clear();
        return this;
    }

    public ExperimentSampleToPrepSteps pressNext(){
        NEXT_BUTTON.click();
        return new ExperimentSampleToPrepSteps();
    }

    public ExperimentSampleToPrepSteps fillInPrepsAndPressNext(ExperimentData experimentData){
        fillInPreps(experimentData);
        return pressNext();
    }

    public boolean isNextButtonEnabled() {
        return NEXT_BUTTON.isEnabled();
    }
}
