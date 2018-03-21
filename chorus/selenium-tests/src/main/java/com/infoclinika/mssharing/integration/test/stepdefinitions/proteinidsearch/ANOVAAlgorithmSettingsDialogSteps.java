package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Checkbox;
import com.infoclinika.mssharing.integration.test.components.DropdownList;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.GroupColumnName;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class ANOVAAlgorithmSettingsDialogSteps extends AlgorithmSettingsDialogSteps{

    private static final Checkbox RUN_FDR_CHECKBOX = controlFactory().checkbox(By.id("anova-fdr"));
    private static final DropdownList GROUP_COLUMN_NAME = controlFactory().dropdownList(By.id("group-column-name"));

    public ANOVAAlgorithmSettingsDialogSteps setRunFDRCheckbox(boolean value){
        RUN_FDR_CHECKBOX.setValue(value);
        return this;
    }

    public ANOVAAlgorithmSettingsDialogSteps selectGroupColumnName(GroupColumnName groupColumnName){
        GROUP_COLUMN_NAME.select(groupColumnName.getValue());
        return this;
    }


}
