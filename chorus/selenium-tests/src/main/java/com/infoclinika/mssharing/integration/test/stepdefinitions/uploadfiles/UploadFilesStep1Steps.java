package com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.Checkbox;
import com.infoclinika.mssharing.integration.test.components.DropdownList;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class UploadFilesStep1Steps extends AbstractPageSteps{

    private static final DropdownList INSTRUMENT_DROPDOWN = controlFactory().dropdownList(By.id("s2id_instrument"));
    private static final DropdownList SPECIES_DROPDOWN = controlFactory().dropdownList(By.id("s2id_specie"));
    private static final Button NEXT_BUTTON = controlFactory().button(By.cssSelector(".btn.main-action.next"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector("[ng-click='onCancel()']"));
    private static final Pane MODAL_DIALOG = controlFactory().pane(By.cssSelector("#uploadDialog > .modal-holder"));
    private static final Checkbox AUTO_TRANSLATE_CHECKBOX = controlFactory().checkbox(By.id("autotranslate"));

    public UploadFilesStep1Steps selectInstrument(String instrument){
        INSTRUMENT_DROPDOWN.select(instrument);
        return this;
    }

    public UploadFilesStep1Steps selectSpecies(String species){
        SPECIES_DROPDOWN.select(species);
        return this;
    }

    public UploadFilesStep2Steps pressNextButton(){
        NEXT_BUTTON.click();
        return new UploadFilesStep2Steps();
    }

    public DashboardPageSteps pressCancelButton(){
        CANCEL_BUTTON.click();
        MODAL_DIALOG.waitForElementToBeInvisible();
        return new DashboardPageSteps();
    }

    public UploadFilesStep1Steps selectAutoTranslateCheckbox() {
        AUTO_TRANSLATE_CHECKBOX.click();
        return this;
    }
}
