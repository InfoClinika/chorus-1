package com.infoclinika.mssharing.integration.test.test.files;

import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeFileName;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class FilesDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] verifyCounterOfSelectedFiles() {
        int numberOfSelectedFiles = 3;
        String counter1 = "3 files";
        int numberOfDeselectedFile1 = 1;
        String counter2 = "2 files";
        int numberOfDeselectedFile2 = 2;
        String counter3 = "1 file";
        return new Object[][]{{numberOfSelectedFiles, counter1, numberOfDeselectedFile1, counter2, numberOfDeselectedFile2, counter3}};
    }

    @DataProvider
    public static Object[][] bulkAppendingExistingLabels() {
        int numberOfFile1 = 1;
        int numberOfFile2 = 2;
        String addedLabel = randomizeName("new");
        return new Object[][]{{numberOfFile1, numberOfFile2, addedLabel}};
    }

    @DataProvider
    public static Object[][] bulkReplacingLabels() {
        int numberOfFile1 = 1;
        int numberOfFile2 = 2;
        String addedLabel = randomizeName("new");
        return new Object[][]{{numberOfFile1, numberOfFile2, addedLabel}};
    }

    @DataProvider
    public static Object[][] cancelLabelsEditing() {
        int numberOfFile1 = 1;
        int numberOfFile2 = 2;
        String addedLabel = randomizeName("new");
        return new Object[][]{{numberOfFile1, numberOfFile2, addedLabel}};
    }

    @DataProvider
    public static Object[][] bulkRemovingFiles() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        FileData fileData = new FileData(
                randomizeFileName(".RAW"),
                randomizeFileName(".RAW"),
                randomizeFileName(".RAW")
        );
        fileData.createFiles();
        return new Object[][]{{instrumentData, fileData}};
    }

    @DataProvider
    public static Object[][] bulkChangingFilesSpecies() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        FileData fileData = new FileData(
                randomizeFileName(".RAW")
        );
        fileData.createFiles();
        String species = "Escherichia coli";
        return new Object[][]{{instrumentData, fileData, species}};
    }

    @DataProvider
    public static Object[][] changingFileSpecies() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        FileData fileData = new FileData(
                randomizeFileName(".RAW")
        );
        fileData.createFiles();
        String species = "Escherichia coli";
        return new Object[][]{{instrumentData, fileData, species}};
    }
}
