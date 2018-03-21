package com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles;

import com.infoclinika.mssharing.integration.test.components.*;
import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import com.infoclinika.mssharing.integration.test.data.file.FileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;

import java.awt.*;
import java.awt.event.KeyEvent;

import static org.testng.Assert.fail;

/**
 * @author Alexander Orlov
 */
public class UploadFilesStep4Steps extends AbstractPageSteps {

    private Log log = LogFactory.getLog(this.getClass());

    //Dynamic locator
    private Label doneLabelForUploadedFile(String fileName) {
        return new Label(By.cssSelector("td[title='" + fileName + "'] + td > .done[ng-show=\"item.status == 'done'\"]"));
    }

    private static final Button OK_BUTTON = controlFactory().button(By.id("okButton"));
    private static final Pane MODAL_DIALOG = controlFactory().pane(By.cssSelector("#uploadDialog > .modal-holder"));

    public UploadFilesStep4Steps waitForFinishUploading(FileData fileData, int secondsToWait) {
        for (String fileName : fileData.getFiles()) {
            log.info("Waiting for file '" + fileName + "' uploading...");
            doneLabelForUploadedFile(fileName).waitForElementToBeVisible(secondsToWait);
            log.info("- - Ok! File '" + fileName + "' has been uploaded.");
        }
        return this;
    }

    public DashboardPageSteps pressOkButton() {
        OK_BUTTON.click();
        MODAL_DIALOG.waitForElementToBeInvisible();
        return new DashboardPageSteps();
    }

    public UploadFilesStep1Steps interruptUploadingByRefreshingThePage() {
        try {
            Thread.sleep(300);
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_R);
            robot.keyRelease(KeyEvent.VK_R);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            Thread.sleep(1000);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
        } catch (Exception e) {
            fail("Exception when attempt to refresh the page using java Robot");
        }
        return new UploadFilesStep1Steps();
    }
}
