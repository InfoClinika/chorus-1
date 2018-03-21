package com.infoclinika.mssharing.integration.test.stepdefinitions.filelist;

import com.infoclinika.mssharing.integration.test.components.*;
import com.infoclinika.mssharing.integration.test.data.file.FileArchivingStatus;
import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.data.file.FileTranslationStatus;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.experiment.ExperimentGeneralInfoSteps;
import org.openqa.selenium.By;


import static org.testng.FileAssert.fail;

/**
 * @author Alexander Orlov
 */
public class FileListSteps extends AbstractPageSteps {

    //Dynamic locator
    private Pane fileInList(String fileName) {
        return new Pane(By.xpath("//div[@ng-repeat='file in (selectedFiles = files)'][.//span[@title='" + fileName + "']]"));
    }

    private Checkbox checkbox(String fileName) {
        return new Checkbox(By.xpath("//div[@ng-dblclick='toggleExpandMenu(file);'][.//span[text()='" + fileName + "']]//input[@type='checkbox']"));
    }

    private Checkbox checkboxByIndex(int index) {
        return new Checkbox(By.xpath("//*[@ng-style='viewStyleContent']/div[" + index + "]//*[@type='checkbox']"));
    }

    private Label labelForFile(String fileName) {
        return new Label(By.xpath("//div[@ng-dblclick='toggleExpandMenu(file);'][.//span[text()='" + fileName + "']]//div[contains(@class, 'labels_gen')]//span"));
    }

    private Label labelForFileByIndex(int index) {
        return new Label(By.xpath("//*[@ng-style='viewStyleContent']/div[" + index + "]//*[contains(@class, 'labels')]//span"));
    }

    private Button fileTitle(String fileName) {
        return new Button(By.xpath("//div[@ng-dblclick='toggleExpandMenu(file);']//span[@title='" + fileName + "']"));
    }

    private Button deleteFileButton(String fileName) {
        return new Button(By.xpath("//div[@ng-dblclick='toggleExpandMenu(file);'][.//span[text()='" + fileName + "']]//a[@title='Delete File']"));
    }

    private Button downloadIcon(String fileName) {
        return new Button(By.xpath("//div[@ng-dblclick='toggleExpandMenu(file);'][.//span[text()='" + fileName + "']]//i[contains(@class, 'Download')]"));
    }

    private Button translationIcon(String fileName) {
        return new Button(By.xpath("//div[@ng-dblclick='toggleExpandMenu(file);'][.//span[text()='" + fileName + "']]//i[contains(@class, 'translation-status')]"));
    }

    private static final Button DELETE_CONFIRMATION_BUTTON = controlFactory().button(By.cssSelector("#remove-file-confirmation .btn.btn-primary.main-action"));
    private static final Label SELECTED_FILES_COUNTER = controlFactory().label(By.cssSelector(".items-number > [count='operationButtonFactory.count']"));
    private static final Button EDIT_SELECTED_FILES_BUTTON = controlFactory().button(By.cssSelector("[title='Edit selected files']"));
    private static final Button REMOVE_SELECTED_FILES_BUTTON = controlFactory().button(By.cssSelector("[title='Remove selected files']"));
    private static final Button REMOVE_SELECTED_FILES_CONFIRMATION_BUTTON = controlFactory().button(By.cssSelector("[ng-click='removesSelectedPopup.ok()']"));
    private static final Button RUN_NEW_EXPERIMENT_WITH_THE_SELECTED_FILES_BUTTON = controlFactory().button(By.cssSelector("[title='Run new experiment with selected files']"));
    private static final Label EXPANDED_FILE_SPECIES_LABEL = controlFactory().label(By.xpath("//div[contains(@class, 'opened')]//p[@ng-bind='file.details.specieName']"));
    private static final Button ARCHIVE_ITEM_IN_CONTEXT_MENU = controlFactory().button(By.cssSelector("[title='Archive file']"));
    private static final Button ARCHIVE_CONFIRMATION_BUTTON = controlFactory().button(By.xpath("//div[@id='archive-file-confirmation']//button[contains(@class, 'main-action')]"));
    private static final Button UNARCHIVE_ITEM_IN_CONTEXT_MENU = controlFactory().button(By.xpath("//a[contains(text(), 'Unarchive')]"));
    private static final Button UNARCHIVE_CONFIRMATION_BUTTON = controlFactory().button(By.xpath("//div[@id='unarchive-file-confirmation']//button[contains(@class, 'main-action')]"));
    private static final InputBox FILTER_INPUT_BOX = controlFactory().inputBox(By.id("filterList"));

    private ContextMenu removeFileItemInContextMenu(String fileName) {
        return new ContextMenu(fileInList(fileName), deleteFileButton(fileName));
    }

    private ContextMenu archiveItemInContextMenu(String fileName) {
        return new ContextMenu(fileInList(fileName), ARCHIVE_ITEM_IN_CONTEXT_MENU);
    }

    private ContextMenu unarchiveItemInContextMenu(String fileName) {
        return new ContextMenu(fileInList(fileName), UNARCHIVE_ITEM_IN_CONTEXT_MENU);
    }

    public boolean isFilePresent(String fileName) {
        return fileInList(fileName).isPresent();
    }

