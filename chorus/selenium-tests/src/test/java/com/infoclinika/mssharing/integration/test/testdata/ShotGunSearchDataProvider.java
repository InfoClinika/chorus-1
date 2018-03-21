package com.infoclinika.mssharing.integration.test.testdata;

import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.*;
import com.infoclinika.mssharing.integration.test.utils.CSVFileReader;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class ShotGunSearchDataProvider {

    @DataProvider(name = "openSearchAndVerifyNumberOfFiles")
    public static Object[][] openSearchAndVerifyNumberOfFiles() {
        ShotGunSearchData shotGunSearchData = new ShotGunSearchData.Builder()
                .annotationItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/raw/protein/raw_protein_annotation_table.csv"))
                .dataItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/raw/protein/raw_protein_data_table.csv"))
                .build();
        return new Object[][]{{shotGunSearchData}};
    }

    @DataProvider(name = "selectNormalizedPeptide")
    public static Object[][] selectNormalizedPeptide() {
        ShotGunSearchData shotGunSearchData = new ShotGunSearchData.Builder()
                .annotationItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/normalized/peptide/normalized_peptide_annotation_table.csv"))
                .dataItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/normalized/peptide/normalized_peptide_data_table.csv"))
                .composeBy("Normalized")
                .viewLevel("Peptide")
                .build();
        return new Object[][]{{shotGunSearchData}};
    }

    @DataProvider(name = "selectConditionIsotopeGroup")
    public static Object[][] selectConditionIsotopeGroup() {
        ShotGunSearchData shotGunSearchData = new ShotGunSearchData.Builder()
                .annotationItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/condition/isotopegroup/condition_isotopegroup_annotation_table.csv"))
                .composeBy("Condition")
                .viewLevel("Isotope Group")
                .build();
        return new Object[][]{{shotGunSearchData}};
    }

    @DataProvider(name = "selectRatioFeature")
    public static Object[][] selectRatioFeature() {
        ShotGunSearchData shotGunSearchData = new ShotGunSearchData.Builder()
                .annotationItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/ratio/feature/ratio_feature_annotation_table.csv"))
                .composeBy("Ratio")
                .viewLevel("Feature")
                .build();
        return new Object[][]{{shotGunSearchData}};
    }

    @DataProvider(name = "normalizeIntensitiesSheetAndZScoreAndANOVA")
    public static Object[][] normalizeIntensitiesSheetAndZScoreAndANOVA() {
        AlgorithmData normalizationAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.NORMALIZATION)
                .sheet(Sheet.INTENSITIES_SHEET).build();
        AlgorithmData zScoreAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.Z_SCORE)
                .filterZScore(true)
                .sampleZScore(false)
                .sheet(Sheet.INTENSITIES_SHEET).build();
        AlgorithmData aNovaAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.ANOVA)
                .sheet(Sheet.INTENSITIES_SHEET)
                .runFDR(false)
                .groupColumnName(GroupColumnName.CONDITION_ID)
                .build();
        List<AlgorithmData> algorithmDataList = new ArrayList<>();
        algorithmDataList.add(normalizationAlgorithm);
        algorithmDataList.add(zScoreAlgorithm);
        algorithmDataList.add(aNovaAlgorithm);
        ShotGunSearchData shotGunSearchData = new ShotGunSearchData.Builder()
                .annotationItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/complexalgorithms/normalized_zscore_anova_annotation_table.csv"))
                .dataItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/complexalgorithms/normalized_zscore_anova_data_table.csv"))
                .algorithmData(algorithmDataList)
                .build();
        return new Object[][]{{shotGunSearchData}};
    }

    @DataProvider(name = "filteringAndNormalizationAndLog2AndRatio")
    public static Object[][] filteringAndNormalizationAndLog2AndRatio() {
        List<FilterSettingsData> filterSettingsDataList = new ArrayList<>();
        FilterSettingsData filterSettingsData = new FilterSettingsData.Builder()
                .filterBy(FilterBy.ROW_ANNOTATIONS)
                .filter(Filter.PROTEIN_COVERAGE)
                .matchIf(MatchIf.GREATER_THAN)
                .value("3").build();
        filterSettingsDataList.add(filterSettingsData);
        AlgorithmData filteringAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.FILTERING)
                .filterSettingsData(filterSettingsDataList).build();
        AlgorithmData normalizationAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.NORMALIZATION)
                .sheet(Sheet.INTENSITIES_SHEET).build();
        AlgorithmData log2Algorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.LOG2)
                .sheet(Sheet.INTENSITIES_SHEET).build();
        AlgorithmData ratioAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.RATIO)
                .sheet(Sheet.INTENSITIES_SHEET)
                .build();
        List<AlgorithmData> algorithmDataList = new ArrayList<>();
        algorithmDataList.add(filteringAlgorithm);
        algorithmDataList.add(normalizationAlgorithm);
        algorithmDataList.add(log2Algorithm);
        algorithmDataList.add(ratioAlgorithm);
        ShotGunSearchData shotGunSearchData = new ShotGunSearchData.Builder()
                .annotationItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/complexalgorithms/filtering_normalization_log2_ratio_annotation_table.csv"))
                .dataItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/complexalgorithms/filtering_normalization_log2_ratio_data_table.csv"))
                .algorithmData(algorithmDataList)
                .build();
        return new Object[][]{{shotGunSearchData}};
    }

    @DataProvider(name = "saveAnalysisAndThenLoad")
    public static Object[][] saveAnalysisAndThenLoad() {
        SortingSettingsData sortingSettingsData = new SortingSettingsData.Builder()
                .dimension(FilterBy.ROW_ANNOTATIONS)
                .sortBy(Filter.PROTEIN_ID)
                .direction(Direction.DESC).build();
        AlgorithmData sortingAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.SORTING)
                .sortingSettingsData(sortingSettingsData).build();
        AlgorithmData logNAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.LOGN)
                .sheet(Sheet.INTENSITIES_SHEET).build();
        AlgorithmData aNovaAlgorithm = new AlgorithmData.Builder()
                .algorithm(Algorithm.ANOVA)
                .sheet(Sheet.INTENSITIES_SHEET)
                .runFDR(true)
                .groupColumnName(GroupColumnName.FACTOR_CONCENTRATION)
                .build();
        List<AlgorithmData> algorithmDataList = new ArrayList<>();
        algorithmDataList.add(sortingAlgorithm);
        algorithmDataList.add(logNAlgorithm);
        algorithmDataList.add(aNovaAlgorithm);
        ShotGunSearchData shotGunSearchData = new ShotGunSearchData.Builder()
                .annotationItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/complexalgorithms/sorting_logn_anova_annotation_table.csv"))
                .dataItemsFromCsvFile(CSVFileReader.getDataFromCsvFile("/data/proteinidsearchdata/shotgun/complexalgorithms/sorting_logn_anova_data_table.csv"))
                .algorithmData(algorithmDataList)
                .build();
        UserData userData = new UserData.Builder().email("pavel.kaplin@gmail.com").password("pwd").build();
        String analysisName = randomizeName("Analysis");
        return new Object[][]{{shotGunSearchData, userData, analysisName}};
    }


}
