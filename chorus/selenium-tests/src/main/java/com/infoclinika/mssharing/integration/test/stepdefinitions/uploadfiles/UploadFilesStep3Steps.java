package com.infoclinika.mssharing.integration.test.stepdefinitions.uploadfiles;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class UploadFilesStep3Steps extends AbstractPageSteps {

    private static final Button UPLOAD_BUTTON = controlFactory().button(By.cssSelector("[ng-show='step == 3']"));


    public UploadFilesStep4Steps pressUploadButton() {
        UPLOAD_BUTTON.click();
        return new UploadFilesStep4Steps();
    }


}
