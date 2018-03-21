package com.infoclinika.mssharing.integration.test.stepdefinitions.instrument;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.components.Pane;
import com.infoclinika.mssharing.integration.test.data.experiment.LockMz;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.fail;

/**
 * @author Sergii Moroz
 */
public class InstrumentDetailsSteps extends AbstractPageSteps {

    private static final Button SAVE_BUTTON = controlFactory().button(By.xpath("//*[contains(@class, 'instrument-details')]//button[contains(text(), 'Save')]"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector(".create-instrument .btn[data-dismiss=\"modal\"]"));
    private static final Pane MODAL_DIALOG_FORM = controlFactory().pane(By.name("form"));
    private static final InputBox NAME_FIELD = controlFactory().inputBox(By.id("name"));
    private static final InputBox SERIAL_NUMBER_FIELD = controlFactory().inputBox(By.id("serialNumber"));
    private static final InputBox HPLC_FIELD = controlFactory().inputBox(By.id("hplc"));
    private static final Label ACCESS_OPERATOR_LABEL = controlFactory().label(By.cssSelector("[ng-show=\"instrument.access == 'OPERATOR'\"]"));
    private static final InputBox PERIPHERALS_FIELD = controlFactory().inputBox(By.cssSelector("[ng-model='instrument.peripherals']"));
    private static final Label INCORRECT_SERIAL_ERROR_LABEL = controlFactory().label(By.xpath("//span[@ng-show='form.serialNumber.$valid']"));
    private static final Label INCORRECT_NAME_ERROR_LABEL = controlFactory().label(By.xpath("//span[@ng-show='form.name.$valid']"));
    private Label ALL_LOCK_MASSES_IN_LIST = controlFactory().label(By.xpath("//div[@class='lock-masses-selector']//tr"));
    private static final Pane OPTIONALS_SECTION = controlFactory().pane(By.cssSelector(".optionals"));
    private static final Button OPTIONAL_FIELDS_BUTTON = controlFactory().button(By.cssSelector(".optional-fields"));

    private Label lockMassValueInList(int index) {
        return new Label(By.xpath("//tr[" + index + "]//span[@ng-bind='item.lockMass']"));
    }

    private Label chargeValueInList(int index) {
        return new Label(By.xpath("//tr[" + index + "]//span[contains(@ng-bind, 'item.charge')]"));
    }

    public InstrumentDetailsSteps() {
        waitForModalDialogAppearing();
    }

    private InstrumentDetailsSteps waitForModalDialogAppearing() {
        MODAL_DIALOG_FORM.waitForElementToBeVisible();
        NAME_FIELD.waitForElementToBeClickable();
        return this;
    }

    public String getName() {
        return NAME_FIELD.getValue();
    }

    public String getHplc() {
        return HPLC_FIELD.getValue();
    }

    public String getPeripherals() {
        return PERIPHERALS_FIELD.getValue();
    }

    public String getSerial() {
        return SERIAL_NUMBER_FIELD.getValue();
    }

    public String getOperatorAccess() {
        return ACCESS_OPERATOR_LABEL.getText();
    }

    public InstrumentDetailsSteps changeName(String name) {
        NAME_FIELD.fillIn(name);
        return this;
    }

    public InstrumentDetailsSteps changeSerial(String serial) {
        SERIAL_NUMBER_FIELD.fillIn(serial);
        return this;
    }

    public InstrumentDetailsSteps changeHlpc(String hlpc) {
        HPLC_FIELD.scrollToElement();
        HPLC_FIELD.fillIn(hlpc);
        return this;
    }

    public InstrumentDetailsSteps changePeripherals(String peripherals) {
        PERIPHERALS_FIELD.fillIn(peripherals);
        return this;
    }

    public DashboardPageSteps pressSave() {
        SAVE_BUTTON.click();
        return new DashboardPageSteps();
    }

    public InstrumentDetailsSteps pressSaveForIncorrectForm() {
        SAVE_BUTTON.click();
        return this;
    }

    public InstrumentListSteps pressSaveAndWait() {
        SAVE_BUTTON.clickUntilElementWillDisappear(MODAL_DIALOG_FORM);
        return new InstrumentListSteps();
    }

    public InstrumentListSteps pressCancel() {
        CANCEL_BUTTON.click();
        return new InstrumentListSteps();
    }

    public InstrumentDetailsSteps clearName() {
        NAME_FIELD.clear();
        return this;
    }

    public InstrumentDetailsSteps clearSerial() {
        SERIAL_NUMBER_FIELD.clear();
        return this;
    }

    public boolean isCreationEnabled() {
        return SAVE_BUTTON.isEnabled();
    }

    public String getIncorrectNameError() {
        INCORRECT_NAME_ERROR_LABEL.waitForAppearing();
        return INCORRECT_NAME_ERROR_LABEL.getText();
    }

    public InstrumentDetailsSteps expandOptionalFields() {
        OPTIONAL_FIELDS_BUTTON.clickUntilAttributeWillHaveExactValue(OPTIONALS_SECTION, "style", "");
        return this;
    }

    public List<LockMz> readAllLockMasses() {
        expandOptionalFields();
        int itemsNumber = countNumberOfAllLockMasses();
        List<LockMz> result = new LinkedList<>();
        for (int i = 1; i <= itemsNumber; i++) {
            String lockMassValue = lockMassValueInList(i).getText();
            String lockMassCharge = chargeValueInList(i).getText().replace("(", "").replace(")", "");
            result.add(new LockMz.Builder()
                    .lockMass(lockMassValue)
                    .charge(lockMassCharge).build());
        }
        return result;
    }

    private int countNumberOfAllLockMasses() {
        return ALL_LOCK_MASSES_IN_LIST.getAllElements().size();
    }
}
