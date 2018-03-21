package com.infoclinika.mssharing.test;

import com.infoclinika.mssharing.helper.AbstractBillingTest;
import com.infoclinika.mssharing.services.billing.persistence.BillingMigration;
import com.infoclinika.mssharing.services.billing.persistence.helper.BillingFeatureChargingHelper;
import com.infoclinika.mssharing.services.billing.persistence.helper.PaymentCalculationsHelper;

import javax.inject.Inject;

/**
 * @author timofei.kasianov 4/28/16
 */
public class BillingMigrationTest extends AbstractBillingTest {

    @Inject
    private BillingFeatureChargingHelper billingFeatureChargingHelper;
    @Inject
    private PaymentCalculationsHelper paymentCalculationsHelper;
    @Inject
    private BillingMigration billingMigration;
/*
    @Test
    public void testAccountFeaturesAreResetAfterMigration() {
        final long labId = uc.createLab3();
        final long paul = uc.createPaul();
        billingManagement.makeLabAccountEnterprise(paul, labId);
        final ChargeableItemUsageReader.LabInvoiceDetails details = chargeableItemUsageReader.readLabDetails(paul, labId);

        billingMigration.migrateAccounts(admin());

        final Set<String> features = chargeableItemUsageReader.readLabDetails(paul, labId).featuresData.features;

        assertTrue(details.featuresData.features.size() != features.size());

        assertTrue(features.contains(BillingFeature.ANALYSE_STORAGE.toString()));
        assertFalse(features.contains(BillingFeature.TRANSLATION.toString()));
        assertTrue(features.contains(BillingFeature.DOWNLOAD.toString()));
        assertTrue(features.contains(BillingFeature.PUBLIC_DOWNLOAD.toString()));
        assertTrue(features.contains(BillingFeature.PROTEIN_ID_SEARCH.toString()));
    }

    @Test
    public void testNegativeBalanceIsResetAfterMigration() {
        final long labId = createAccountWithNegativeBalance();
        final long paul = uc.createPaul();
        final ChargeableItemUsageReader.LabInvoiceDetails detailsBefore = chargeableItemUsageReader.readLabDetails(paul, labId);

        assertTrue(detailsBefore.storeBalance < 0);

        billingMigration.migrateAccounts(admin());

        final ChargeableItemUsageReader.LabInvoiceDetails detailsAfter = chargeableItemUsageReader.readLabDetails(paul, labId);

        assertTrue(detailsAfter.storeBalance == 0);
    }

    @Test
    public void testPositiveBalanceIsNotResetAfterMigration() {
        final long labId = createAccountWithPositiveBalance();
        final long paul = uc.createPaul();
        final ChargeableItemUsageReader.LabInvoiceDetails detailsBefore = chargeableItemUsageReader.readLabDetails(paul, labId);

        assertTrue(detailsBefore.storeBalance > 0);

        billingMigration.migrateAccounts(admin());

        final ChargeableItemUsageReader.LabInvoiceDetails detailsAfter = chargeableItemUsageReader.readLabDetails(paul, labId);

        assertTrue(detailsAfter.storeBalance == detailsBefore.storeBalance);
    }

    @Test
    public void testAccountBecomesEnterpriseIfUsesArchiveStorage() {
        final long labId = uc.createLab3();
        final long paul = uc.createPaul();
        createFilesArchiveAndLog(paul, labId);

        final ChargeableItemUsageReader.LabInvoiceDetails detailsBefore = chargeableItemUsageReader.readLabDetails(paul, labId);
        assertTrue(detailsBefore.isFree);

        billingMigration.migrateAccounts(admin());

        final ChargeableItemUsageReader.LabInvoiceDetails detailsAfter = chargeableItemUsageReader.readLabDetails(paul, labId);
        assertTrue(!detailsAfter.isFree);
    }

    @Test
    public void testAccountBecomesEnterpriseIfUsesStorageMoreThanFreeLimit() {
        final long labId = uc.createLab3();
        final long paul = uc.createPaul();
        final long freeAccountStorageLimit = billingPropertiesProvider.getFreeAccountStorageLimit();
        long totalSize = 0;
        while (totalSize < freeAccountStorageLimit) {
            totalSize += createFilesAndLog(paul, labId);
        }

        final ChargeableItemUsageReader.LabInvoiceDetails detailsBefore = chargeableItemUsageReader.readLabDetails(paul, labId);
        assertTrue(detailsBefore.isFree);

        billingMigration.migrateAccounts(admin());

        final ChargeableItemUsageReader.LabInvoiceDetails detailsAfter = chargeableItemUsageReader.readLabDetails(paul, labId);
        assertTrue(!detailsAfter.isFree);
    }

    @Test
    public void testProcessingBecomesEnabledIfLabRecentlyRunAnalysis() {
        final long paul = uc.createPaul();
        final long labId = uc.getLab3();

        createExperimentAndRunSearch();

        final ChargeableItemUsageReader.LabInvoiceDetails details = chargeableItemUsageReader.readLabDetails(paul, labId);
        assertFalse(details.featuresData.features.contains(BillingFeature.PROCESSING.toString()));

        billingMigration.migrateAccounts(admin());

        final ChargeableItemUsageReader.LabInvoiceDetails detailsAfter = chargeableItemUsageReader.readLabDetails(paul, labId);
        assertTrue(detailsAfter.featuresData.features.contains(BillingFeature.PROCESSING.toString()));
    }

    private long createAccountWithNegativeBalance() {
        final long labId = uc.createLab3();
        final long price = 15001;
        final long scalePrice = paymentCalculationsHelper.scalePrice(price);
        billingFeatureChargingHelper.charge(labId, scalePrice + 999999);
        return labId;
    }

    private long createAccountWithPositiveBalance() {
        final long labId = uc.createLab3();
        paymentManagement.depositStoreCredit(admin(), labId, 15000);
        return labId;
    }

    private void createExperimentAndRunSearch() {

        final long bob = uc.createLab3AndBob();
        final long ex = createExperimentForRun(bob);
        final long workflowTemplate = workflowCreator.createWorkflowTemplate(admin(), "single_step", "", newArrayList(getPersistProteinDBStepType()));
        final long persistProteinStepID = getProcessorToWorkflowStepMap().get(PersistProteinDatabaseStepTypeProcessor.class);
        final long run = createProteinSearch(
                bob,
                ex,
                getProteinDatabaseEcoli(),
                workflowTemplate,
                newArrayList(persistProteinStepID), newHashSet()
        );

        workflowRunner.run(bob, run);
    }
*/

}
