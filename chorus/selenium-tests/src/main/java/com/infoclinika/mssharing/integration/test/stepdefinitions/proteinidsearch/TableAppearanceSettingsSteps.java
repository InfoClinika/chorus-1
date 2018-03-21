package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Checkbox;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class TableAppearanceSettingsSteps extends AbstractPageSteps{

    private static final Button COLUMNS_TAB = controlFactory().button(By.cssSelector("[data-target='#columnsTabContent']"));
    private static final Button APPLY_BUTTON = controlFactory().button(By.cssSelector("[ng-click=\"applyAppearanceSettings()\"]"));
    private static final Checkbox ALL_CHECKBOXES_IN_COLUMNS_TAB = controlFactory().checkbox(By.xpath("//*[@id='columnsTabContent']//div//input"));

    private Checkbox checkboxInColumnsTab(int index) {
        return new Checkbox(By.xpath("//*[@id='columnsTabContent']//div[" + index + "]//input"));
    }


    public TableAppearanceSettingsSteps selectColumnsTab() {
        COLUMNS_TAB.click();
        checkboxInColumnsTab(1).waitForElementToBeClickable();
        return this;
    }

    public TableAppearanceSettingsSteps checkCheckbox(int number) {
        checkboxInColumnsTab(number).check();
        return this;
    }

    public ProcessingResultsPageSteps pressApply() {
        APPLY_BUTTON.click();
        return new ProcessingResultsPageSteps();
    }

    public ProcessingResultsPageSteps selectAllCheckboxesAndApply() {
        selectColumnsTab();
        for (int i = 0; i < ALL_CHECKBOXES_IN_COLUMNS_TAB.getNumberOfAll(); i++) {
            checkCheckbox(i + 1);
        }
        pressApply();
        return new ProcessingResultsPageSteps();
    }
}
