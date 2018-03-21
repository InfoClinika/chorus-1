package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.DropdownList;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.Sheet;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class AlgorithmSettingsDialogSteps extends AbstractPageSteps {

    private static final DropdownList APPLY_ALGORITHM_TO_SHEET_DROPDOWN = controlFactory().dropdownList(By.id("s2id_algorithmApplyToSheet"));
    private static final Button APPLY_BUTTON = controlFactory().button(By.xpath(".//*[@id='new-filter-dialog']//*[text()='Apply']"));


    public AlgorithmSettingsDialogSteps selectAlgorithmSheet(Sheet sheet){
        APPLY_ALGORITHM_TO_SHEET_DROPDOWN.select(sheet.getValue());
        return this;
    }

    public ProcessingResultsPageSteps pressApplyButton(){
        APPLY_BUTTON.click();
        return new ProcessingResultsPageSteps();
    }


}
