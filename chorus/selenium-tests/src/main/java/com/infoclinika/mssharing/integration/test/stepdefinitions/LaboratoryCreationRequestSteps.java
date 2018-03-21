package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.components.Label;
import com.infoclinika.mssharing.integration.test.data.LaboratoryData;
import org.openqa.selenium.By;

/**
 * @author Sergii Moroz
 */
public class LaboratoryCreationRequestSteps extends AbstractPageSteps {

    private static final InputBox INSTITUTION_URL_FIELD = controlFactory().inputBox(By.id("institutionUrl"));
    private static final InputBox LABORATORY_NAME_FIELD = controlFactory().inputBox(By.id("labName"));
    private static final InputBox YOUR_EMAIL_FIELD = controlFactory().inputBox(By.id("email"));
    private static final InputBox LAB_HEAD_FIRST_NAME_FIELD = controlFactory().inputBox(By.id("headFirstName"));
    private static final InputBox LAB_HEAD_LAST_NAME_FIELD = controlFactory().inputBox(By.id("headLastName"));
    private static final InputBox LAB_HEAD_EMAIL_FIELD = controlFactory().inputBox(By.id("headEmail"));
    private static final Label LAB_NAME_ALERT = controlFactory().label(By.cssSelector("[for='labName']"));
    private static final Label INSTITUTION_URL_ALERT = controlFactory().label(By.cssSelector("[for='institutionUrl']"));
    private static final Label YOUR_EMAIL_ALERT = controlFactory().label(By.cssSelector("[for='email']"));
    private static final Label LAB_HEAD_FIRST_NAME_ALERT = controlFactory().label(By.cssSelector("[for='headFirstName']"));
    private static final Label LAB_HEAD_LAST_NAME_ALERT = controlFactory().label(By.cssSelector("[for='headLastName']"));
    private static final Label LAB_HEAD_EMAIL_ALERT = controlFactory().label(By.cssSelector("[for='headEmail']"));
    private static final Button CREATE_BUTTON = controlFactory().button(By.cssSelector(".btn.main-action"));
    private static final Label SUCCESS_MESSAGE_LABEL = controlFactory().label(By.cssSelector(".description > h3"));

    public LaboratoryCreationRequestSteps fillForm(LaboratoryData labData) {
        specifyInstitutionUrl(labData.getInstitutionUrl());
        specifyLaboratoryName(labData.getLaboratoryName());
        specifyContactEmail(labData.getContactEmail());
        specifyLabHeadFirstName(labData.getLabHeadFirstName());
        specifyLabHeadLastName(labData.getLabHeadLastName());
        specifyLabHeadEmail(labData.getLabHeadEmail());
        return new LaboratoryCreationRequestSteps();
    }

    public boolean isCreationEnabled() {
        return CREATE_BUTTON.isEnabled();
    }

    public LaboratoryCreationRequestSteps specifyInstitutionUrl(String url) {
        INSTITUTION_URL_FIELD.fillIn(url);
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps clearInstitutionUrl() {
        INSTITUTION_URL_FIELD.clear();
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps specifyLaboratoryName(String labName) {
        LABORATORY_NAME_FIELD.fillIn(labName);
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps clearLaboratoryName() {
        LABORATORY_NAME_FIELD.clear();
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps specifyContactEmail(String email) {
        YOUR_EMAIL_FIELD.fillIn(email);
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps clearContactEmail() {
        YOUR_EMAIL_FIELD.clear();
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps specifyLabHeadFirstName(String labHeadFirstName) {
        LAB_HEAD_FIRST_NAME_FIELD.fillIn(labHeadFirstName);
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps clearLabHeadFirstName() {
        LAB_HEAD_FIRST_NAME_FIELD.clear();
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps specifyLabHeadLastName(String lanHeadLastName) {
        LAB_HEAD_LAST_NAME_FIELD.fillIn(lanHeadLastName);
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps clearLabHeadLastName() {
        LAB_HEAD_LAST_NAME_FIELD.clear();
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps specifyLabHeadEmail(String labHeadEmail) {
        LAB_HEAD_EMAIL_FIELD.fillIn(labHeadEmail);
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps clearLabHeadEmail() {
        LAB_HEAD_EMAIL_FIELD.clear();
        return new LaboratoryCreationRequestSteps();
    }

    public LaboratoryCreationRequestSteps pressSendButton() {
        CREATE_BUTTON.click();
        return new LaboratoryCreationRequestSteps();
    }

    public String getAlertSuccessText() {
        SUCCESS_MESSAGE_LABEL.waitForElementToBeVisible();
        return SUCCESS_MESSAGE_LABEL.getText();
    }

    public String getInstitutionURLAlert() {
        return INSTITUTION_URL_ALERT.getText();
    }

    public String getLabNameAlert() {
        return LAB_NAME_ALERT.getText();
    }

    public String getContactEmailAlert() {
        return YOUR_EMAIL_ALERT.getText();
    }

    public String getLabHeadFirstNameAlert() {
        return LAB_HEAD_FIRST_NAME_ALERT.getText();
    }

    public String getLabHeadLastNameAlert() {
        return LAB_HEAD_LAST_NAME_ALERT.getText();
    }

    public String getLabHeadEmailAlert() {
        return LAB_HEAD_EMAIL_ALERT.getText();
    }


}
