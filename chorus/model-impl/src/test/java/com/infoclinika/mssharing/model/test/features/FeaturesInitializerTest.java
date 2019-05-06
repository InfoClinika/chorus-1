package com.infoclinika.mssharing.model.test.features;

import com.infoclinika.mssharing.model.helper.AbstractTest;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.model.features.ApplicationFeature.MICROARRAYS;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Pavel Kaplin
 */
public class FeaturesInitializerTest extends AbstractTest {

    @Test
    public void testMicroArraysFeatureIsEnabledAfterManuallySpecified() {
        setFeature(MICROARRAYS, true);
        assertTrue(featuresHelper.isEnabled(MICROARRAYS));
        assertTrue(featuresHelper.isEnabledForLab(MICROARRAYS, uc.createLab3()));
    }
}