    public FileListSteps deleteFile(String fileName) {
        removeFileItemInContextMenu(fileName).hoverAndClick();
        DELETE_CONFIRMATION_BUTTON.waitForElementToBeClickable();
        DELETE_CONFIRMATION_BUTTON.click();
        fileInList(fileName).waitForElementToDisappear();
        return this;
    }

    public FileListSteps deleteFiles(FileData fileData) {
        for (int i = 0; i < fileData.getFiles().length; i++) {
            deleteFile(fileData.getFiles()[i]);
        }
        return this;
    }

    public FileListSteps selectFile(String fileName) {
        checkbox(fileName).scrollToElement();
        checkbox(fileName).click();
        return this;
    }

    public FileListSteps selectFiles(FileData fileData) {
        for (int i = 0; i < fileData.getFiles().length; i++) {
            checkbox(fileData.getFiles()[i]).click();
        }
        return this;
    }

    public FileListSteps selectFiles(int numberOfFiles) {
        for (int i = 1; i <= numberOfFiles; i++) {
            checkboxByIndex(i).setValue(true);
        }
        return this;
    }

    public FileListSteps selectFile(int numberOfFileToSelect) {
        checkboxByIndex(numberOfFileToSelect).click();
        return this;
    }

    public FileListSteps archiveFile(String fileName) {
        archiveItemInContextMenu(fileName).hoverAndClick();
        ARCHIVE_CONFIRMATION_BUTTON.waitForElementToBeClickable();
        ARCHIVE_CONFIRMATION_BUTTON.click();
        return this;
    }

    public FileListSteps unarchiveFile(String fileName) {
        unarchiveItemInContextMenu(fileName).hoverAndClick();
        UNARCHIVE_CONFIRMATION_BUTTON.waitForElementToBeClickable();
        UNARCHIVE_CONFIRMATION_BUTTON.click();
        return this;
    }

    public boolean isFileArchived(String fileName){
        return isFileHasArchivingStatusEquals(fileName, FileArchivingStatus.ARCHIVED);
    }

    public boolean isFileUnarchivingProgressStarted(String fileName){
        return isFileHasArchivingStatusEquals(fileName, FileArchivingStatus.IN_PROGRESS_OF_UNARCHIVING);
    }

    public boolean isFileUnarchived(String fileName){
        return isFileHasArchivingStatusEquals(fileName, FileArchivingStatus.UNARCHIVED);
    }

    public boolean isFileInNotTranslatedStatus (String fileName){
        return isFileTranslationStatusEquals(fileName, FileTranslationStatus.NOT_TRANSLATED);
    }

    public boolean isFileInProgressOfTranslation(String fileName){
        return isFileTranslationStatusEquals(fileName, FileTranslationStatus.TRANSLATION_IN_PROGRESS);
    }

    public String getSelectedFilesCounterValue() {
        wait(1); //this wait is added, because counter does not updated immediately on slow internet connection
        return SELECTED_FILES_COUNTER.getText();
    }

    public BulkEditLabelsDialogSteps pressEditSelectedFilesButton() {
        EDIT_SELECTED_FILES_BUTTON.click();
        return new BulkEditLabelsDialogSteps();
    }

    public String getFileLabel(String fileName) {
        wait(1);
        return labelForFile(fileName).getText();
    }

    public String getFileLabel(int fileNumberInList){
        wait(1);
        return labelForFileByIndex(fileNumberInList).getText();
    }

    public FileListSteps pressRemoveSelectedFilesButton() {
        REMOVE_SELECTED_FILES_BUTTON.click();
        return this;
    }

    public FileListSteps pressRemoveSelectedFilesConfirmationButton() {
        REMOVE_SELECTED_FILES_CONFIRMATION_BUTTON.click();
        return this;
    }

    public FileListSteps waitForFileDisappearingFromDOM(String fileName) {
        fileInList(fileName).waitForElementToDisappear();
        return this;
    }

    public ExperimentGeneralInfoSteps pressRunExperimentForSelectedFilesButton() {
        RUN_NEW_EXPERIMENT_WITH_THE_SELECTED_FILES_BUTTON.click();
        return new ExperimentGeneralInfoSteps();
    }

    public boolean isFilesSpeciesCorrect(FileData fileData, String species) {
        for (int i = 0; i < fileData.getFiles().length; i++) {
            expandFileDetails(fileData.getFiles()[i]);
            if (!getFileSpecies().equals(species)) {
                fail("Species for " + fileData.getFiles()[i] + " is incorrect - " + getFileSpecies() + " instead of " + species);
            }
        }
        return true;
    }

    public void expandFileDetails(String fileName) {
        fileTitle(fileName).doubleClick();
        wait(1);
    }

    private String getFileSpecies() {
        return EXPANDED_FILE_SPECIES_LABEL.getText();
    }

    private boolean isFileHasArchivingStatusEquals(String fileName, FileArchivingStatus archivingStatus){
        return downloadIcon(fileName).waitUntilAttributeWillEqualsValue("title", archivingStatus.getName());
    }

    private boolean isFileTranslationStatusEquals(String fileName, FileTranslationStatus translationStatus) {
        return translationIcon(fileName).waitUntilAttributeWillEqualsValue("title", translationStatus.getTitle().trim());
    }
}
