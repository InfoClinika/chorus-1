package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.stepdefinitions.experiment.ExperimentGeneralInfoSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.instrument.InstrumentGeneralSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.news.NewsDialogSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.project.ProjectGeneralTabSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.sharinggroup.CreateSharingGroupDialogSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class CreationMenuSteps extends AbstractPageSteps{

    private static final Button PROJECT_MENU_BUTTON = controlFactory().button(By.xpath("//ul[@class='dropdown-menu']//a[text()='Project']"));
    private static final Button EXPERIMENT_MENU_BUTTON = controlFactory().button(By.xpath("//ul[@class='dropdown-menu']//a[text()='Experiment']"));
    private static final Button INSTRUMENT_MENU_BUTTON = controlFactory().button(By.xpath("//ul[@class='dropdown-menu']//a[text()='Instrument']"));
    private static final Button SHARING_GROUP_MENU_BUTTON = controlFactory().button(By.xpath("//ul[@class='dropdown-menu']//a[text()='Sharing Group']"));
    private static final Button NEWS_MENU_BUTTON = controlFactory().button(By.xpath("//ul[@class='dropdown-menu']//a[text()='News']"));

    public ProjectGeneralTabSteps clickCreateProject(){
        PROJECT_MENU_BUTTON.click();
        return new ProjectGeneralTabSteps();
    }

    public InstrumentGeneralSteps clickCreateInstrument(){
        INSTRUMENT_MENU_BUTTON.click();
        return new InstrumentGeneralSteps();
    }

    public CreateSharingGroupDialogSteps clickCreateSharingGroup(){
        SHARING_GROUP_MENU_BUTTON.click();
        return new CreateSharingGroupDialogSteps();
    }

    public ExperimentGeneralInfoSteps clickCreateExperiment(){
        EXPERIMENT_MENU_BUTTON.click();
        return new ExperimentGeneralInfoSteps();
    }

    public NewsDialogSteps clickCreateNews(){
        NEWS_MENU_BUTTON.click();
        return new NewsDialogSteps();
    }
}
