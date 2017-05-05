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

    @Test
    public void testDisableBillingMakeTranslationEnable() {
        long lab = uc.createLab3();
        setBilling(false);
        assertTrue(billingFeaturesHelper.isFeatureEnabled(lab, BillingFeature.TRANSLATION));
    }

    @Test
    public void testNotOwnerOfFileCanTranslateExperimentFilesWithEnabledBilling() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        final long paulsFile = uc.saveFile(paul);
        setFeaturePerLab(ApplicationFeature.TRANSLATION, Lists.newArrayList(uc.getLab3()));
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), false);
        setBilling(true);
        createExperiment(paul, createPublicProject(paul), paulsFile, uc.getLab3()); //Make Paul's file public
        final long experiment = createExperiment(bob, createPublicProject(bob), paulsFile, uc.getLab3());

        studyManagement.markExperimentFilesForTranslation(bob, experiment, uc.getLab3());

    }

    @Test
    public void testNotOwnerOfFileCanTranslateExperimentFilesWhenBillingDisabled() {
        setBilling(false);
        final long bob = uc.createLab3AndBob();
        setFeaturePerLab(ApplicationFeature.TRANSLATION, Lists.newArrayList(uc.getLab3()));
        final long paul = uc.createPaul();
        final long paulsFile = uc.saveFile(paul);
        createExperiment(paul, createPublicProject(paul), paulsFile, uc.getLab3()); //Make Paul's file public
        final long experiment = createExperiment(bob, createPublicProject(bob), paulsFile, uc.getLab3());
        studyManagement.markExperimentFilesForTranslation(bob, experiment, uc.getLab3());
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
    public void testEnableBillingMakeTranslationDisable() {
        long lab = uc.createLab3();
        //featureManagement.disableFeature(lab, invoice.translationBill.chargeableId, null);
        setBilling(true);
        assertFalse(billingFeaturesHelper.isFeatureEnabled(lab, BillingFeature.TRANSLATION));
    }

    //Invalid case. Now can enable/disable features only through billing plan
    @Test(enabled = false)
    public void testDisableBillingMakeTranslationPossibleForOneFile() {
        long lab = uc.createLab3();
        long bob = uc.createLab3AndBob();

        ImmutableSet<DictionaryItem> models = instrumentCreationHelper.models(thermoVendor());
        long file = uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), models.iterator().next().id).get());
        // featureManagement.disableFeature(lab, invoice.translationBill.chargeableId, null);
        setBilling(false);
        studyManagement.markFileForTranslation(bob, uc.getLab3(), file);
        assertEquals(fileReader.readFiles(bob, Filter.MY).iterator().next().status, DashboardReader.TranslationStatus.IN_PROGRESS);
    }

    //Invalid case. Now can enable/disable features only through billing plan
    @Test(enabled = false)
    public void testEnableBillingMakeTranslationNotPossibleForOneFile() {
        long lab = uc.createLab3();
        long bob = uc.createLab3AndBob();
        ImmutableSet<DictionaryItem> models = instrumentCreationHelper.models(thermoVendor());
        long file = uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), models.iterator().next().id).get());
        // featureManagement.disableFeature(lab, invoice.translationBill.chargeableId, null);
        setBilling(true);
        studyManagement.markFileForTranslation(bob, lab, file);
        assertEquals(fileReader.readFiles(bob, Filter.MY).iterator().next().status, DashboardReader.TranslationStatus.FAILURE);
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
