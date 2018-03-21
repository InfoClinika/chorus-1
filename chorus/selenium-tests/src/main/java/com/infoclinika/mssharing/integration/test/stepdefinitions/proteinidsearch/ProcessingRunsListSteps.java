package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.ContextMenu;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.ConfigurationManager.getDriver;

/**
 * @author Alexander Orlov
 */
public class ProcessingRunsListSteps extends AbstractPageSteps{

    //Dynamic locators
    public ContextMenu viewProcessingResultsItemInContextMenu(String searchName){
        return new ContextMenu(new Pane(By.xpath("//*[@id='general']/div[.//span[@title='" + searchName + "']]")), new Button(By.cssSelector("[title='View Processing Results']")));
    }

    public ProcessingResultsPageSteps viewProcessingResults(String searchName){
        viewProcessingResultsItemInContextMenu(searchName).hoverAndClick();
        List<String> tabs2 = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(tabs2.get(1));
        return new ProcessingResultsPageSteps();
    }
}
