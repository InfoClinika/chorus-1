package com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Image;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.data.file.FileData;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import com.infoclinika.mssharing.integration.test.utils.ConfigurationManager;
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
public class ResumeUploadDialogSteps extends AbstractPageSteps {

    private Log log = LogFactory.getLog(this.getClass());

    //Dynamic locator
    private Pane fileInList(String fileName) {
        return new Pane(By.cssSelector("[ng-repeat='item in uploadItems'] > [title='" + fileName + "']"));
    }

    private Button resumeButtonForCertainFile(String fileName) {
        return new Button(By.cssSelector("[title='" + fileName + "'] + td .refresh-location-wrapper"));
    }

    private Image doneIconForCertainFile(String fileName) {
        return new Image(By.cssSelector("[title='" + fileName + "'] + td + td + td >.icon.upload-done"));
    }

    private final static Button RESUME_BUTTON = controlFactory().button(By.cssSelector("[ng-click='resume()']"));
    private final static Button CLOSE_DIALOG_BUTTON = controlFactory().button(By.cssSelector(".close"));

    public boolean isFilePresent(String fileName) {
        return fileInList(fileName).isPresent();
    }

    public ResumeUploadDialogSteps pressResumeUploadButtonForFile(String fileName) {
        resumeButtonForCertainFile(fileName).click();
        return this;
    }

    public ResumeUploadDialogSteps selectAndAddFile(FileData files) {
        if (ConfigurationManager.OS.contains("win")) {
            final List<String> args = new ArrayList(Arrays.asList(UploadFilesStep2Steps.class.getResource("/autoitscript/upload_file_using_browse_button_v5.exe").getFile(), "Open", files.getFolder().getAbsolutePath()));
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
            log.info("Current OS does not support .exe files, so method 'UploadFilesStep2Steps.selectAndAddFiles' is skipped");
        }
        return this;
    }

    public ResumeUploadDialogSteps pressResumeButton() {
        RESUME_BUTTON.click();
        return this;
    }

    public DashboardPageSteps closeDialog() {
        CLOSE_DIALOG_BUTTON.click();
        return new DashboardPageSteps();
    }

    public ResumeUploadDialogSteps waitForFinishUploading(String fileName, int secondsToWait) {
        log.info("Waiting for file '" + fileName + "' uploading...");
        doneIconForCertainFile(fileName).waitForElementToBeVisible(secondsToWait);
        log.info("- - Ok! File '" + fileName + "' has been uploaded.");
        return this;
    }
}
