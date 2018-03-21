package com.infoclinika.mssharing.integration.test.test.files;

import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.filelist.FileListSteps;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.startDriver;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Orlov
 */
public class Files extends BaseTest {

    @Test(dataProvider = "verifyCounterOfSelectedFiles", dataProviderClass = FilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void verifyCounterOfSelectedFiles(int numberOfSelectedFiles, String firstCounter, int fileToDeselect1, String secondCounter, int fileToDeselect2, String thirdCounter) {
        FileListSteps fileListSteps = dashboardPageSteps.getSidebarMenuSteps()
                .selectItemMyFiles()
                .selectFiles(numberOfSelectedFiles);
        assertEquals(fileListSteps.getSelectedFilesCounterValue(), firstCounter,
                "Counter of selected files shows wrong value, when 3 files have been selected");
        fileListSteps.selectFile(fileToDeselect1);
        assertEquals(fileListSteps.getSelectedFilesCounterValue(), secondCounter,
                "Counter of selected files shows wrong value, when one of three files has been deselected");
        fileListSteps.selectFile(fileToDeselect2);
        assertEquals(fileListSteps.getSelectedFilesCounterValue(), thirdCounter,
                "Counter of selected files shows wrong value, when only 1 file have been selected");
    }

    @Test(dataProvider = "bulkAppendingExistingLabels", dataProviderClass = FilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void bulkAppendingExistingLabels(int numberOfFile1, int numberOfFile2, String addedLabel) {
        FileListSteps fileListSteps = dashboardPageSteps.getSidebarMenuSteps()
                .selectItemMyFiles();
        String label1 = fileListSteps.getFileLabel(numberOfFile1);
        String label2 = fileListSteps.getFileLabel(numberOfFile2);
        fileListSteps
                .selectFile(numberOfFile1)
                .selectFile(numberOfFile2)
                .pressEditSelectedFilesButton()
                .selectEditLabelsPart()
                .selectAppendExistingLabels()
                .specifyNewLabels(addedLabel)
                .pressSave();
        assertEquals(fileListSteps.getFileLabel(numberOfFile1), label1 + " " + addedLabel,
                "Labels which are displayed for file in the file list do not meet expected result");
        assertEquals(fileListSteps.getFileLabel(numberOfFile2), label2 + " " + addedLabel,
                "Labels which are displayed for file in the file list do not meet expected result");
        //restoring defaults
        fileListSteps
                .selectFile(numberOfFile1)
                .pressEditSelectedFilesButton()
                .selectEditLabelsPart()
                .selectReplaceLabels()
                .specifyNewLabels(label1)
                .pressSave();
        fileListSteps
                .selectFile(numberOfFile2)
                .pressEditSelectedFilesButton()
                .selectEditLabelsPart()
                .selectReplaceLabels()
                .specifyNewLabels(label2)
                .pressSave();
    }

    @Test(dataProvider = "bulkReplacingLabels", dataProviderClass = FilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void bulkReplacingLabels(int numberOfFile1, int numberOfFile2, String newLabel) {
        //setup
        FileListSteps fileListSteps = dashboardPageSteps.getSidebarMenuSteps()
                .selectItemMyFiles();
        String label1 = fileListSteps.getFileLabel(numberOfFile1);
        String label2 = fileListSteps.getFileLabel(numberOfFile2);
        //test
        fileListSteps
                .selectFile(numberOfFile1)
                .selectFile(numberOfFile2)
                .pressEditSelectedFilesButton()
                .selectEditLabelsPart()
                .selectReplaceLabels()
                .specifyNewLabels(newLabel)
                .pressSave();
        assertEquals(fileListSteps.getFileLabel(numberOfFile1), newLabel,
                "Labels which are displayed for file in the file list do not meet expected result");
        assertEquals(fileListSteps.getFileLabel(numberOfFile2), newLabel,
                "Labels which are displayed for file in the file list do not meet expected result");
        //restoring defaults
        fileListSteps
                .selectFile(numberOfFile1)
                .pressEditSelectedFilesButton()
                .selectEditLabelsPart()
                .selectReplaceLabels()
                .specifyNewLabels(label1)
                .pressSave();
        fileListSteps
                .selectFile(numberOfFile2)
                .pressEditSelectedFilesButton()
                .selectEditLabelsPart()
                .selectReplaceLabels()
                .specifyNewLabels(label2)
                .pressSave();
    }

    @Test(dataProvider = "cancelLabelsEditing", dataProviderClass = FilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void cancelLabelsEditing(int numberOfFile1, int numberOfFile2, String addedLabel) {
        FileListSteps fileListSteps = dashboardPageSteps.getSidebarMenuSteps()
                .selectItemMyFiles();
        String label1 = fileListSteps.getFileLabel(numberOfFile1);
        String label2 = fileListSteps.getFileLabel(numberOfFile2);
        fileListSteps
                .selectFile(numberOfFile1)
                .selectFile(numberOfFile2)
                .pressEditSelectedFilesButton()
                .selectEditLabelsPart()
                .selectAppendExistingLabels()
                .specifyNewLabels(addedLabel)
                .pressCancel();
        assertEquals(fileListSteps.getFileLabel(numberOfFile1), label1,
                "Labels which are displayed for file in the file list do not meet expected result");
        assertEquals(fileListSteps.getFileLabel(numberOfFile2), label2,
                "Labels which are displayed for file in the file list do not meet expected result");
    }

    @Test(dataProvider = "bulkRemovingFiles", dataProviderClass = FilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void bulkRemovingFiles(InstrumentData instrumentData, FileData fileData) {
        dashboardPageSteps
                .createInstrument(instrumentData)
                .uploadFiles(fileData, instrumentData.getName(), 300);
        FileListSteps fileListSteps = dashboardPageSteps.getFileListSteps()
                .selectFile(fileData.getFiles()[0])
                .selectFile(fileData.getFiles()[1])
                .selectFile(fileData.getFiles()[2])
                .pressRemoveSelectedFilesButton()
                .pressRemoveSelectedFilesConfirmationButton()
                .waitForFileDisappearingFromDOM(fileData.getFiles()[0]);
        assertFalse(fileListSteps.isFilePresent(fileData.getFiles()[0]),
                "File is still presented in the file list after bulk removing");
        assertFalse(fileListSteps.isFilePresent(fileData.getFiles()[1]),
                "File is still presented in the file list after bulk removing");
        assertFalse(fileListSteps.isFilePresent(fileData.getFiles()[2]),
                "File is still presented in the file list after bulk removing");
        dashboardPageSteps.getSidebarMenuSteps().selectInstrumentsItem().deleteInstrument(instrumentData.getName());
    }

    @Test(dataProvider = "bulkChangingFilesSpecies", dataProviderClass = FilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void bulkChangingFilesSpecies(InstrumentData instrumentData, FileData fileData, String species) {
        dashboardPageSteps
                .createInstrument(instrumentData)
                .uploadFiles(fileData, instrumentData.getName(), 300);
        //test
        FileListSteps fileListSteps = dashboardPageSteps.getSidebarMenuSteps()
                .selectItemMyFiles()
                .selectFiles(fileData)
                .pressEditSelectedFilesButton()
                .selectEditSpeciesPart()
                .selectSpecies(species)
                .pressSave();
        assertTrue(fileListSteps.isFilesSpeciesCorrect(fileData, species), "Species are incorrect");
        //added to avoid issues with expanded file details
        dashboardPageSteps.getSidebarMenuSteps()
                .selectItemMyFiles();
        //clearing data
        fileListSteps.deleteFiles(fileData);
        dashboardPageSteps.getSidebarMenuSteps()
                .selectInstrumentsItem()
                .deleteInstrument(instrumentData.getName());
    }

    @Test(dataProvider = "changingFileSpecies", dataProviderClass = FilesDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void changingFileSpecies(InstrumentData instrumentData, FileData fileData, String species) {
        //setup
        dashboardPageSteps
                .createInstrument(instrumentData)
                .uploadFiles(fileData, instrumentData.getName(), 300);
        //test
        FileListSteps fileListSteps = dashboardPageSteps.getSidebarMenuSteps()
                .selectItemMyFiles()
                .selectFiles(fileData)
                .pressEditSelectedFilesButton()
                .selectEditSpeciesPart()
                .selectSpecies(species)
                .pressSave();
        assertTrue(fileListSteps.isFilesSpeciesCorrect(fileData, species), "Species are incorrect");
        //added to avoid issues with expanded file details
        dashboardPageSteps.getSidebarMenuSteps()
                .selectItemAllFiles();
        //clearing data
        fileListSteps.deleteFile(fileData.getFiles()[0]);
        dashboardPageSteps.getSidebarMenuSteps()
                .selectInstrumentsItem()
                .deleteInstrument(instrumentData.getName());
    }

}
