package com.infoclinika.mssharing.integration.test.test.experiment;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.experiment.*;
import com.infoclinika.mssharing.integration.test.data.projectdata.PersonToInvite;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class ExperimentDataProvider extends AbstractDataProvider {

    @DataProvider(name = "Create Experiment while staying on 'All Projects' Page")
    public static Object[][] createExperimentStayingOnAllProjectsPage() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileToPrepInfo fileToPrepInfo = new FileToPrepInfo.Builder()
                .prepsList(Lists.newArrayList("prep1", "prep2", "prep3")).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(3).build();
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo)
                .fileToPrepInfo(fileToPrepInfo)
                .build();
        return new Object[][]{{experimentData}};
    }

    @DataProvider(name = "Create Experiment while staying on 'All Experiments' Page")
    public static Object[][] createExperimentStayingOnAllExperimentsPage() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(3).build();
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo).build();
        return new Object[][]{{experimentData}};
    }

    @DataProvider(name = "Create Experiment with factors")
    public static Object[][] createExperimentWithFactors() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(3).build();
        ExperimentDesignInfo experimentDesignInfo1 = new ExperimentDesignInfo.Builder()
                .factorName("Factor1").valueType(FactorType.TEXT).build();
        ExperimentDesignInfo experimentDesignInfo2 = new ExperimentDesignInfo.Builder()
                .factorName("Factor2").valueType(FactorType.NUMBER).build();
        List<ExperimentDesignInfo> experimentDesignInfoList = new ArrayList<>();
        experimentDesignInfoList.add(experimentDesignInfo1);
        experimentDesignInfoList.add(experimentDesignInfo2);
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo)
                .experimentDesignInfo(experimentDesignInfoList).build();
        return new Object[][]{{experimentData}};
    }

    @DataProvider(name = "Verify, that 'Next' button is disabled if all required fields are not filled in")
    public static Object[][] experimentWizardFormsLimitationsDuringCreation() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(1).build();
        ExperimentDesignInfo experimentDesignInfo1 = new ExperimentDesignInfo.Builder()
                .factorName("Factor1").valueType(FactorType.TEXT).build();
        List<ExperimentDesignInfo> experimentDesignInfoList = new ArrayList<>();
        experimentDesignInfoList.add(experimentDesignInfo1);
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo)
                .experimentDesignInfo(experimentDesignInfoList).build();
        return new Object[][]{{experimentData}};
    }

    @DataProvider(name = "Verify, that 'Next' button is disabled in Edit Experiment Dialog, if all required fields are not filled in")
    public static Object[][] experimentWizardFormsLimitationsDuringEditing() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(1).build();
        ExperimentDesignInfo experimentDesignInfo = new ExperimentDesignInfo.Builder()
                .factorName("Factor1").valueType(FactorType.TEXT).build();
        List<ExperimentDesignInfo> experimentDesignInfoList = new ArrayList<>();
        experimentDesignInfoList.add(experimentDesignInfo);
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo)
                .experimentDesignInfo(experimentDesignInfoList).build();
        return new Object[][]{{experimentData, experimentDesignInfo.getFactorName()}};
    }

    @DataProvider(name = "Edit Experiment")
    public static Object[][] editExperiment() {
        GeneralInfo generalInfo1 = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument)
                .description("Description").build();
        AnalysisInfo analysisInfo1 = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileSelectionInfo fileSelectionInfo1 = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(1).build();
        ExperimentData experimentData1 = new ExperimentData.Builder()
                .generalInfo(generalInfo1)
                .fileSelectionInfo(fileSelectionInfo1)
                .analysisInfo(analysisInfo1).build();

        GeneralInfo generalInfo2 = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project2)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument)
                .description("Edited Description").build();
        AnalysisInfo analysisInfo2 = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_PROTEOMICS).build();
        FileSelectionInfo fileSelectionInfo2 = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(2).build();
        ExperimentData experimentData2 = new ExperimentData.Builder()
                .generalInfo(generalInfo2)
                .fileSelectionInfo(fileSelectionInfo2)
                .analysisInfo(analysisInfo2).build();
        return new Object[][]{{experimentData1, experimentData2}};
    }

    @DataProvider(name = "Remove Experiment")
    public static Object[][] removeExperiment() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(3).build();
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo).build();
        return new Object[][]{{experimentData}};
    }

    @DataProvider(name = "Verify, that alert message appears while attempt to create Experiment with the existing name")
    public static Object[][] shouldNotAllowToCreateExperimentsWithIdenticalNames() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(3).build();
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo).build();
        return new Object[][]{{experimentData, experimentData}};
    }

    @DataProvider(name = "Create Experiment with translation range")
    public static Object[][] createExperimentWithTranslationRangeAndLockMz() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        List<LockMz> lockMzList = new ArrayList<>();
        LockMz lockMz = new LockMz.Builder().lockMass("2").charge("+2").build();
        lockMzList.add(lockMz);
        TranslationRange translationRange = new TranslationRange.Builder()
                .minRt("1").maxRt("2")
                .minMz("1").maxMz("5.1")
                .lockMzList(lockMzList).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS)
                .translationRange(translationRange)
                .build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(3).build();

        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo).build();
        return new Object[][]{{experimentData}};
    }

    @DataProvider(name = "Share and download Experiment")
    public static Object[][] shareAndDownloadExperiment() {
        List<PersonToInvite> personToInviteList = new ArrayList<>();
        personToInviteList.add(new PersonToInvite.Builder()
                .name(SampleData.ALL)
                .build());
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(environmentSpecificData.defaultLaboratory)
                .area(randomizeName("Area"))
                .personToInvite(personToInviteList).build();
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(projectData.getName())
                .species(SampleData.SPECIES_UNSPECIFIED)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(1).build();
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo).build();
        return new Object[][]{{projectData, experimentData, environmentSpecificData.pavelKaplinAtGmail}};
    }

    @DataProvider(name = "Create Experiment with selected files")
    public static Object[][] createExperimentWithSelectedFiles() {
        GeneralInfo generalInfo = new GeneralInfo.Builder()
                .name(randomizeName("Experiment"))
                .project(environmentSpecificData.project1)
                .species(SampleData.SPECIES_ANOPHELES_GAMBIAE)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .instrumentModel(SampleData.VENDOR_THERMO + " - " + SampleData.MODEL_EXACTIVE_PLUS)
                .instrument(environmentSpecificData.defaultInstrument).build();
        AnalysisInfo analysisInfo = new AnalysisInfo.Builder()
                .experimentType(SampleData.EXPERIMENT_TYPE_METABOLOMICS).build();
        FileToPrepInfo fileToPrepInfo = new FileToPrepInfo.Builder()
                .prepsList(Lists.newArrayList("prep1", "prep2", "prep3")).build();
        FileSelectionInfo fileSelectionInfo = new FileSelectionInfo.Builder()
                .numberOfSelectedFiles(3).build();
        ExperimentData experimentData = new ExperimentData.Builder()
                .generalInfo(generalInfo)
                .fileSelectionInfo(fileSelectionInfo)
                .analysisInfo(analysisInfo)
                .fileToPrepInfo(fileToPrepInfo)
                .build();
        return new Object[][]{{experimentData}};
    }


}
