package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class AnalysisManagementDialog extends AbstractPageSteps {

    private static final InputBox SAVE_CURRENT_ANALYSIS_WITH_NAME_INPUT_BOX = controlFactory().inputBox(By.id("pipelineNameInput"));
    private static final Button SAVE_PIPELINE_BUTTON = controlFactory().button(By.cssSelector("[ng-click='savePipeline()']"));
    private static final Button CLOSE_DIALOG_BUTTON = controlFactory().button(By.xpath("//*[@id='save-analysis-dialog']//button[contains(text(),'Close')]"));
    private Button savedAnalysisLink(String name) {
        return new Button(By.xpath("//a[text()='" + name + "']"));
    }
    private Button removeSavedAnalysisButton(String name) {
        return new Button(By.xpath("//a[text()='" + name + "']/../a[@class='button-delete']"));
    }

    public ProcessingResultsPageSteps saveAnalysis(String analysisName){
        SAVE_CURRENT_ANALYSIS_WITH_NAME_INPUT_BOX.fillIn(analysisName);
        SAVE_PIPELINE_BUTTON.click();
        return new ProcessingResultsPageSteps();
    }

    public AnalysisManagementDialog deleteSavedAnalysis(String name){
        removeSavedAnalysisButton(name).click();
        return this;
    }

    public ProcessingResultsPageSteps loadSavedAnalysis(String name){
        savedAnalysisLink(name).click();
        return new ProcessingResultsPageSteps();
    }

    public ProcessingResultsPageSteps closeDialog(){
        CLOSE_DIALOG_BUTTON.click();
        return new ProcessingResultsPageSteps();
    }



}
