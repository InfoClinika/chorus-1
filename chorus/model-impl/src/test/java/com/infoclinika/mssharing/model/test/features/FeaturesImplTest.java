package com.infoclinika.mssharing.model.test.features;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DashboardReader.FeatureItem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Map;

import static com.infoclinika.mssharing.model.features.ApplicationFeature.MICROARRAYS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Kaplin
 */
public class FeaturesImplTest extends AbstractTest {

    @Inject
    private DashboardReader dashboardReader;

    @BeforeMethod
    public void addSomeFeatures() {
        try {
            featuresManagement.add("whatever", false);
            featuresManagement.add("some_for_labs", true, ImmutableSet.of(uc.createLab3()));
        } catch (Exception ex) {
            //Ignore repeated initialisation error
        }
    }

    @Test
    public void testGet() throws Exception {
        assertFalse(dashboardReader.getFeatures(uc.createLab3AndBob()).get("whatever"));
    }

    @Test
    public void testFeaturesForUserInLabEnabled() {
        assertTrue(dashboardReader.getFeatures(uc.createLab3AndBob()).get("some_for_labs"));
    }

    @Test
    public void testFeaturesForUserOutLabDisabled() {
        assertFalse(dashboardReader.getFeatures(uc.createKateAndLab2()).get("some_for_labs"));
    }

    @Test
    public void getDetailsOfFeatureWhichIsEnabledOnlyForBobLab() {
        final long kate = uc.createKateAndLab2();
        final long bob = uc.createLab3AndBob();
        setFeaturePerLab(MICROARRAYS, ImmutableSet.of(uc.getLab3()));
        final Map<String, FeatureItem> kateFeatures = dashboardReader.getFeatureItems(kate);
        final Map<String, FeatureItem> bobFeatures = dashboardReader.getFeatureItems(bob);

        assertFalse(bobFeatures.get(MICROARRAYS.getFeatureName()).enabledGlobally);
        assertTrue(bobFeatures.get(MICROARRAYS.getFeatureName()).enabledForLabs.contains(uc.getLab3()));

        assertFalse(kateFeatures.get(MICROARRAYS.getFeatureName()).enabledGlobally);
        assertFalse(kateFeatures.get(MICROARRAYS.getFeatureName()).enabledForLabs.contains(uc.getLab2()));
        assertFalse(kateFeatures.get(MICROARRAYS.getFeatureName()).enabledForLabs.contains(uc.getLab3()));

        uc.addKateToLab3();

        final Map<String, FeatureItem> updatedKateFeatures = dashboardReader.getFeatureItems(kate);
        assertTrue(updatedKateFeatures.get(MICROARRAYS.getFeatureName()).enabledForLabs.contains(uc.getLab3()));

    }

    @Test
    public void getDetailsOfFeaturesWhichIsTurnedOnGlobally() {
        final long kate = uc.createKateAndLab2();
        setFeature(MICROARRAYS, true);

        final Map<String, FeatureItem> kateFeatures = dashboardReader.getFeatureItems(kate);

        assertTrue(kateFeatures.get(MICROARRAYS.getFeatureName()).enabledGlobally);
        assertTrue(kateFeatures.get(MICROARRAYS.getFeatureName()).enabledForLabs.isEmpty());
    }

    @Test
    public void getDetailsOfFeaturesWhichIsTurnedOffGlobally() {
        final long kate = uc.createKateAndLab2();
        setFeature(MICROARRAYS, false);

        final Map<String, FeatureItem> kateFeatures = dashboardReader.getFeatureItems(kate);

        assertFalse(kateFeatures.get(MICROARRAYS.getFeatureName()).enabledGlobally);
        assertTrue(kateFeatures.get(MICROARRAYS.getFeatureName()).enabledForLabs.isEmpty());
    }


}
