package com.infoclinika.mssharing.integration.test.test.uploadfiles;

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
public class UploadFilesDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] uploadRawFilesUsingBrowseButton() {
        FileData fileData = new FileData(
                randomizeFileName(".RAW"),
                randomizeFileName(".RAW"),
                randomizeFileName(".RAW")
        );
        fileData.createFiles();
        return new Object[][]{{fileData, environmentSpecificData.defaultInstrument}};
    }

    @DataProvider
    public static Object[][] uploadThreeWiffFilesUsingBrowseButton() {
        String fileName = randomizeName("file");
        FileData fileData = new FileData(
                fileName + ".wiff",
                fileName + ".wiff.scan",
                fileName + ".wiff.mtd"
        );
        FileData zippedFiles = new FileData(fileName + ".wiff.zip");
        fileData.createFiles();
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_AB_SCIEX)
                .model(SampleData.MODEL_TRIPLE_TOF_5600)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("sciex")).build();
        return new Object[][]{{fileData, zippedFiles, instrumentData}};
    }

    @DataProvider
    public static Object[][] uploadTwoWiffFilesUsingBrowseButton() {
        String fileName = randomizeName("file");
        FileData fileData = new FileData(
                fileName + ".wiff",
                fileName + ".wiff.scan"
        );
        FileData zippedFiles = new FileData(fileName + ".wiff.zip");
        fileData.createFiles();
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_AB_SCIEX)
                .model(SampleData.MODEL_TRIPLE_TOF_5600)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("sciex")).build();
        return new Object[][]{{fileData, zippedFiles, instrumentData}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToUploadWiffFilesWithoutMainWiffFile() {
        String fileName = randomizeName("file");
        FileData fileData = new FileData(
                fileName + ".wiff.scan",
                fileName + ".wiff.mtd"
        );
        fileData.createFiles();
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_AB_SCIEX)
                .model(SampleData.MODEL_TRIPLE_TOF_5600)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("sciex")).build();
        return new Object[][]{{fileData, instrumentData}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToUploadOneSetOfWiffFilesIfNamesAreNotEqual() {
        String fileName = randomizeName("file");
        FileData fileData = new FileData(
                fileName + "1.wiff",
                fileName + "2.wiff.scan",
                fileName + "3.wiff.mtd"
        );
        fileData.createFiles();
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_AB_SCIEX)
                .model(SampleData.MODEL_TRIPLE_TOF_5600)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("sciex")).build();
        return new Object[][]{{fileData, instrumentData}};
    }

    @DataProvider
    public static Object[][] resumeUpload() {
        FileData fileData = new FileData(
                randomizeFileName(".RAW")
        );
        fileData.createFilesWithSizeInMb(50);
        return new Object[][]{{fileData, environmentSpecificData.defaultInstrument}};
    }
}
