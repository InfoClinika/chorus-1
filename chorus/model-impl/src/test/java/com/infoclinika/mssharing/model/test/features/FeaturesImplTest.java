package com.infoclinika.mssharing.model.test.features;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.model.features.ApplicationFeature.*;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Pavel Kaplin
 */
public class FeaturesImplTest extends AbstractTest {

    @BeforeMethod
    public void addSomeFeatures() {
        try {
            featuresHelper.set(BLOG, false);
            featuresHelper.setForLabs(SUBSCRIBE, ImmutableSet.of(uc.createLab3()));
        } catch (Exception ex) {
            //Ignore repeated initialisation error
        }
    }

    @Test
    public void testGet() throws Exception {
        assertFalse(featuresHelper.isEnabled(BLOG));
    }

    @Test
    public void testFeaturesForUserInLabEnabled() {
        assertTrue(featuresHelper.isEnabledForLab(SUBSCRIBE, uc.getLab3()));
    }

    @Test
    public void testFeaturesForUserOutLabDisabled() {
        assertFalse(featuresHelper.isEnabledForLab(SUBSCRIBE, uc.createLab2()));
    }

    @Test
    public void getDetailsOfFeatureWhichIsEnabledOnlyForBobLab() {
        setFeaturePerLab(MICROARRAYS, ImmutableSet.of(uc.getLab3()));
        assertTrue(featuresHelper.isEnabled(MICROARRAYS));
        assertTrue(featuresHelper.isEnabledForLab(MICROARRAYS, uc.getLab3()));
        assertFalse(featuresHelper.isEnabledForLab(MICROARRAYS, uc.createLab2()));
    }


}
