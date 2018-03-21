package com.infoclinika.mssharing.integration.test.test;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.ShotGunSearchData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.Environment;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.LoginPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch.ProcessingResultsPageSteps;
import com.infoclinika.mssharing.integration.test.testdata.ShotGunSearchDataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.navigateTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Orlov
 */

public class ShotGunSearch extends BaseTest {

    private final static String PRODUCTION_URL = "https://chorusproject.org/pages/dashboard.html";
    private final static String SHOTGUN_SEARCH_URL = "https://chorusproject.org/pages/sequest-search-board.html#/protein-search/515/results";

    @Test(dataProvider = "openSearchAndVerifyNumberOfFiles", dataProviderClass = ShotGunSearchDataProvider.class)
    @Environment(url = SHOTGUN_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void openSearchAndVerifyNumberOfFiles(ShotGunSearchData shotGunSearchData) {
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps.pressTableAppearanceSettingsButton()
                .selectAllCheckboxesAndApply();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(4, 7);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(4, 4);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test(dataProvider = "selectNormalizedPeptide", dataProviderClass = ShotGunSearchDataProvider.class)
    @Environment(url = SHOTGUN_SEARCH_URL)
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
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(17, 11);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(17, 4);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test(dataProvider = "selectConditionIsotopeGroup", dataProviderClass = ShotGunSearchDataProvider.class)
    @Environment(url = SHOTGUN_SEARCH_URL)
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

    @Test(dataProvider = "selectRatioFeature", dataProviderClass = ShotGunSearchDataProvider.class)
    @Environment(url = SHOTGUN_SEARCH_URL)
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

    @Test(dataProvider = "normalizeIntensitiesSheetAndZScoreAndANOVA", dataProviderClass = ShotGunSearchDataProvider.class)
    @Environment(url = SHOTGUN_SEARCH_URL)
    @LoginRequired(isDisableBillingNotification = false)
    public void normalizeIntensitiesSheetAndZScoreAndANOVA(ShotGunSearchData shotGunSearchData){
        ProcessingResultsPageSteps processingResultsPageSteps = new ProcessingResultsPageSteps();
        processingResultsPageSteps
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(0))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(1))
                .addAlgorithm(shotGunSearchData.getAlgorithmData().get(2))
                .pressExecutePipelineButton()
                .showAllColumnsInTable();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(4, 8);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(4, 4);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test(dataProvider = "filteringAndNormalizationAndLog2AndRatio", dataProviderClass = ShotGunSearchDataProvider.class)
    @Environment(url = SHOTGUN_SEARCH_URL)
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
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(3, 7);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(3, 1);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test(dataProvider = "saveAnalysisAndThenLoad", dataProviderClass = ShotGunSearchDataProvider.class)
    @Environment(url = SHOTGUN_SEARCH_URL)
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
        navigateTo(SHOTGUN_SEARCH_URL);
        LoginPageSteps loginPageSteps = new LoginPageSteps();
        loginPageSteps.login(pavelKaplin);
        processingResultsPageSteps
                .loadSavedAnalysis(analysisName)
                .showAllColumnsInTable();
        List<String> itemsFromAnnotationTable = processingResultsPageSteps.getValuesFromAnnotationTable(4, 9);
        List<String> itemsFromDataTable = processingResultsPageSteps.getValuesFromDataTable(4, 4);
        processingResultsPageSteps.deleteSavedAnalysis(analysisName);
        assertEquals(itemsFromAnnotationTable, shotGunSearchData.getAnnotationsItemsFromCsvFile(), "Data, that is shown in the annotation table has incorrect values");
        assertEquals(itemsFromDataTable, shotGunSearchData.getDataItemsFromCsvFile(), "Data, that is shown in the data table has incorrect values");
    }

    @Test
    @Environment(url = SHOTGUN_SEARCH_URL)
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
