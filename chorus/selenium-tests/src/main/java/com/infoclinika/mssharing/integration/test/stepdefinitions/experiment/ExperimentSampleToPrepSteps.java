package com.infoclinika.mssharing.integration.test.stepdefinitions.experiment;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class ExperimentSampleToPrepSteps extends AbstractPageSteps {


    private static final Button NEXT_BUTTON = controlFactory().button(By.id("next"));

    public ExperimentDesignSteps pressNext(){
        NEXT_BUTTON.click();
        return new ExperimentDesignSteps();
    }


    public ExperimentDesignSteps fillInSamplesAndPressNext(){

        return new ExperimentDesignSteps();
    }

}
