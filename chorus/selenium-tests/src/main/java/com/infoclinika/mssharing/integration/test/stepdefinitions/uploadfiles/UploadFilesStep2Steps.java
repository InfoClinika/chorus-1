package com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import com.infoclinika.mssharing.integration.test.utils.ConfigurationManager;
import com.infoclinika.mssharing.integration.test.data.file.FileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class UploadFilesStep2Steps extends AbstractPageSteps{

    private Log log = LogFactory.getLog(this.getClass());

    //Dynamic locator
    private static Pane fileInList(String fileName) {
        return new Pane(By.xpath("//tr[.//td[text()='" + fileName + "']]"));
    }

    private static final Button NEXT_BUTTON = controlFactory().button(By.cssSelector(".btn.main-action.next"));
    private static final Button BROWSE_FILES_BUTTON = controlFactory().button(By.cssSelector(".select-file-btn"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("[ng-click='onCancel()']"));
    private static final Pane MODAL_DIALOG = controlFactory().pane(By.cssSelector("#uploadDialog > .modal-holder"));

    public UploadFilesStep2Steps pressBrowseButton() {
        BROWSE_FILES_BUTTON.click();
        wait(1);
        return this;
    }

    public UploadFilesStep2Steps selectAndAddFiles(FileData files) {
        if (ConfigurationManager.OS.contains("win")) {
            List<String> args = new ArrayList(Arrays.asList(UploadFilesStep2Steps.class.getResource("/autoitscript/upload_file_using_browse_button_v5.exe").getFile(), "Open", files.getFolder().getAbsolutePath()));
            log.info(files.getFolder().getAbsolutePath());
            args.addAll(Arrays.asList(files.getFiles()));
            final String[] stringArgs = args.toArray(new String[args.size()]);
            log.info("Attempt to execute file '/autoitscript/upload_file_using_browse_button_v5.exe'");
            try {
                Runtime.getRuntime().exec(stringArgs);
            } catch (IOException e) {
                log.error("Fail! Error occurred while attempt to execute file '/autoitscript/upload_file_using_browse_button_v5.exe'");
                e.printStackTrace();
            }
            log.info("- - Ok! File executed successfully");
        } else {
            log.info("Current OS does not support .exe files, so method 'UploadFilesStep2Steps.selectAndAddFiles()' is skipped");
        }
        return this;
    }

    //Up to 12 files could be drag'n'dropped
    public UploadFilesStep2Steps dragNDropFiles(FileData fileData) {
        if (ConfigurationManager.OS.contains("win")) {
            String tempFolder = fileData.getFolder().getAbsolutePath();
            int numberOfFilesToDragNDrop = fileData.getFiles().length;


            String[] newFileWindowParams = new String[]{UploadFilesStep2Steps.class.getResource("/autoitscript/create_window_by_path.exe").getFile(), tempFolder};
            String[] dragNDropParams = new String[]{UploadFilesStep2Steps.class.getResource("/autoitscript/select_files_and_dragndrop_v2.exe").getFile(), Integer.toString(numberOfFilesToDragNDrop)};

            log.info("Attempt to execute .exe for drag'n'drop files");
            try {
                Runtime.getRuntime().exec(newFileWindowParams);
                Runtime.getRuntime().exec(dragNDropParams);
            } catch (IOException e) {
                log.error("Fail! Error occurred while attempt to execute .exe for drag'n'drop files");
                e.printStackTrace();
            }
            log.info("- - Ok! File executed successfully");
        } else {
            log.info("Current OS does not support .exe files, so method 'UploadFilesStep2Steps.dragNDropFiles' is skipped");
        }
        return this;
    }

    public UploadFilesStep3Steps pressNextButton() {
        NEXT_BUTTON.waitForButtonToBeEnabled();
        NEXT_BUTTON.click();
        return new UploadFilesStep3Steps();
    }

    public boolean isFilePresent(String fileName) {
        fileInList(fileName).waitForAppearing();
        return fileInList(fileName).isPresent();
    }

    public boolean isNextButtonEnabled(){
        return NEXT_BUTTON.isEnabled();
    }

    public DashboardPageSteps pressCancelButton(){
        CANCEL_BUTTON.click();
        MODAL_DIALOG.waitForElementToBeInvisible();
        return new DashboardPageSteps();
    }

}
