package com.infoclinika.mssharing.integration.test.test.billing;

import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.filelist.FileListSteps;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Orlov
 */
public class BillingFeature extends BaseTest {

    @Test(dataProvider = "uploadFilesWithEnabledAutoTranslateOption", dataProviderClass = BillingFeatureDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void uploadFilesWithEnabledAutoTranslateOption(InstrumentData instrumentData, FileData fileData) {
        String fileName = fileData.getFiles()[0];
        dashboardPageSteps
                .createInstrument(instrumentData)
                .uploadFiles(fileData, instrumentData.getName(), 300);
        assertTrue(dashboardPageSteps.getFileListSteps().isFilePresent(fileName), "File " + fileName +
                " is not displayed in the File List");
        boolean isFileInProgressOfTranslation = dashboardPageSteps.getFileListSteps().isFileInProgressOfTranslation(fileName);
        boolean isFileNotArchived = dashboardPageSteps.getFileListSteps().isFileUnarchived(fileName);
        assertTrue(isFileInProgressOfTranslation, "File should has 'Translation In Progress' status, but it has another status");
        assertTrue(isFileNotArchived, "File is archived, but should not be");
        dashboardPageSteps.getFileListSteps().deleteFiles(fileData);
        dashboardPageSteps.getSidebarMenuSteps().selectInstrumentsItem().deleteInstrument(instrumentData.getName());
    }

    @Test(enabled = false, dataProvider = "enableAutoTranslationOptionInUploadDialog", dataProviderClass = BillingFeatureDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void enableAutoTranslationOptionInUploadDialog(InstrumentData instrumentData, FileData fileData) {
        String fileName = fileData.getFiles()[0];
        dashboardPageSteps
                .createInstrument(instrumentData)
                .uploadFilesAndEnableAutoTranslateOption(fileData, instrumentData.getName());
        assertTrue(dashboardPageSteps.getFileListSteps().isFilePresent(fileName), "File " + fileName +
                " is not displayed in the File List");
        boolean isFileInProgressOfTranslation = dashboardPageSteps.getFileListSteps().isFileInProgressOfTranslation(fileName);
        boolean isFileNotArchived = dashboardPageSteps.getFileListSteps().isFileUnarchived(fileName);
        assertTrue(isFileInProgressOfTranslation, "File should has 'Translation In Progress' status, but it has another status");
        assertTrue(isFileNotArchived, "File is archived, but should not be");
        dashboardPageSteps.getFileListSteps().deleteFiles(fileData);
        dashboardPageSteps.getSidebarMenuSteps().selectInstrumentsItem().deleteInstrument(instrumentData.getName());
    }

    @Test(enabled = false, dataProvider = "archiveFile", dataProviderClass = BillingFeatureDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void archiveFile(InstrumentData instrumentData, FileData fileData) {
        String fileName = fileData.getFiles()[0];
        FileListSteps fileListSteps = dashboardPageSteps
                .createInstrument(instrumentData)
                .uploadFiles(fileData, instrumentData.getName(), 300)
                .getFileListSteps()
                .archiveFile(fileName);
        assertTrue(fileListSteps.isFileArchived(fileName), "File is not archived");
        fileListSteps.deleteFile(fileName);
        dashboardPageSteps.getSidebarMenuSteps().selectInstrumentsItem().deleteInstrument(instrumentData.getName());
    }

}
