package com.infoclinika.mssharing.integration.test.test.uploadfiles;

import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.filelist.FileListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles.ResumeUploadDialogSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles.UploadFilesStep2Steps;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Orlov
 */
public class UploadFiles extends BaseTest {

    @Test(dataProvider = "uploadRawFilesUsingBrowseButton", dataProviderClass = UploadFilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void uploadRawFilesUsingBrowseButton(FileData fileData, String instrumentName) {
        UploadFilesStep2Steps uploadFilesStep2Steps = dashboardPageSteps.getTopPanelSteps().pressUploadFilesButton()
                .selectInstrument(instrumentName)
                .pressNextButton()
                .pressBrowseButton()
                .selectAndAddFiles(fileData);
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[0]));
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[1]));
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[2]));
        uploadFilesStep2Steps
                .pressNextButton()
                .pressUploadButton()
                .waitForFinishUploading(fileData, 300)
                .pressOkButton();
        FileListSteps fileList = dashboardPageSteps.getSidebarMenuSteps().selectItemMyFiles();
        assertTrue(fileList.isFilePresent(fileData.getFiles()[0]));
        assertTrue(fileList.isFilePresent(fileData.getFiles()[1]));
        assertTrue(fileList.isFilePresent(fileData.getFiles()[2]));
        //clearing data
        fileList.deleteFiles(fileData);
    }

    @Test(dataProvider = "uploadThreeWiffFilesUsingBrowseButton", dataProviderClass = UploadFilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void uploadThreeWiffFilesUsingBrowseButton(FileData fileData, FileData zippedFiles, InstrumentData sciexInstrument){
        UploadFilesStep2Steps uploadFilesStep2Steps = dashboardPageSteps.createInstrument(sciexInstrument)
                .getTopPanelSteps().pressUploadFilesButton()
                .selectInstrument(sciexInstrument.getName())
                .pressNextButton()
                .pressBrowseButton()
                .selectAndAddFiles(fileData);
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[0]),
                ".wiff file is not presented in list of files after adding");
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[1]),
                ".wiff.scan file is not presented in list of files after adding");
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[2]),
                ".wiff.mtd file is not presented in list of files after adding");
        assertTrue(uploadFilesStep2Steps.isNextButtonEnabled(),
                "'Next' button is not enabled though all files were added.");
        uploadFilesStep2Steps
                .pressNextButton()
                .pressUploadButton()
                .waitForFinishUploading(zippedFiles, 300)
                .pressOkButton();
        FileListSteps fileList = dashboardPageSteps.getSidebarMenuSteps().selectItemMyFiles();
        assertTrue(fileList.isFilePresent(zippedFiles.getFiles()[0]),
                "Zipped .wiff file does not present in the file list");
        fileList.deleteFile(zippedFiles.getFiles()[0]);
        //clearing data
        dashboardPageSteps.getSidebarMenuSteps()
                .selectInstrumentsItem()
                .deleteInstrument(sciexInstrument.getName());
    }

    @Test(dataProvider = "uploadTwoWiffFilesUsingBrowseButton", dataProviderClass = UploadFilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void uploadTwoWiffFilesUsingBrowseButton(FileData fileData, FileData zippedFiles, InstrumentData sciex){
        UploadFilesStep2Steps uploadFilesStep2Steps = dashboardPageSteps.createInstrument(sciex)
                .getTopPanelSteps().pressUploadFilesButton()
                .selectInstrument(sciex.getName())
                .pressNextButton()
                .pressBrowseButton()
                .selectAndAddFiles(fileData);
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[0]),
                ".wiff file is not presented in list of files after adding");
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[1]),
                ".wiff.scan file is not presented in list of files after adding");
        assertTrue(uploadFilesStep2Steps.isNextButtonEnabled(),
                "'Next' button is not enabled though all files were added.");
        uploadFilesStep2Steps
                .pressNextButton()
                .pressUploadButton()
                .waitForFinishUploading(zippedFiles, 300)
                .pressOkButton();
        FileListSteps fileList = dashboardPageSteps.getSidebarMenuSteps().selectItemMyFiles();
        assertTrue(fileList.isFilePresent(zippedFiles.getFiles()[0]),
                "Zipped .wiff file does not present in the file list");
        //clearing data
        fileList.deleteFile(zippedFiles.getFiles()[0]);
        dashboardPageSteps.getSidebarMenuSteps()
                .selectInstrumentsItem()
                .deleteInstrument(sciex.getName());
    }

    @Test(dataProvider = "shouldNotAllowToUploadWiffFilesWithoutMainWiffFile", dataProviderClass = UploadFilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void shouldNotAllowToUploadWiffFilesWithoutMainWiffFile(FileData fileData, InstrumentData sciex){
        UploadFilesStep2Steps uploadFilesStep2Steps = dashboardPageSteps.createInstrument(sciex)
                .getTopPanelSteps().pressUploadFilesButton()
                .selectInstrument(sciex.getName())
                .pressNextButton()
                .pressBrowseButton()
                .selectAndAddFiles(fileData);
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[0]),
                ".wiff.scan file is not presented in list of files after adding");
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[1]),
                ".wiff.mtd file is not presented in list of files after adding");
        assertFalse(uploadFilesStep2Steps.isNextButtonEnabled(),
                "'Next' button is enabled, though main .wiff file is not added");
        uploadFilesStep2Steps.pressCancelButton();
        //clearing data
        dashboardPageSteps.getInstrumentListSteps()
                .deleteInstrument(sciex.getName());
    }

    @Test(dataProvider = "shouldNotAllowToUploadOneSetOfWiffFilesIfNamesAreNotEqual", dataProviderClass = UploadFilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void shouldNotAllowToUploadOneSetOfWiffFilesIfNamesAreNotEqual(FileData fileData, InstrumentData sciex){
        UploadFilesStep2Steps uploadFilesStep2Steps = dashboardPageSteps.createInstrument(sciex)
                .getTopPanelSteps().pressUploadFilesButton()
                .selectInstrument(sciex.getName())
                .pressNextButton()
                .pressBrowseButton()
                .selectAndAddFiles(fileData);
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[0]),
                ".wiff file is not presented in list of files after adding");
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[1]),
                ".wiff.scan file is not presented in list of files after adding");
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileData.getFiles()[2]),
                ".wiff.mtd file is not presented in list of files after adding");
        assertFalse(uploadFilesStep2Steps.isNextButtonEnabled(),
                "'Next' button is enabled, files have different names, though");
        uploadFilesStep2Steps.pressCancelButton();
        //clearing data
        dashboardPageSteps.getInstrumentListSteps()
                .deleteInstrument(sciex.getName());
    }

    @Test(dataProvider = "resumeUpload", dataProviderClass = UploadFilesDataProvider.class, groups = {"staging"}, enabled = false)
    @LoginRequired
    public void resumeUpload(FileData fileData, String instrumentName){
        String fileName = fileData.getFiles()[0];
        UploadFilesStep2Steps uploadFilesStep2Steps = dashboardPageSteps.getTopPanelSteps().pressUploadFilesButton()
                .selectInstrument(instrumentName)
                .pressNextButton()
                .pressBrowseButton()
                .selectAndAddFiles(fileData);
        assertTrue(uploadFilesStep2Steps.isFilePresent(fileName));
        uploadFilesStep2Steps
                .pressNextButton()
                .pressUploadButton()
                .interruptUploadingByRefreshingThePage()
                .pressCancelButton();
        ResumeUploadDialogSteps resumeUploadDialogSteps = dashboardPageSteps.getTopPanelSteps().pressResumeUploadButton();
        assertTrue(resumeUploadDialogSteps.isFilePresent(fileName));
        resumeUploadDialogSteps
                .pressResumeUploadButtonForFile(fileName)
                .selectAndAddFile(fileData)
                .pressResumeButton()
                .waitForFinishUploading(fileName,300)
                .closeDialog();
        FileListSteps fileListSteps = dashboardPageSteps.getSidebarMenuSteps().selectItemMyFiles();
        assertTrue(fileListSteps.isFilePresent(fileName), "File is not present in file list after resuming uploading");
        //clearing data
        fileListSteps.deleteFile(fileName);
    }
}

