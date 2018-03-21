package com.infoclinika.mssharing.model.test.features;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.read.DashboardReader;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.infoclinika.mssharing.model.features.ApplicationFeature.MICROARRAYS;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Kaplin
 */
public class FeaturesInitializerTest extends AbstractTest {

    @Inject
    private DashboardReader dashboardReader;

    @Test
    public void testProteinIdSearchIsEnabled() {
        setProteinSearch(true);
        assertTrue(dashboardReader.getFeatures(uc.createLab3AndBob()).get(ApplicationFeature.PROTEIN_ID_SEARCH.getFeatureName()));
    }

    @Test
    public void testMicroArraysFeatureIsEnabledAfterManuallySpecified() {
        setFeature(MICROARRAYS, true);
        final long bob = uc.createLab3AndBob();
        assertTrue(dashboardReader.getFeatures(bob).get(MICROARRAYS.getFeatureName()));
    }
}
