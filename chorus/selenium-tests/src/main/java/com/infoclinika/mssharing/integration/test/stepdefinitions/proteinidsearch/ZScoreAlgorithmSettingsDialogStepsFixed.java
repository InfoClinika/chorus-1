package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Checkbox;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class ZScoreAlgorithmSettingsDialogStepsFixed extends AlgorithmSettingsDialogSteps {

    private static final Checkbox FEATURE_Z_SCORE_CHECKBOX = controlFactory().checkbox(By.id("z-score-feature"));
    private static final Checkbox SAMPLE_Z_SCORE_CHECKBOX = controlFactory().checkbox(By.id("z-score-sample"));

   public ZScoreAlgorithmSettingsDialogStepsFixed setFeatureZScore(boolean value){
       FEATURE_Z_SCORE_CHECKBOX.setValue(value);
       return this;
   }

    public ZScoreAlgorithmSettingsDialogStepsFixed setSampleZScore(boolean value){
        SAMPLE_Z_SCORE_CHECKBOX.setValue(value);
        return this;
    }
}
