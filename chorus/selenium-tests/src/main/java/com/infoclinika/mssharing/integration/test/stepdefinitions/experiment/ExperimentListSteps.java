package com.infoclinika.mssharing.integration.test.stepdefinitions.experiment;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.ContextMenu;
import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch.ProcessingRunsListSteps;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class ExperimentListSteps extends AbstractPageSteps {

    //Dynamic locator
    private Pane experimentInList(String experimentName) {
        return new Pane(By.xpath("//div[@ng-repeat='experiment in experiments'][.//experiment-name[@title='" + experimentName + "']]"));
    }

    private ContextMenu editItemInContextMenu(String experimentName) {
        return new ContextMenu(experimentInList(experimentName), EDIT_ITEM_IN_CONTEXT_MENU);
    }

    private ContextMenu deleteItemInContextMenu(String experimentName) {
        return new ContextMenu(experimentInList(experimentName), DELETE_ITEM_IN_CONTEXT_MENU);
    }

    private ContextMenu processingRunsItemInContextMenu(String experimentName) {
        return new ContextMenu(experimentInList(experimentName), PROCESSING_RUNS_ITEM_IN_CONTEXT_MENU);
    }

    private ContextMenu shareLinkItemInContextMenu(String experimentName) {
        return new ContextMenu(experimentInList(experimentName), SHARE_LINK_ITEM_IN_CONTEXT_MENU);
    }

    private static final Button EDIT_ITEM_IN_CONTEXT_MENU = controlFactory().button(By.cssSelector("[data-target='#experimentDetails']"));
    private static final Button DELETE_ITEM_IN_CONTEXT_MENU = controlFactory().button(By.cssSelector("[title='Delete experiment']"));
    private static final Button PROCESSING_RUNS_ITEM_IN_CONTEXT_MENU = controlFactory().button(By.cssSelector("[title='>Processing Runs']"));
    private static final Button SHARE_LINK_ITEM_IN_CONTEXT_MENU = controlFactory().button(By.cssSelector("[title='Share link']"));
    private static final Button DELETE_CONFIRMATION_BUTTON = controlFactory().button(By.cssSelector("#remove-experiment-confirmation>.modal-holder>.modal-frame>.modal-footer>.main-action"));
    private static final Label SHARE_LINK = controlFactory().label(By.xpath("//*[contains(@id, 'experiment-download-permalink') and @style='display: block;']//input[@type='text']"));
    private static final Button CLOSE_BUTTON = controlFactory().button(By.xpath("//*[contains(@id, 'experiment-download-permalink') and @style='display: block;']//button[contains(text(),'Close')]"));
    private static final Pane SHARE_LINK_DIALOG = controlFactory().pane(By.xpath("//*[contains(@id, 'experiment-download-permalink') and @style='display: block;']"));

    public boolean isExperimentDisplayed(String experimentName) {
        experimentInList(experimentName).waitForAppearing();
        return experimentInList(experimentName).isPresent();
    }

    public boolean isExperimentNotDisplayed(String experimentName) {
        return !experimentInList(experimentName).isPresent();
    }

    public ExperimentGeneralInfoSteps openExperimentDetails(String experimentName) {
        editItemInContextMenu(experimentName).hoverAndClick();
        return new ExperimentGeneralInfoSteps();
    }

    public DashboardPageSteps deleteExperiment(String experimentName) {
        experimentInList(experimentName).waitForElementToBeVisible();
        deleteItemInContextMenu(experimentName).hoverAndClick();
        DELETE_CONFIRMATION_BUTTON.waitForElementToBeClickable();
        DELETE_CONFIRMATION_BUTTON.click();
        experimentInList(experimentName).waitForElementToDisappear();
        return new DashboardPageSteps();
    }

    public ProcessingRunsListSteps viewProcessingRunsList(String experimentName) {
        processingRunsItemInContextMenu(experimentName).hoverAndClick();
        return new ProcessingRunsListSteps();
    }

    public String getExperimentShareLink(String experimentName) {
        experimentInList(experimentName).waitForElementToBeVisible();
        shareLinkItemInContextMenu(experimentName).hoverAndClick();
        String shareLink = SHARE_LINK.getAttribute("value");
        CLOSE_BUTTON.click();
        if (SHARE_LINK_DIALOG.isPresent()) {
            SHARE_LINK_DIALOG.waitForElementToBeInvisible();
        }
        return shareLink;
    }
}
