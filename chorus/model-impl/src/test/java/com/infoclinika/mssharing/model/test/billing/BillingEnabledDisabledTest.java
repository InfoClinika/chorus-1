package com.infoclinika.mssharing.model.test.billing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.UploadUnavailable;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.SortedSet;

import static org.testng.Assert.*;

/**
 * @author Elena Kurilina
 */
public class BillingEnabledDisabledTest extends AbstractTest {

    @Inject
    protected BillingFeaturesHelper billingFeaturesHelper;

    @Test(expectedExceptions = AccessDenied.class)
    public void testDisableBillingMakePossibleMultipartUpload() {
        long lab = uc.createLab3();
        long bob = uc.createLab3AndBob();
        long file = uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        setBilling(false);
        instrumentManagement.completeMultipartUpload(bob, file, generateString());
    }

    @Test
    public void testDisableBillingMakeInstrumentEnableForUpload() {
        long lab = uc.createLab3();
        long bob = uc.createLab3AndBob();
        uc.createInstrumentAndApproveIfNeeded(bob, lab);
        uc.createInstrumentAndApproveIfNeeded(bob, lab);
        setBilling(false);
        SortedSet<InstrumentItem> instruments = dashboardReader.readInstrumentsWhereUserIsOperator(bob);
        assertFalse(instruments.isEmpty());

    }

    @Test
    public void testDisableBillingMakeStorageEnable() {
        long lab = uc.createLab3();
        setBilling(false);
        assertTrue(billingFeaturesHelper.isFeatureEnabled(lab, BillingFeature.ANALYSE_STORAGE));
    }

    //Invalid case. Now can enable/disable features only through billing plan
    @Test(enabled = false)
    public void testEnableBillingMakeStorageDisable() {
        long lab = uc.createLab3();
        //featureManagement.disableFeature(lab, invoice.analyzableStorageBill.chargeableId, null);
        setBilling(true);
        assertFalse(billingFeaturesHelper.isFeatureEnabled(lab, BillingFeature.ARCHIVE_STORAGE) ||
            billingFeaturesHelper.isFeatureEnabled(lab, BillingFeature.ANALYSE_STORAGE));
    }

    //Invalid case. Now can enable/disable features only through billing plan
    @Test(enabled = false)
    public void testEnableBillingMakeInstrumentNotEnableForUpload() {
        long lab = uc.createLab3();
        long bob = uc.createLab3AndBob();
        uc.createInstrumentAndApproveIfNeeded(bob, lab);
        uc.createInstrumentAndApproveIfNeeded(bob, lab);
        // featureManagement.disableFeature(lab, invoice.analyzableStorageBill.chargeableId, null);
        setBilling(true);
        SortedSet<InstrumentItem> instruments = dashboardReader.readInstrumentsWhereUserIsOperator(bob);
        assertTrue(instruments.isEmpty());

    }

    //Invalid case. Now can enable/disable features only through billing plan
    @Test(expectedExceptions = UploadUnavailable.class, enabled = false)
    public void testEnableBillingMakeNotPossibleMultipartUpload() {
        long lab = uc.createLab3();
        long bob = uc.createLab3AndBob();
        long file = uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        //featureManagement.disableFeature(lab, invoice.analyzableStorageBill.chargeableId, null);
        setBilling(true);
        instrumentManagement.completeMultipartUpload(bob, file, generateString());
    }
}
