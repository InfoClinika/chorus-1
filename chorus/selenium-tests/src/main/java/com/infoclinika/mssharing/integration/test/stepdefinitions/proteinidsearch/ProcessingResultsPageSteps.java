package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.*;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.Algorithm;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.AlgorithmData;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class ProcessingResultsPageSteps extends AbstractPageSteps {

    //Page Elements
    private static final Button AUTO_BUTTON = controlFactory().button(By.xpath("//button[text()='Auto']"));
    private static final Button MANUAL_BUTTON = controlFactory().button(By.xpath("//button[text()='Manual']"));
    private static final Button ANALYSIS_MANAGEMENT_BUTTON = controlFactory().button(By.xpath("//button[contains(@title,'Analysis Management')]"));
    private static final Button ADD_ALGORITHM_BUTTON = controlFactory().button(By.className("btn-add-filter"));
    private static final Button EXECUTE_PIPELINE_BUTTON = controlFactory().button(By.className("executePipelineButton"));
    private static final Button DATA_APPEARANCE_SETTINGS_BUTTON = controlFactory().button(By.cssSelector("[title='Data Appearance Settings']"));
    private static final Button GET_ANALYSIS_RESULT_DATA_BUTTON = controlFactory().button(By.cssSelector("[title='Get Analysis Results Data']"));
    private static final Button BACK_BUTTON = controlFactory().button(By.cssSelector("[title='Back']"));
    private static final Button SERVER_ERROR_CLOSE_BUTTON = controlFactory().button(By.xpath("//*[@id='server-error-message']/../div//button[text()='Close']"));
    private static final Frame CHARTS_FRAME = controlFactory().frame(By.cssSelector("iframe.chartAcrossAllFiles.all-space"));
    private static final Chart TOP_CHART_IMAGE = controlFactory().chart(By.xpath("//*[@id='top-chart']//img"));
    private static final Chart BOTTOM_CHART_IMAGE = controlFactory().chart(By.xpath("//*[@id='bottom-chart']//img"));
    private Label cellInDataTable(int rowNumber, int columnNumber) {
        return new Label(By.xpath("//*[@id='data-table']//tr[" + rowNumber + "]/td[" + columnNumber + "]"));
    }
    private Button cellInAnnotationTable(int rowNumber, int columnNumber) {
        return new Button(By.xpath("//*[@id='row-annotations-section']//tr[" + rowNumber + "]/td[" + columnNumber + "]/span"));
    }

    //Step Definitions
    public GetAnalysisResultDataDialogSteps openGetAnalysisResultDataDialog() {
        GET_ANALYSIS_RESULT_DATA_BUTTON.click();
        return new GetAnalysisResultDataDialogSteps();
    }

    public List<String> getValuesFromAnnotationTable(int rowNumbers, int columnNumbers) {
        List<String> itemsFromAnnotationTable = new ArrayList<>();
        for (int rowNumber = 1; rowNumber <= rowNumbers; rowNumber++) {
            for (int columnNumber = 1; columnNumber <= columnNumbers; columnNumber++) {
                String item = cellInAnnotationTable(rowNumber, columnNumber).getText();
                if (!item.isEmpty()) {
                    itemsFromAnnotationTable.add(item);
                }
            }
        }
        return itemsFromAnnotationTable;
    }

    public List<String> getValuesFromDataTable(int rowNumbers, int columnNumbers) {
        List<String> itemsFromDataTable = new ArrayList<>();
        for (int rowNumber = 1; rowNumber <= rowNumbers; rowNumber++) {
            for (int columnNumber = 1; columnNumber <= columnNumbers; columnNumber++) {
                String item = cellInDataTable(rowNumber, columnNumber).getText();
                if (!item.isEmpty()) {
                    itemsFromDataTable.add(item);
                }
            }
        }
        return itemsFromDataTable;
    }

    public TableAppearanceSettingsSteps pressTableAppearanceSettingsButton() {
        wait(1);
        DATA_APPEARANCE_SETTINGS_BUTTON.click();
        return new TableAppearanceSettingsSteps();
    }

    public ChooseAlgorithmDialogSteps pressAddAlgorithmButton() {
        wait(2);
        ADD_ALGORITHM_BUTTON.click();
        return new ChooseAlgorithmDialogSteps();
    }

    public ProcessingResultsPageSteps pressExecutePipelineButton() {
        EXECUTE_PIPELINE_BUTTON.click();
        return this;
    }

    public ProcessingResultsPageSteps addAlgorithm(AlgorithmData algorithmData) {
        Algorithm algorithm = algorithmData.getAlgorithm();
        AlgorithmSettingsDialogSteps algorithmSettingsDialogSteps = pressAddAlgorithmButton().selectAlgorithm(algorithm);
        switch (algorithm) {
            case NORMALIZATION:
            case RATIO:
            case LOG2:
            case LOG10:
            case LOGN: {
                algorithmSettingsDialogSteps.selectAlgorithmSheet(algorithmData.getSheet());
                break;
            }
            case CLUSTERING:
                break;
            case Z_SCORE: {
                ZScoreAlgorithmSettingsDialogStepsFixed zScoreAlgorithmSettingsDialogSteps
                        = (ZScoreAlgorithmSettingsDialogStepsFixed) algorithmSettingsDialogSteps;
                zScoreAlgorithmSettingsDialogSteps
                        .setFeatureZScore(algorithmData.isFeatureZScore())
                        .setSampleZScore(algorithmData.isSampleZScore())
                        .selectAlgorithmSheet(algorithmData.getSheet());
                break;
            }
            case ANOVA:
                ANOVAAlgorithmSettingsDialogSteps anovaAlgorithmSettingsDialogSteps
                        = (ANOVAAlgorithmSettingsDialogSteps) algorithmSettingsDialogSteps;
                if (algorithmData.getGroupColumnName() != null) {
                    anovaAlgorithmSettingsDialogSteps.selectGroupColumnName(algorithmData.getGroupColumnName());
                }
                anovaAlgorithmSettingsDialogSteps
                        .setRunFDRCheckbox(algorithmData.isRunFDR())
                        .selectAlgorithmSheet(algorithmData.getSheet());
                break;
            case FILTERING:
                FilteringSettingsDialogSteps filteringSettingsDialogSteps
                        = (FilteringSettingsDialogSteps) algorithmSettingsDialogSteps;
                filteringSettingsDialogSteps
                        .setFilterSettings(algorithmData.getFilterSettingsData());
                break;
            case SORTING:
                SortingSettingsDialogSteps sortingSettingsDialogSteps
                        = (SortingSettingsDialogSteps) algorithmSettingsDialogSteps;
                sortingSettingsDialogSteps
                        .setSortingSettings(algorithmData.getSortingSettingsData());
                break;
        }
        return algorithmSettingsDialogSteps.pressApplyButton();
    }

    public ProcessingResultsPageSteps showAllColumnsInTable() {
        return pressTableAppearanceSettingsButton()
                .selectColumnsTab()
                .selectAllCheckboxesAndApply();
    }

    public AnalysisManagementDialog pressAnalysisManagementButton(){
        wait(2);
        ANALYSIS_MANAGEMENT_BUTTON.click();
        return new AnalysisManagementDialog();
    }

    public ProcessingResultsPageSteps saveAnalysisAs(String name){
        return pressAnalysisManagementButton().saveAnalysis(name);
    }

    public ProcessingResultsPageSteps loadSavedAnalysis(String name){
        return pressAnalysisManagementButton().loadSavedAnalysis(name);
    }

    public ProcessingResultsPageSteps deleteSavedAnalysis(String name){
        return pressAnalysisManagementButton().deleteSavedAnalysis(name).closeDialog();
    }

    public DashboardPageSteps pressBackButton(){
        BACK_BUTTON.click();
        return new DashboardPageSteps();
    }

    public ProcessingResultsPageSteps doubleClickOnTheRowInTable(int row){
        cellInAnnotationTable(row, 4).doubleClick();
        return this;
    }

    public boolean isTopChartImageLoaded(){
        CHARTS_FRAME.switchToFrame();
        boolean isLoaded = TOP_CHART_IMAGE.isChartImageLoaded();
        CHARTS_FRAME.backOutOfFrame();
        return isLoaded;
    }

    public boolean isBottomChartImageLoaded(){
        CHARTS_FRAME.switchToFrame();
        boolean isLoaded = BOTTOM_CHART_IMAGE.isChartImageLoaded();
        CHARTS_FRAME.backOutOfFrame();
        return isLoaded;
    }
}
