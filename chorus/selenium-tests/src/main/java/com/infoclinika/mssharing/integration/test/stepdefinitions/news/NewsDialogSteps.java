package com.infoclinika.mssharing.integration.test.stepdefinitions.news;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.components.InputBox;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class NewsDialogSteps extends AbstractPageSteps{

    private static final InputBox NEWS_TITLE_FIELD = controlFactory().inputBox(By.id("name"));
    private static final InputBox CREATOR_EMAIL_FIELD = controlFactory().inputBox(By.id("url"));
    private static final InputBox INTRODUCTION_FIELD = controlFactory().inputBox(By.id("introduction"));
    private static final InputBox TEXT_FIELD = controlFactory().inputBox(By.id("text"));
    private static final Button CREATE_BUTTON = controlFactory().button(By.cssSelector("[ng-click=\"modalReturnUrl = '/news'\"]"));

    public NewsDialogSteps specifyNewsTitle(String newsTitle){
        NEWS_TITLE_FIELD.fillIn(newsTitle);
        return this;
    }

    public NewsDialogSteps specifyCreatorEmail(String email){
        CREATOR_EMAIL_FIELD.fillIn(email);
        return this;
    }

    public NewsDialogSteps specifyIntroduction(String introduction){
        INTRODUCTION_FIELD.fillIn(introduction);
        return this;
    }

    public NewsDialogSteps specifyText(String text){
        TEXT_FIELD.fillIn(text);
        return this;
    }

    public NewsListSteps pressCreateButton(){
        CREATE_BUTTON.click();
        return new NewsListSteps();
    }

}
