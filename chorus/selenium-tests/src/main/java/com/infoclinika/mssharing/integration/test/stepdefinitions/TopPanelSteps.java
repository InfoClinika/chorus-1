package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.stepdefinitions.project.ProjectsListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles.ResumeUploadDialogSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles.UploadFilesStep1Steps;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class TopPanelSteps extends AbstractPageSteps {

    private static final Button TOP_PANEL_CREATE_BUTTON = controlFactory().button(By.cssSelector(".create.btn"));
    private static final Button TOP_PANEL_UPLOAD_FILES_BUTTON = controlFactory().button(By.cssSelector(".btn.upload.main-action"));
    private static final Button PROJECTS_SEARCH_TAB = controlFactory().button(By.xpath("//span[@ng-repeat='tab in page.custom.searchTabs']//a[text()='Projects']"));
    private static final Button EXPERIMENTS_SEARCH_TAB = controlFactory().button(By.xpath("//span[@ng-repeat='tab in page.custom.searchTabs']//a[text()='Experiments']"));
    private static final Button FILES_SEARCH_TAB = controlFactory().button(By.xpath("//span[@ng-repeat='tab in page.custom.searchTabs']//a[text()='Files']"));
    private static final Button INSTRUMENTS_SEARCH_TAB = controlFactory().button(By.xpath("//span[@ng-repeat='tab in page.custom.searchTabs']//a[text()='Instruments']"));
    private static final Button RESUME_UPLOAD_BUTTON = controlFactory().button(By.cssSelector(".icon.warning-uploads"));

    public CreationMenuSteps openCreationMenu() {
        TOP_PANEL_CREATE_BUTTON.waitForButtonToBeEnabled();
        TOP_PANEL_CREATE_BUTTON.click();
        return new CreationMenuSteps();
    }


    public UploadFilesStep1Steps pressUploadFilesButton() {
        TOP_PANEL_UPLOAD_FILES_BUTTON.click();
        return new UploadFilesStep1Steps();
    }

    public ProjectsListSteps selectProjectsTab() {
        PROJECTS_SEARCH_TAB.click();
        return new ProjectsListSteps();
    }

    public ResumeUploadDialogSteps pressResumeUploadButton() {
        RESUME_UPLOAD_BUTTON.click();
        return new ResumeUploadDialogSteps();
    }

}
