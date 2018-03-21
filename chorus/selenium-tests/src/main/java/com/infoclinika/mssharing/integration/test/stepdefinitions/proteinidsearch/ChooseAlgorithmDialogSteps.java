package com.infoclinika.mssharing.integration.test.stepdefinitions.proteinidsearch;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.data.proteinsearch.Algorithm;
import com.infoclinika.mssharing.integration.test.exception.FuncTestInfrastructureException;
import com.infoclinika.mssharing.integration.test.stepdefinitions.AbstractPageSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class ChooseAlgorithmDialogSteps extends AbstractPageSteps {

    private Button item(Algorithm itemName) {
        return new Button(By.xpath("//*[@id='new-filter-dialog']//*[contains(text(), '" + itemName.getValue() + "')]"));
    }

    private static final Button NEXT_BUTTON = controlFactory().button(By.cssSelector("[ng-click=\"nextWizardStep()\"]"));

    public AlgorithmSettingsDialogSteps selectAlgorithm(Algorithm algorithm) {
        switch (algorithm) {
            case NORMALIZATION:
            case RATIO:
            case LOG2:
            case LOG10:
            case LOGN: {
                item(Algorithm.SIMPLE_MATH).click();
                break;
            }
            case CLUSTERING: {
                throw new FuncTestInfrastructureException("Clustering Algorithm is not implemented yet");
            }
            case Z_SCORE: {
                item(Algorithm.ANALYSIS).click();
                item(algorithm).click();
                NEXT_BUTTON.click();
                return new ZScoreAlgorithmSettingsDialogStepsFixed();
            }
            case ANOVA: {
                item(Algorithm.ANALYSIS).click();
                item(algorithm).click();
                NEXT_BUTTON.click();
                return new ANOVAAlgorithmSettingsDialogSteps();
            }
            case FILTERING: {
                item(Algorithm.COMMON).click();
                item(algorithm).click();
                NEXT_BUTTON.click();
                return new FilteringSettingsDialogSteps();
            }
            case SORTING: {
                item(Algorithm.COMMON).click();
                item(algorithm).click();
                NEXT_BUTTON.click();
                return new SortingSettingsDialogSteps();
            }
        }
        item(algorithm).click();
        NEXT_BUTTON.click();
        return new AlgorithmSettingsDialogSteps();
    }


}
