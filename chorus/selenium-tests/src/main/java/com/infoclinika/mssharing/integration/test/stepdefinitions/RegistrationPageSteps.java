package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.AutoCompleteList;
import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.data.UserData;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class RegistrationPageSteps extends AbstractPageSteps {

    //Dynamic locators
    private Label itemInAutocompleteList(String item) {
        return new Label(By.partialLinkText(item));
    }

    private static final InputBox FIRST_NAME_FIELD = controlFactory().inputBox(By.id("firstName"));
    private static final InputBox LAST_NAME_FIELD = controlFactory().inputBox(By.id("lastName"));
    private static final InputBox ADD_LAB_FIELD = controlFactory().inputBox(By.className("add-lab"));
    private static final AutoCompleteList AUTOCOMPLETE_LIST = controlFactory().autoCompleteList(By.cssSelector(".ui-autocomplete.ui-menu"));
    private static final Button CANT_FIND_LINK = controlFactory().button(By.cssSelector("[href='laboratory-request.html']"));
    private static final InputBox CONFIRM_EMAIL_FIELD = controlFactory().inputBox(By.id("confirmEmail"));
    private static final InputBox CONFIRM_PASSWORD_FIELD = controlFactory().inputBox(By.id("confirmPassword"));
    private static final Button CREATE_BUTTON = controlFactory().button(By.cssSelector(".btn.main-action"));
    private static final Label FIRST_NAME_ALERT = controlFactory().label(By.cssSelector("[for='firstName']"));
    private static final Label LAST_NAME_ALERT = controlFactory().label(By.cssSelector("[for='lastName']"));
    private static final Label EMAIL_ALERT = controlFactory().label(By.cssSelector("[for='email']"));
    private static final Label CONFIRM_EMAIL_ALERT = controlFactory().label(By.cssSelector("[for='confirmEmail']"));
    private static final Label PASSWORD_ALERT = controlFactory().label(By.cssSelector("[for='password']"));
    private static final Label CONFIRM_PASSWORD_ALERT = controlFactory().label(By.cssSelector("[for='confirmPassword']"));
    private static final InputBox EMAIL_FIELD = controlFactory().inputBox(By.id("email"));
    private static final InputBox PASSWORD_FIELD = controlFactory().inputBox(By.id("password"));

    //First Name input box methods
    public RegistrationPageSteps specifyFirstName(String firstName) {
        FIRST_NAME_FIELD.fillIn(firstName);
        return this;
    }

    public RegistrationPageSteps clearFirstName() {
        FIRST_NAME_FIELD.clear();
        return this;
    }

    public String getFirstNameAlert() {
        return FIRST_NAME_ALERT.getText();
    }

    //Last Name input box methods
    public RegistrationPageSteps specifyLastName(String lastName) {
        LAST_NAME_FIELD.fillIn(lastName);
        return this;
    }

    public RegistrationPageSteps clearLastName() {
        LAST_NAME_FIELD.clear();
        return this;
    }

    public String getLastNameAlert() {
        return LAST_NAME_ALERT.getText();
    }

    //Email input box methods
    public RegistrationPageSteps specifyEmail(String email) {
        EMAIL_FIELD.fillIn(email);
        return this;
    }

    public RegistrationPageSteps clearEmail() {
        EMAIL_FIELD.clear();
        return this;
    }

    public String getEmailAlert() {
        return EMAIL_ALERT.getText();
    }

    //Confirm Email input box methods
    public RegistrationPageSteps confirmEmail(String email) {
        CONFIRM_EMAIL_FIELD.fillIn(email);
        return this;
    }

    public RegistrationPageSteps clearConfirmEmail() {
        CONFIRM_EMAIL_FIELD.clear();
        return this;
    }

    public String getConfirmEmailAlert() {
        return CONFIRM_EMAIL_ALERT.getText();
    }

    //Password input box methods
    public RegistrationPageSteps specifyPassword(String password) {
        PASSWORD_FIELD.fillIn(password);
        return this;
    }

    public RegistrationPageSteps clearPassword() {
        PASSWORD_FIELD.clear();
        return this;
    }

    public String getPasswordAlert() {
        return PASSWORD_ALERT.getText();
    }

    //Confirm Password input box methods
    public RegistrationPageSteps confirmPassword(String password) {
        CONFIRM_PASSWORD_FIELD.fillIn(password);
        return this;
    }

    public RegistrationPageSteps clearConfirmPassword() {
        CONFIRM_PASSWORD_FIELD.clear();
        return this;
    }

    public String getConfirmPasswordAlert() {
        return CONFIRM_PASSWORD_ALERT.getText();
    }

    //Laboratory Section methods
    public LaboratoryCreationRequestSteps openLabRequestPage() {
        CANT_FIND_LINK.scrollAndClick();
        return new LaboratoryCreationRequestSteps();
    }

    public RegistrationPageSteps specifyLab(String labName) {
        ADD_LAB_FIELD.fillIn(labName);
        AUTOCOMPLETE_LIST.waitForAppearing();
        itemInAutocompleteList(labName).click();
        return this;
    }

    //Submit form button methods
    public boolean isCreateButtonEnabled() {
        return CREATE_BUTTON.isEnabled();
    }

    public LoginPageSteps pressCreateButton() {
        CREATE_BUTTON.scrollAndClick();
        return new LoginPageSteps();
    }

    //Complex methods
    public RegistrationPageSteps fillForm(UserData userData) {
        specifyFirstName(userData.getFirstName());
        specifyLastName(userData.getLastName());
        specifyEmail(userData.getEmail());
        if (userData.getEmailConfirmation() == null) {
            confirmEmail(userData.getEmail());
        } else {
            confirmEmail(userData.getEmailConfirmation());
        }
        specifyPassword(userData.getPassword());
        if (userData.getPasswordConfirmation() == null) {
            confirmPassword(userData.getPassword());
        } else {
            confirmPassword(userData.getPasswordConfirmation());
        }
        return this;
    }

    public String getEmail() {
        return EMAIL_FIELD.getValue();
    }

    public String getConfirmationEmail() {
        return CONFIRM_EMAIL_FIELD.getValue();
    }
}
