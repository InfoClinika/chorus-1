package com.infoclinika.mssharing.integration.test.stepdefinitions.instrument;

import com.infoclinika.mssharing.integration.test.components.*;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.data.instrument.OptionalFields;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.DashboardPageSteps;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class InstrumentGeneralSteps extends AbstractPageSteps {

    private static final InputBox NAME_FIELD = controlFactory().inputBox(By.cssSelector("[ng-model='instrument.details.name']"));
    private static final DropdownList VENDOR_DROPDOWN = controlFactory().dropdownList(By.id("s2id_vendor"));
    private static final DropdownList MODEL_DROPDOWN = controlFactory().dropdownList(By.id("s2id_model"));
    private static final DropdownList LABORATORY_DROPDOWN = controlFactory().dropdownList(By.id("s2id_lab"));
    private static final InputBox SERIAL_NUMBER_FIELD = controlFactory().inputBox(By.cssSelector("[ng-model='instrument.details.serialNumber']"));
    private static final InputBox HPLC_FIELD = controlFactory().inputBox(By.cssSelector("[ng-model='instrument.details.hplc']"));
    private static final InputBox PERIPHERALS_FIELD = controlFactory().inputBox(By.cssSelector("[ng-model='instrument.details.peripherals']"));
    private static final Button CANCEL_BUTTON = controlFactory().button(By.cssSelector(".create-instrument .btn[data-dismiss=\"modal\"]"));
    private static final Button CREATE_BUTTON = controlFactory().button(By.cssSelector(".ng-dirty>.modal-footer>.main-action"));
    private static final Label INCORRECT_SERIAL_ERROR_LABEL = controlFactory().label(By.xpath("//span[@ng-show='form.serialNumber.$valid']"));
    private static final Label INCORRECT_NAME_ERROR_LABEL = controlFactory().label(By.xpath("//span[@ng-show='form.name.$valid']"));
    private static final Pane OPERATORS_TAB = controlFactory().pane(By.cssSelector("[href='#operators']"));
    private static final Pane MODAL_DIALOG = controlFactory().pane(By.cssSelector(".create-instrument > .modal-holder"));
    private static final Button OPTIONAL_FIELDS_BUTTON = controlFactory().button(By.cssSelector(".optional-fields"));
    private static final Checkbox FIRST_DEFAULT_LOCK_MASS_VALUE_CHECKBOX = controlFactory().checkbox(By.xpath("//div[@class='lock-masses-group']/div[1]/input"));
    private static final Checkbox SECOND_DEFAULT_LOCK_MASS_VALUE_CHECKBOX = controlFactory().checkbox(By.xpath("//div[@class='lock-masses-group']/div[2]/input"));
    private static final InputBox LOCK_MASS_FIELD = controlFactory().inputBox(By.id("lockMassInput"));
    private static final DropdownList LOCK_MASS_CHARGE_DROPDOWN = controlFactory().dropdownList(By.id("s2id_autogen2"));
    private static final Button ADD_LOCK_MASS_BUTTON = controlFactory().button(By.cssSelector(".add-lock-mass-holder > button"));
    private static final Checkbox AUTO_TRANSLATE_CHECKBOX = controlFactory().checkbox(By.id("autoTranslate"));
    private static final Pane OPTIONALS_SECTION = controlFactory().pane(By.cssSelector(".optionals"));

    public InstrumentGeneralSteps specifyName(String name) {
        NAME_FIELD.fillIn(name);
        return this;
    }

    public InstrumentGeneralSteps selectVendor(String vendor) {
        VENDOR_DROPDOWN.select(vendor);
        return this;
    }

    public InstrumentGeneralSteps selectModel(String model) {
        MODEL_DROPDOWN.select(model);
        return this;
    }

    public InstrumentGeneralSteps selectLab(String labName) {
        LABORATORY_DROPDOWN.select(labName);
        return this;
    }

    public InstrumentGeneralSteps specifySerialNumber(String serialNumber) {
        SERIAL_NUMBER_FIELD.fillIn(serialNumber);
        return this;
    }

    public InstrumentGeneralSteps specifyHLPC(String hLPC) {
        HPLC_FIELD.fillIn(hLPC);
        return this;
    }

    public InstrumentGeneralSteps specifyPeripherals(String peripherals) {
        PERIPHERALS_FIELD.fillIn(peripherals);
        return this;
    }

    public DashboardPageSteps pressCreateAndWait() {
        CREATE_BUTTON.click();
        MODAL_DIALOG.waitForElementToBeInvisible();
        return new DashboardPageSteps();
    }

    public InstrumentGeneralSteps pressCreateForIncorrectForm() {
        CREATE_BUTTON.click();
        return this;
    }

    public DashboardPageSteps pressCancel() {
        CANCEL_BUTTON.click();
        return new DashboardPageSteps();
    }

    public InstrumentGeneralSteps fillFormRequiredFields(InstrumentData instrumentData) {
        specifyName(instrumentData.getName());
        selectVendor(instrumentData.getVendor());
        selectModel(instrumentData.getModel());
        selectLab(instrumentData.getLaboratory());
        specifySerialNumber(instrumentData.getSerialNumber());
        return this;
    }

    public InstrumentGeneralSteps fillFormAllFields(InstrumentData instrumentData) {
        fillFormRequiredFields(instrumentData);
        OptionalFields optionalFields = instrumentData.getOptionalFields();
        if (optionalFields != null) {
            expandOptionalFields();
            setAutoTranslateOptionTo(optionalFields.isAutoTranslate());
            if (optionalFields.gethLPC() != null) {
                specifyHLPC(optionalFields.gethLPC());
            }
            if (optionalFields.getPeripherals() != null) {
                specifyPeripherals(optionalFields.getPeripherals());
            }
            if (optionalFields.isUseDefaultLockMass1()) {
                FIRST_DEFAULT_LOCK_MASS_VALUE_CHECKBOX.click();
            }
            if (optionalFields.isUseDefaultLockMass2()) {
                SECOND_DEFAULT_LOCK_MASS_VALUE_CHECKBOX.click();
            }
            if (optionalFields.getLockMasses() != null && optionalFields.getLockMasses().size() > 0) {
                LOCK_MASS_FIELD.scrollToElement();
                for (int i = 0; i < optionalFields.getLockMasses().size(); i++) {
                    LOCK_MASS_FIELD.fillIn(optionalFields.getLockMasses().get(i).getLockMass());
                    LOCK_MASS_CHARGE_DROPDOWN.select(optionalFields.getLockMasses().get(i).getCharge());
                    ADD_LOCK_MASS_BUTTON.click();
                }
            }
        }
        return this;
    }

    private InstrumentGeneralSteps expandOptionalFields() {
        OPTIONAL_FIELDS_BUTTON.clickUntilAttributeWillHaveExactValue(OPTIONALS_SECTION, "style", "");
        return this;
    }

    public boolean isCreationEnabled() {
        return CREATE_BUTTON.isEnabled();
    }

    public String getIncorrectSerialError() {
        INCORRECT_SERIAL_ERROR_LABEL.waitForAppearing();
        return INCORRECT_SERIAL_ERROR_LABEL.getText();
    }

    public String getIncorrectNameError() {
        INCORRECT_NAME_ERROR_LABEL.waitForAppearing();
        return INCORRECT_NAME_ERROR_LABEL.getText();
    }

    public InstrumentGeneralSteps clearNameField() {
        NAME_FIELD.clear();
        return this;
    }

    public InstrumentGeneralSteps clearSerialNumberField() {
        SERIAL_NUMBER_FIELD.clear();
        return this;
    }

    public InstrumentOperatorsSteps selectOperatorsTab() {
        OPERATORS_TAB.click();
        return new InstrumentOperatorsSteps();
    }

    public InstrumentGeneralSteps setAutoTranslateOptionTo(boolean value) {
        AUTO_TRANSLATE_CHECKBOX.setValue(value);
        return this;
    }
}
