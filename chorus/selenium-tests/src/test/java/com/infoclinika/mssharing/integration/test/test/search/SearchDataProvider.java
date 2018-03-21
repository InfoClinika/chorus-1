package com.infoclinika.mssharing.integration.test.test.search;

import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.experiment.AnalysisInfo;
import com.infoclinika.mssharing.integration.test.data.experiment.ExperimentData;
import com.infoclinika.mssharing.integration.test.data.experiment.FileSelectionInfo;
import com.infoclinika.mssharing.integration.test.data.experiment.GeneralInfo;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.data.projectdata.ProjectData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeFileName;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class SearchDataProvider extends AbstractDataProvider{

    @DataProvider
    public static Object[][] searchForProjectByTitle() {
        ProjectData projectData = new ProjectData.Builder()
                .name(randomizeName("Project"))
                .laboratory(SampleData.LAB_NO_LAB)
                .area(randomizeName("Area"))
                .description("Description").build();
        return new Object[][]{{projectData}};
    }

    @DataProvider
    public static Object[][] searchForExperimentByTitle() {
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

    @DataProvider
    public static Object[][] searchForInstrumentByTitle() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_AGILENT)
                .model(SampleData.MODEL_220_GCMS_ION_TRAP_SYSTEM)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("AH999X")).build();
        return new Object[][]{{instrumentData}};
    }

    @DataProvider
    public static Object[][] searchForFileByFileName() {
        FileData fileData = new FileData(
                "c15092005_000.RAW");
        return new Object[][]{{fileData}};
    }
}
