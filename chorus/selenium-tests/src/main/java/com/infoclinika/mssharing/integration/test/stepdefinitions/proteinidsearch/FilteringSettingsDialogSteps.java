package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.DropdownList;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.FilterSettingsData;
import org.openqa.selenium.By;

import java.util.List;

/**
 * @author Alexander Orlov
 */
public class FilteringSettingsDialogSteps extends AlgorithmSettingsDialogSteps {

    private static final DropdownList FILTER_BY_DROPDOWN = controlFactory().dropdownList(By.xpath("//label[contains(text(), 'Filter By')]/../div"));

    private DropdownList filterDropdown(int row) {
        return new DropdownList(By.xpath("//*[@class='clearfix with-labels ng-scope'][" + row + "]//label[contains(text(), 'Filter')]/../div"));
    }

    private DropdownList matchIfDropdown(int row) {
        return new DropdownList(By.xpath("//*[@class='clearfix with-labels ng-scope'][" + row + "]//label[contains(text(), 'Match if')]/../div"));
    }

    private InputBox valueInputBox(int row) {
        return new InputBox(By.xpath("//*[@class='clearfix with-labels ng-scope'][" + row + "]//label[contains(text(), 'Value')]/../input"));
    }

    private Button plusButton(int row) {
        return new Button(By.xpath("//*[@class='clearfix with-labels ng-scope'][" + row + "]//*[contains(text(), '+')]"));
    }

    private DropdownList andOrDropdown(int row) {
        return new DropdownList(By.xpath("//*[@class='clearfix with-labels ng-scope'][" + row + "]//*[@title='And/Or']/../div"));
    }

    public FilteringSettingsDialogSteps setFilterSettings(List<FilterSettingsData> filterSettingsData) {
        for (int i = 1; i <= filterSettingsData.size(); i++) {
            FilterSettingsData filterData = filterSettingsData.get(i - 1);
            FILTER_BY_DROPDOWN.select(filterData.getFilterBy().getValue());
            filterDropdown(i).select(filterData.getFilter().getValue());
            matchIfDropdown(i).select(filterData.getMatchIf().getValue());
            valueInputBox(i).clearAndFill(filterData.getValue());
            if (filterData.getAndOr() != null) {
                andOrDropdown(i).select(filterData.getAndOr().getValue());
            }
            if (filterData.isAddOneMoreFilter()) {
                plusButton(i).click();
            }
        }
        return this;
    }


}
