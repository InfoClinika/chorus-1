package com.infoclinika.mssharing.integration.test.test;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.ShotGunSearchData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.Environment;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.LoginPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch.ProcessingResultsPageSteps;
import com.infoclinika.mssharing.integration.test.testdata.DMSSearchDataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.navigateTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Orlov
 */
public class DMSSearch extends BaseTest {

    private static final String DMS_SEARCH_URL = "https://chorusproject.org/pages/sequest-search-board.html#/protein-search/514/results";

    @Test(dataProvider = "openSearchAndVerifyNumberOfFiles", dataProviderClass = DMSSearchDataProvider.class)
    @Environment(url = DMS_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void openSearchAndVerifyNumberOfFiles(ShotGunSearchData shotGunSearchData) {
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps.pressTableAppearanceSettingsButton()
                .selectAllCheckboxesAndApply();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(6, 7);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(6, 4);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test(dataProvider = "selectNormalizedPeptide", dataProviderClass = DMSSearchDataProvider.class)
    @Environment(url = DMS_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void selectNormalizedPeptide(ShotGunSearchData shotGunSearchData) {
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps
                .openGetAnalysisResultDataDialog()
                .selectItemInComposeByDropdown(shotGunSearchData.getComposeBy())
                .selectItemInViewLevelDropdown(shotGunSearchData.getViewLevel())
                .pressApply()
                .pressTableAppearanceSettingsButton()
                .selectAllCheckboxesAndApply();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(15, 11);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(15, 4);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test(dataProvider = "selectConditionIsotopeGroup", dataProviderClass = DMSSearchDataProvider.class)
    @Environment(url = DMS_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void selectConditionIsotopeGroup(ShotGunSearchData shotGunSearchData) {
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps
                .openGetAnalysisResultDataDialog()
                .selectItemInComposeByDropdown(shotGunSearchData.getComposeBy())
                .selectItemInViewLevelDropdown(shotGunSearchData.getViewLevel())
                .pressApply()
                .pressTableAppearanceSettingsButton()
                .selectAllCheckboxesAndApply();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(30, 16);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
    }

    @Test(dataProvider = "selectRatioFeature", dataProviderClass = DMSSearchDataProvider.class)
    @Environment(url = DMS_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void selectRatioFeature(ShotGunSearchData shotGunSearchData) {
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps
                .openGetAnalysisResultDataDialog()
                .selectItemInComposeByDropdown(shotGunSearchData.getComposeBy())
                .selectItemInViewLevelDropdown(shotGunSearchData.getViewLevel())
                .pressApply()
                .pressTableAppearanceSettingsButton()
                .selectAllCheckboxesAndApply();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(30, 16);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
    }

    @Test(dataProvider = "normalizeIntensitiesSheetAndZScoreAndANOVA", dataProviderClass = DMSSearchDataProvider.class)
    @Environment(url = DMS_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void normalizeIntensitiesSheetAndZScoreAndANOVA(ShotGunSearchData shotGunSearchData){
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(0))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(1))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(2))
                .pressExecutePipelineButton()
                .showAllColumnsInTable();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(6, 8);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(6, 4);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test(dataProvider = "filteringAndNormalizationAndLog2AndRatio", dataProviderClass = DMSSearchDataProvider.class)
    @Environment(url = DMS_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void filteringAndNormalizationAndLog2AndRatio(ShotGunSearchData shotGunSearchData){
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(0))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(1))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(2))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(3))
                .pressExecutePipelineButton()
                .showAllColumnsInTable();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(4, 7);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(4, 1);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test(dataProvider = "saveAnalysisAndThenLoad", dataProviderClass = DMSSearchDataProvider.class)
    @Environment(url = DMS_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void saveAnalysisAndThenLoad(ShotGunSearchData shotGunSearchData, UserData pavelKaplin, String analysisName){
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(0))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(1))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(2))
                .pressExecutePipelineButton()
                .saveAnalysisAs(analysisName)
                .pressBackButton()
                .getHeader().logout();
        navigateTo(DMS_SEARCH_URL);
        LoginPageSteps loginPageSteps = new LoginPageSteps();
        loginPageSteps.login(pavelKaplin);
        processingResultsPageSteps
                .loadSavedAnalysis(analysisName)
                .showAllColumnsInTable();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(6, 9);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(6, 4);
        processingResultsPageSteps.deleteSavedAnalysis(analysisName);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test
    @Environment(url = DMS_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void viewChart(){
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps
                .doubleClickOnTheRowInTable(1)
                .doubleClickOnTheRowInTable(1)
                .doubleClickOnTheRowInTable(1);
        assertTrue(processingResultsPageSteps.isTopChartImageLoaded(), "Top Chart is not shown");
        assertTrue(processingResultsPageSteps.isBottomChartImageLoaded(), "Bottom Chart is not shown");
    }





}
