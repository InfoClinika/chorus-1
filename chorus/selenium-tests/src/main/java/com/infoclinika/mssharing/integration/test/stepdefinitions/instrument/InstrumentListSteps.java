package com.infoclinika.mssharing.integration.test.stepdefinitions.instrument;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.ContextMenu;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class InstrumentListSteps extends AbstractPageSteps {

    private Pane instrumentInList(String instrumentName) {
        return new Pane(By.xpath("//div[@class='row clearfix ng-scope'][.//div[@title='" + instrumentName + "']]"));
    }

    private Button requestAccessButton(String instrumentName) {
        return new Button(By.xpath("//div[@class='row clearfix ng-scope'][.//div[@title='" + instrumentName
                + "']]//a[text()='Request']"));
    }

    public ContextMenu deleteItemInContextMenu(String instrumentName) {
        return new ContextMenu(instrumentInList(instrumentName), INSTRUMENT_LIST_DELETE_BUTTON);
    }

    public ContextMenu editItemInContextMenu(String instrumentName) {
        return new ContextMenu(instrumentInList(instrumentName), INSTRUMENT_LIST_EDIT_BUTTON);
    }

    private static final Button INSTRUMENT_DELETE_CONFIRMATION_BUTTON = controlFactory().button(By.cssSelector("#remove-instrument-confirmation>.modal-holder>.modal-frame>.modal-footer>.main-action"));
    private static final Button INSTRUMENT_LIST_DELETE_BUTTON = controlFactory().button(By.cssSelector("[title='Remove Instrument']"));
    private static final Button INSTRUMENT_LIST_EDIT_BUTTON = controlFactory().button(By.cssSelector("[data-target='#instrumentDetails']"));

    public boolean isInstrumentDisplayed(String instrumentName) {
        return instrumentInList(instrumentName).isPresent();
    }

    public InstrumentDetailsSteps openInstrumentDetails(String instrumentName) {
        editItemInContextMenu(instrumentName).hoverAndClick();
        return new InstrumentDetailsSteps();
    }

    public InstrumentListSteps deleteInstrument(String instrumentName) {
        deleteItemInContextMenu(instrumentName).hoverAndClick();
        INSTRUMENT_DELETE_CONFIRMATION_BUTTON.waitForElementToBeClickable();
        INSTRUMENT_DELETE_CONFIRMATION_BUTTON.click();
        instrumentInList(instrumentName).waitForElementToDisappear();
        return this;
    }

    public InstrumentListSteps requestAccessToInstrument(InstrumentData instrumentData) {
        requestAccessButton(instrumentData.getName()).click();
        return this;
    }

}
