package com.infoclinika.mssharing.test;

import com.infoclinika.mssharing.helper.AbstractBillingTest;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Elena Kurilina
 */
public class FeatureTest extends AbstractBillingTest {

    @Test
    public void testActiveByDefaultFeatures() {
        uc.createLab3AndBob();
        final Long labId = uc.getLab3();
        assertTrue(billingFeaturesHelper.isFeatureEnabled(labId, BillingFeature.ANALYSE_STORAGE));
        assertTrue(billingFeaturesHelper.isFeatureEnabled(labId, BillingFeature.DOWNLOAD));
        assertTrue(billingFeaturesHelper.isFeatureEnabled(labId, BillingFeature.PUBLIC_DOWNLOAD));
    }

    @Test
    public void testDisabledByDefaultFeatures() {
        setBilling(true);
        uc.createLab3AndBob();
        final Long labId = uc.getLab3();
        assertFalse(billingFeaturesHelper.isFeatureEnabled(labId, BillingFeature.ARCHIVE_STORAGE));
        assertFalse(billingFeaturesHelper.isFeatureEnabled(labId, BillingFeature.ARCHIVE_STORAGE_VOLUMES));
        assertFalse(billingFeaturesHelper.isFeatureEnabled(labId, BillingFeature.STORAGE_VOLUMES));
    }
}
