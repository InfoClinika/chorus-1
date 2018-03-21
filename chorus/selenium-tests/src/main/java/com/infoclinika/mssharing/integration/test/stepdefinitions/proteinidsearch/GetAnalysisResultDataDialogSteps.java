package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.DropdownList;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class GetAnalysisResultDataDialogSteps extends AbstractPageSteps{

    private static final DropdownList COMPOSE_BY_DROPDOWN = controlFactory().dropdownList(By.id("s2id_composeTypeSelect"));
    private static final DropdownList VIEW_LEVEL_DROPDOWN = controlFactory().dropdownList(By.id("s2id_viewLevelSelect"));
    private static final Button APPLY_GET_DATA_BUTTON = controlFactory().button(By.cssSelector("[ng-click='applyGetData()']"));

    public GetAnalysisResultDataDialogSteps selectItemInComposeByDropdown(String item){
        COMPOSE_BY_DROPDOWN.select(item);
        return this;
    }

    public GetAnalysisResultDataDialogSteps selectItemInViewLevelDropdown(String item){
        VIEW_LEVEL_DROPDOWN.select(item);
        return this;
    }

    public ProcessingResultsPageSteps pressApply(){
        APPLY_GET_DATA_BUTTON.click();
        return new ProcessingResultsPageSteps();
    }

}
