package com.infoclinika.mssharing.integration.test.test.billing;

import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.data.instrument.OptionalFields;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeFileName;
import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class BillingFeatureDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] uploadFilesWithEnabledAutoTranslateOption() {
        OptionalFields optionalFields = new OptionalFields.Builder()
                .isAutoTranslate(true).build();
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial"))
                .optionalFields(optionalFields).build();
        FileData fileData = new FileData(
                randomizeFileName(".RAW")
        );
        fileData.createFiles();
        return new Object[][]{{instrumentData, fileData}};
    }

    @DataProvider
    public static Object[][] enableAutoTranslationOptionInUploadDialog() {
        OptionalFields optionalFields = new OptionalFields.Builder()
                .isAutoTranslate(false).build();
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial"))
                .optionalFields(optionalFields).build();
        FileData fileData = new FileData(
                randomizeFileName(".RAW")
        );
        fileData.createFiles();
        return new Object[][]{{instrumentData, fileData}};
    }

    @DataProvider
    public static Object[][] archiveFile() {
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
        return new Object[][]{{instrumentData, fileData}};
    }
}
