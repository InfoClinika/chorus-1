package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.DropdownList;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.SortingSettingsData;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class SortingSettingsDialogSteps extends AlgorithmSettingsDialogSteps {

    private static final DropdownList DIMENSION_DROPDOWN = controlFactory().dropdownList(By.xpath("//label[contains(text(), 'Dimension')]/../div"));
    private static final DropdownList SORT_BY_DROPDOWN = controlFactory().dropdownList(By.xpath("//label[contains(text(), 'Sort By')]/../div"));
    private static final DropdownList DIRECTION_DROPDOWN = controlFactory().dropdownList(By.xpath("//label[contains(text(), 'Direction')]/../div"));

    public SortingSettingsDialogSteps setSortingSettings(SortingSettingsData sortingSettingsData){
        DIMENSION_DROPDOWN.select(sortingSettingsData.getDimension().getValue());
        SORT_BY_DROPDOWN.select(sortingSettingsData.getSortBy().getValue());
        DIRECTION_DROPDOWN.select(sortingSettingsData.getDirection().getValue());
        return this;
    }
}
