package com.infoclinika.mssharing.test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.infoclinika.mssharing.helper.AbstractBillingTest;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.read.PaymentHistoryReader;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader.Invoice;
import com.infoclinika.mssharing.services.billing.rest.api.model.LabAccountFeatureInfo;
import com.infoclinika.mssharing.services.billing.rest.api.model.LabInvoiceDetails;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static com.infoclinika.mssharing.model.write.billing.BillingManagement.LabPaymentAccountFeatureType.ARCHIVE_STORAGE;
import static com.infoclinika.mssharing.model.write.billing.BillingManagement.LabPaymentAccountFeatureType.PROCESSING;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.*;

/**
 * @author Elena Kurilina
 */
public class BillingTest extends AbstractBillingTest {

    public static final long ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE = 40;
    public static final long PROCESSING_FEATURE_SUFFICIENT_BALANCE = 200;
    public static final int MONTH_IN_HOURS = 31 * 24;
    public static final long MONTH_IN_MILLIS = 31 * 24 * 60 * 60 * 1000L;
    public static final int TIME_SUFFICIENT_TO_TRANSIT_BACK_TO_FREE = 12000;
/*
    @Test
    public void testDefaultAccountZero() {
        uc.createLab3AndBob();
        final Long labId = uc.getLab3();
        final LabInvoiceDetails details = billingService.readLabDetails(uc.createPaul(), labId);
        assertEquals(details.storeBalance, 0L);
    }

    @Test
    public void testDefaultAccountTypeFree() {
        uc.createLab3AndBob();
        final Long labId = uc.getLab3();
        final LabInvoiceDetails details = billingService.readLabDetails(uc.createPaul(), labId);
        Assert.assertTrue(details.isFree);
    }

    @Test
    public void testAnalyzableStorageLogEntryCreate() {
        long bob = uc.createLab3AndBob();
        final long head = uc.createPaul();
        uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        storageLogHelper.log(new Date().getTime());
        final Invoice invoice = getInvoice(head, uc.getLab3());
        assertEquals(analyzableStorageBill(invoice).usageByUsers.iterator().next().userId, bob);
    }

    @Test
    public void testEmptyBill() {
        uc.createLab3AndBob();
        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(invoice.total, 0);
    }

    @Test
    public void testTranslationLogEntryCreate() {
        long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        long file = uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), false);
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        Invoice invoice = getInvoice(paul, uc.getLab3());
        assertEquals(translationBill(invoice).usageByUsers.iterator().next().userId, bob);
    }

    @Test
    public void testCompressTranslationLog() {
        long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), false);
        usageReader.readLabsForUser(paul);
        long file = uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        Invoice invoice = getInvoice(paul, uc.getLab3());
        assertEquals(translationBill(invoice).usageByUsers.iterator().next().usageLines.size(), 1);
    }

    @Test
    public void testLogEntryCreateForManyFilesManyUsers() {
        long bob = uc.createLab3AndBob();
        long joe = uc.createJoe();
        uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        uc.saveFile(joe, uc.createInstrumentAndApproveIfNeeded(joe, uc.getLab3()).get());
        uc.saveFile(joe, uc.createInstrumentAndApproveIfNeeded(joe, uc.getLab3()).get());
        storageLogHelper.log(System.currentTimeMillis());
        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(analyzableStorageBill(invoice).usageByUsers.size(), 2);
        assertEquals(analyzableStorageBill(invoice).usageByUsers.iterator().next().usageLines.size(), 2);
    }

    @Test(enabled = true)
    public void testDailyStorageChargeZero() {
        long bob = uc.createLab3AndBob();
        uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);//10.23mb
        uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);//10.23mb
        final int daysInMonth = getDaysInMonth();
        simulateHours(daysInMonth / 2 * 24);
        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(analyzableStorageBill(invoice).total, 0);
    }


    @Test
    public void testStorageTime() {
        final long testTime = System.currentTimeMillis();
        long bob = uc.createLab3AndBob();
        uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        final long afterHours = simulateHours(testTime, 23, false);//simulate 1 month
        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3(), testTime - 1, afterHours + 1);
        assertEquals(((ChargeableItemUsageReader.FileUsageLine) analyzableStorageBill(invoice).usageByUsers.iterator()
                .next().usageLines.iterator().next()).hours, 23);
    }

    @Test
    public void testTranslationCalculatePriceForBigFile() {
//        paymentCalculations.createChargeableItem("translation", 450, 1); //cents
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        billingManagement.enableProcessingForLabAccount(uc.createPaul(), uc.getLab3(), false);
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());

        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(translationBill(invoice).usageByUsers.iterator().next().usageLines.iterator().next().price, 450);
    }

    @Test
    public void testTranslationBillSmallData() {
//        paymentCalculations.createChargeableItem("translation", 450, 1); //cents
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 1024 * 1024 * 50);
        billingManagement.enableProcessingForLabAccount(uc.createPaul(), uc.getLab3(), false);
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());

        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(translationBill(invoice).usageByUsers.iterator().next().usageLines.iterator().next().price, 450 / 20);
        assertEquals(invoice.storeBalance, -22 - AbstractTest.PROCESSING_FEATURE_PRICE);
    }

    @Test
    public void testTranslationCalculatePriceForDifferentUsers() {
//        paymentCalculations.createChargeableItem("translation", 450, 1); //cents
        long bob = uc.createLab3AndBob();
        long paul = uc.createPaul();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        long fileP = uc.saveFileWithSize(paul, uc.createInstrumentAndApproveIfNeeded(paul, uc.getLab3()).get(), GB_IN_BYTES);
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), false);
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(paul, fileP, uc.getLab3());
        paymentManagement.logTranslationUsage(paul, fileP, uc.getLab3());

        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(translationBill(invoice).usageByUsers.size(), 2);
        assertEquals(translationBill(invoice).usageByUsers.iterator().next().usageLines.iterator().next().price, 900);
    }

    @Test
    public void testTranslationCalculatePriceForDifferentFiles() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        billingManagement.enableProcessingForLabAccount(uc.createPaul(), uc.getLab3(), false);
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file2, uc.getLab3());
        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(translationBill(invoice).usageByUsers.size(), 1);
        assertEquals(translationBill(invoice).usageByUsers.iterator().next().usageLines.size(), 2);
    }

    @Test(enabled = true)
    public void testDepositStoreCreditBalance() {

        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);

        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file2, uc.getLab3());
        simulateHours(getDaysInMonth() * 24);

        final Invoice before = getInvoice(uc.createPaul(), uc.getLab3());

        final int deposit = 2000;
        paymentManagement.depositStoreCredit(createPayment(deposit, uc.getLab3()));

        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(invoice.storeBalance, before.storeBalance + deposit);
    }

    @Test
    public void testInvoiceTotal() {
        final long testTime = System.currentTimeMillis();
        long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), false);
        usageReader.readLabsForUser(paul);
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file2, uc.getLab3());
        final long afterHours = simulateHours(testTime, getDaysInMonth() * 24, false);
        Invoice invoice = getInvoice(paul, uc.getLab3(), testTime, afterHours);
        assertEquals(invoice.total, AbstractTest.TRANSLATION_PRICE * 2 + AbstractTest.PROCESSING_FEATURE_PRICE);
    }

    @Test
    public void testInvoiceToPayTotals() {
        final long testTime = System.currentTimeMillis();
        long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), false);
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file2, uc.getLab3());
        final long afterHours = simulateHours(testTime, getDaysInMonth() * 24, false);
        paymentManagement.depositStoreCredit(createPayment(200, uc.getLab3()));
        Invoice invoice = getInvoice(paul, uc.getLab3(), testTime, afterHours);
        assertEquals(invoice.total, AbstractTest.TRANSLATION_PRICE * 2 + AbstractTest.PROCESSING_FEATURE_PRICE);
    }

    @Test
    public void testTranslatedBytesPerUserForSameFile() {
        long bob = uc.createLab3AndBob();
        long joe = uc.createJoe();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        billingManagement.enableProcessingForLabAccount(uc.createPaul(), uc.getLab3(), false);
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(joe, file, uc.getLab3());
        simulateHours(getDaysInMonth() * 24);

        Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(translationBill(invoice).usageByUsers.iterator().next().totalUsedFeatureValue, GB_IN_BYTES);
    }

    @Test
    public void testLabLineTotal() {
        //1073741824  B in GB

        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        billingManagement.enableProcessingForLabAccount(uc.createPaul(), uc.getLab3(), false);

        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file2, uc.getLab3());

        Invoice before = getInvoice(uc.createPaul(), uc.getLab3());
        ImmutableSet<ChargeableItemUsageReader.InvoiceLabLine> labs = usageReader.readLabsForUser(uc.createPaul());
        assertEquals(Math.abs(labs.iterator().next().storeBalance), before.total);

    }

    @Test
    public void testLabLineTotalWithManyUsers() {
        //1073741824  B in GB

        long bob = uc.createLab3AndBob();
        long joe = uc.createJoe();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 107374);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), GB_IN_BYTES);
        billingManagement.enableProcessingForLabAccount(uc.createPaul(), uc.getLab3(), false);

        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file, uc.getLab3());
        paymentManagement.logTranslationUsage(joe, file, uc.getLab3());
        paymentManagement.logTranslationUsage(joe, file, uc.getLab3());
        paymentManagement.logTranslationUsage(joe, file, uc.getLab3());
        paymentManagement.logTranslationUsage(joe, file2, uc.getLab3());
        paymentManagement.logTranslationUsage(bob, file2, uc.getLab3());

        Invoice before = getInvoice(uc.createPaul(), uc.getLab3());
        ImmutableSet<ChargeableItemUsageReader.InvoiceLabLine> labs = usageReader.readLabsForUser(uc.createPaul());
        assertEquals(Math.abs(labs.iterator().next().storeBalance), before.total);

    }

    @Test
    public void testUploadLimitForFreeAccountLab() {
        final long testTime = System.currentTimeMillis() - MILLIS_IN_HOUR * 24;
        final long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();
        uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 95 * GB_IN_BYTES);
        simulateHours(testTime, getDaysInMonth() * 24, false);

        BillingManagement.UploadLimitCheckResult uploadLimitCheckResult = billingManagement.checkUploadLimit(paul, uc.getLab3());
        assertFalse(uploadLimitCheckResult.isExceeded);

        uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10 * GB_IN_BYTES);
        simulateHours(testTime, getDaysInMonth() * 24, false);
        uploadLimitCheckResult = billingManagement.checkUploadLimit(paul, uc.getLab3());
        assertTrue(uploadLimitCheckResult.isExceeded);
    }

    @Test
    public void testArchiveStorageFeatureIsEnabledWhenEnterpriseAccountIsActivated() {
        final long paul = uc.createLab3AndGetPaul();
        depositStoreCredit(uc.getLab3(), ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());
        assertNotNull(labAccountFeatureInfos);
        final Optional<LabAccountFeatureInfo> storageFeature = labAccountFeatureInfos.stream().filter(f -> f.name.equals(ARCHIVE_STORAGE.name())).findFirst();
        assertTrue(storageFeature.isPresent());
        assertTrue(storageFeature.get().active);
    }

    @Test
    public void testFreeLabDoNotHaveArchiveStorageFeatureActivated() {
        final long paul = uc.createLab3AndGetPaul();

        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());
        assertNotNull(labAccountFeatureInfos);
        final Optional<LabAccountFeatureInfo> storageFeature = labAccountFeatureInfos.stream().filter(f -> f.name.equals(ARCHIVE_STORAGE.name())).findFirst();
        storageFeature.ifPresent(f -> assertFalse(f.active));
    }

    @Test
    public void testLabLooseAccessToArchiveStorageFeatureAfterTransitionToFreeAccount() throws InterruptedException {
        final long paul = uc.createLab3AndGetPaul();
        depositStoreCredit(uc.getLab3(), 4000);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        Thread.sleep(TIME_SUFFICIENT_TO_TRANSIT_BACK_TO_FREE);
        billingManagement.makeLabAccountFree(paul, uc.getLab3());
        Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());
        assertNotNull(labAccountFeatureInfos);
        Optional<LabAccountFeatureInfo> storageFeature = labAccountFeatureInfos.stream().filter(f -> f.name.equals(ARCHIVE_STORAGE.name())).findFirst();
        assertTrue(storageFeature.isPresent());
        assertFalse(storageFeature.get().active);
    }

    @Test
    public void testAdminCanTopUpLabBalance() {

        uc.createLab3AndGetPaul();
        final long admin = admin();
        final ChargeableItemUsageReader.LabInvoiceDetails lab = chargeableItemUsageReader.readLabDetails(admin, uc.getLab3());

        assertThat(lab.storeBalance, is(0L));
        billingManagement.topUpLabBalance(admin, uc.getLab3(), 100 * 100); // $100
        final ChargeableItemUsageReader.LabInvoiceDetails details = chargeableItemUsageReader.readLabDetails(admin, uc.getLab3());
        assertThat(details.storeBalance, is(100 * 100L));

        //more balance
        billingManagement.topUpLabBalance(admin, uc.getLab3(), 100 * 100); // %100

        assertThat(chargeableItemUsageReader.readLabDetails(admin, uc.getLab3()).storeBalance, is(200 * 100L)); //$200

    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testNotAdminCannotTopUpLabBalance() {
        final long paul = uc.createLab3AndGetPaul();
        final ChargeableItemUsageReader.LabInvoiceDetails lab = chargeableItemUsageReader.readLabDetails(paul, uc.getLab3());

        assertThat(lab.storeBalance, is(0L));
        billingManagement.topUpLabBalance(paul, uc.getLab3(), 100 * 100); // $100
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnableTopUpBalanceNegativeAmount() {
        billingManagement.topUpLabBalance(admin(), uc.createLab3(), - 10000); // -$100
    }

    @Test
    public void testPaymentLogAppearsAfterAdminTopUpLabBalance() {

        uc.createLab3AndGetPaul();
        final long admin = admin();
        final ChargeableItemUsageReader.LabInvoiceDetails lab = chargeableItemUsageReader.readLabDetails(admin, uc.getLab3());

        billingManagement.topUpLabBalance(admin, uc.getLab3(), 100 * 100); // $100
        final PaymentHistoryReader.HistoryForLab history = paymentHistoryReader.readNextHistory(admin, uc.getLab3(), 0, 1);
        final PaymentHistoryReader.PaymentHistoryLine next = Iterables.getLast(history.months.iterator().next().lines);

        assertThat(next.type, is(PaymentHistoryReader.HistoryItemType.STORE));
        assertThat(next.amount, is(100 * 100L));
    }


    @Test
    public void testCanRetrieveFeaturesListByLabIfUserIsNotLabHead() {
        long bob = uc.createLab3AndBob();
        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());
        assertNotNull(labAccountFeatureInfos);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testNotLabHeadCannotManageLab() {
        billingManagement.makeLabAccountEnterprise(uc.createLab3AndBob(), uc.getLab3());
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testAdminCannotManageLabIfNotLabHead() {
        uc.createLab3AndBob();
        billingManagement.makeLabAccountEnterprise(admin(), uc.getLab3());
    }

    @Test
    public void testMakeLabAccountEnterprise() {
        final long paul = uc.createLab3AndGetPaul();
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        final LabInvoiceDetails details = billingService.readLabDetails(paul, uc.getLab3());
        Assert.assertFalse(details.isFree);
    }

    @Test
    public void testCanActivateProcessingFeatureForEnterpriseLab() {
        final long paul = uc.createLab3AndGetPaul();

        depositStoreCredit(uc.getLab3(), PROCESSING_FEATURE_SUFFICIENT_BALANCE);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), true);

        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());

        assertNotNull(labAccountFeatureInfos);
        Optional<LabAccountFeatureInfo> processingFeature = labAccountFeatureInfos.stream().filter(f -> f.name.equals(PROCESSING.name())).findFirst();

        assertTrue(processingFeature.isPresent());
        assertTrue(processingFeature.get().active);
    }

    @Test
    public void testCanActivateProcessingFeatureForFreeLab() {
        final long paul = uc.createLab3AndGetPaul();

        depositStoreCredit(uc.getLab3(), 1000);
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), true);

        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());

        assertNotNull(labAccountFeatureInfos);
        Optional<LabAccountFeatureInfo> processingFeature = labAccountFeatureInfos.stream().filter(f -> f.name.equals(PROCESSING.name())).findFirst();

        assertTrue(processingFeature.isPresent());
        assertTrue(processingFeature.get().active);
    }


    @Test
    public void testCanActivateProcessingFeatureIfLabAccountHasZeroBalance() {
        final long paul = uc.createLab3AndGetPaul();
        //balance is zero
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), true);
    }

    @Test
    public void testProcessingFeatureActivationCauseDebit() {
        final long expectedFundsCharged = toCents(PROCESSING_FEATURE_SUFFICIENT_BALANCE);
        final long paul = uc.createLab3AndGetPaul();

        depositStoreCredit(uc.getLab3(), 240);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        final ChargeableItemUsageReader.LabInvoiceDetails detailsBeforeProcessingActivation = chargeableItemUsageReader.readLabDetails(paul, uc.getLab3());
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), true);
        final ChargeableItemUsageReader.LabInvoiceDetails detailsAfterProcessingActivation = chargeableItemUsageReader.readLabDetails(paul, uc.getLab3());
        final long charged = detailsBeforeProcessingActivation.storeBalance - detailsAfterProcessingActivation.storeBalance;
        assertEquals(charged, expectedFundsCharged);
    }

    @Test
    public void testProcessingShouldBeTurnedOffAfterOneMonthIfAutoprolongateIsOff() {

        final long paul = uc.createLab3AndGetPaul();

        depositStoreCredit(uc.getLab3(), 1000);
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), false);

        simulateHours(System.currentTimeMillis(), MONTH_IN_HOURS + 10, true);
        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());

        assertNotNull(labAccountFeatureInfos);
        Optional<LabAccountFeatureInfo> processingFeature = labAccountFeatureInfos.stream().filter(f -> f.name.equals(PROCESSING.name())).findFirst();
        assertTrue(processingFeature.isPresent());
        assertFalse(processingFeature.get().active);
    }

    @Test
    public void testProcessingRemainsActiveAfterOneMonthIfAutoprolongateIsOn() {
        final long paul = uc.createLab3AndGetPaul();
        depositStoreCredit(uc.getLab3(), 1000);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), true);
        simulateHours(System.currentTimeMillis(), MONTH_IN_HOURS + 10, true);

        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());

        assertNotNull(labAccountFeatureInfos);
        Optional<LabAccountFeatureInfo> processingFeature = labAccountFeatureInfos.stream().filter(f -> f.name.equals(PROCESSING.name())).findFirst();
        assertTrue(processingFeature.isPresent());
        assertTrue(processingFeature.get().active);
    }

    @Test
    public void testLabCanBecomeFreeIfProcessingIsOn() throws InterruptedException {
        final long paul = uc.createLab3AndGetPaul();
        depositStoreCredit(uc.getLab3(), 1000);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), true);
        Thread.sleep(TIME_SUFFICIENT_TO_TRANSIT_BACK_TO_FREE);
        billingManagement.makeLabAccountFree(paul, uc.getLab3());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testLabCanNotBecomeFreeIfLessThanSufficientTimePassedSinceBecomingEnterprise() throws InterruptedException {
        final long paul = uc.createLab3AndGetPaul();
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        Thread.sleep(1000);
        billingManagement.makeLabAccountFree(paul, uc.getLab3());
    }

    @Test
    public void testLabShouldBeAbleToBecomeFreeIfSufficientTimePassedSinceBecomingEnterprise() throws InterruptedException {
        final long paul = uc.createLab3AndGetPaul();
        depositStoreCredit(uc.getLab3(), ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        Thread.sleep(TIME_SUFFICIENT_TO_TRANSIT_BACK_TO_FREE);   // 32 days
        billingManagement.makeLabAccountFree(paul, uc.getLab3());

        final LabInvoiceDetails labDetails = billingService.readLabDetails(paul, uc.getLab3());

        assertThat(labDetails.accountType, is(LabPaymentAccount.LabPaymentAccountType.FREE.name()));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testLabCanNotBecomeFreeIfUploadLimitIsExceeded() throws InterruptedException {
        final long testTime = System.currentTimeMillis() - MILLIS_IN_HOUR * 24;
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        depositStoreCredit(uc.getLab3(), 1000);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 105 * GB_IN_BYTES);
        simulateHours(testTime, 24, false);
        Thread.sleep(TIME_SUFFICIENT_TO_TRANSIT_BACK_TO_FREE);
        billingManagement.makeLabAccountFree(paul, uc.getLab3());
    }

    @Test()
    public void workplace() {
        final long paul = uc.createLab3AndGetPaul();

        depositStoreCredit(uc.getLab3(), ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE);
        depositStoreCredit(uc.getLab3(), PROCESSING_FEATURE_SUFFICIENT_BALANCE);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());
        billingManagement.enableProcessingForLabAccount(paul, uc.getLab3(), true);

        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(uc.getLab3());
        assertNotNull(labAccountFeatureInfos);
        Optional<LabAccountFeatureInfo> processingFeature = labAccountFeatureInfos.stream().filter(f -> f.name.equals(PROCESSING.name())).findFirst();

        assertTrue(processingFeature.isPresent());
        assertTrue(processingFeature.get().active);
    }

    @Test
    public void testMakingEnterpriseDisablesUploadLimits() {
        final long testTime = System.currentTimeMillis() - MILLIS_IN_HOUR * 24;
        final long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();

        uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 300 * GB_IN_BYTES);
        simulateHours(testTime, 24, false);

        assertTrue(billingManagement.checkUploadLimit(paul, uc.getLab3()).isExceeded);

        depositStoreCredit(uc.getLab3(), ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE);
        billingManagement.makeLabAccountEnterprise(paul, uc.getLab3());

        assertFalse(billingManagement.checkUploadLimit(paul, uc.getLab3()).isExceeded);
    }

    @Test
    public void testEnterpriseLabIsNotChargedForStorageWithinFreeLabBounds() {
        final long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();
        final Long labId = uc.getLab3();

        depositStoreCredit(labId, ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE);

        final long currentBalance = currentBalance(paul, labId);

        final Long freeAccountStorageLimit = billingPropertiesProvider.getFreeAccountStorageLimit();
        uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, labId).get(), freeAccountStorageLimit);
        logStorage();

        storageAndProcessingFeaturesUsageAnalyser.analyseStorageVolumeUsage(new Date().getTime() + MONTH_IN_MILLIS);

        final long balanceAfterStorageChanged = currentBalance(paul, labId);

        assertEquals(currentBalance, balanceAfterStorageChanged);
    }

    @Test
    public void testStorageAnalyserChargesAfterMonthPassed() {

        final long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();
        final Long labId = uc.getLab3();

        depositStoreCredit(labId, ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE);
        billingManagement.makeLabAccountEnterprise(paul, labId);
        final long currentBalance = currentBalance(paul, labId);
        final long storageUsed = createFilesAndLog(bob, labId);
        final long storageCost = storageCost(storageUsed);

        storageAndProcessingFeaturesUsageAnalyser.analyseStorageVolumeUsage(new Date().getTime() + MONTH_IN_MILLIS);

        final long balanceAfterStorageChanged = currentBalance(paul, labId);

        assertEquals(currentBalance - storageCost, balanceAfterStorageChanged);
    }

    @Test
    public void testEnterpriseLabIsNotChargedForArchiveStorageWithinFreeLabBounds() {
        final long paul = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        final Long labId = uc.getLab3();

        depositStoreCredit(labId, ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE);

        final long currentBalance = currentBalance(paul, labId);

        final Long freeAccountStorageLimit = billingPropertiesProvider.getFreeAccountStorageLimit();
        final long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, labId).get(), freeAccountStorageLimit);
        fileMovingManager.moveToArchiveStorage(file);
        logStorage();

        storageAndProcessingFeaturesUsageAnalyser.analyseArchiveStorageVolumeUsage(new Date().getTime() + MONTH_IN_MILLIS);

        final long balanceAfterStorageChanged = currentBalance(paul, labId);

        assertEquals(currentBalance, balanceAfterStorageChanged);
    }

    @Test
    public void testArchiveStorageAnalyserChargesAfterMonthPassed() {

        final long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();
        final Long labId = uc.getLab3();

        depositStoreCredit(labId, ENTERPRISE_TRANSITION_SUFFICIENT_BALANCE);
        billingManagement.makeLabAccountEnterprise(paul, labId);
        final long currentBalance = currentBalance(paul, labId);
        final long storageUsed = createFilesArchiveAndLog(bob, labId);
        final long storageCost = archiveStorageCost(storageUsed);

        storageAndProcessingFeaturesUsageAnalyser.analyseArchiveStorageVolumeUsage(new Date().getTime() + MONTH_IN_MILLIS);

        final long balanceAfterStorageCharged = currentBalance(paul, labId);

        assertEquals(currentBalance - storageCost, balanceAfterStorageCharged);
    }

    @Test
    public void testUploadedSizeForLab() {
        final long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();
        final Long labId = uc.getLab3();
        long testTime = new Date().getTime();

        long storageUsage = billingService.getUploadedFilesSizeForLab(paul, labId, testTime);
        assertEquals(storageUsage, 0L);

        final long fileOneSize = GB_IN_BYTES * 10;
        final long fileTwoSize = GB_IN_BYTES * 20;
        final long fileOneId = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, labId).get(), fileOneSize);
        final long fileTwoId = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, labId).get(), fileTwoSize);
        testTime = simulateHours(System.currentTimeMillis(), 24, false);

        storageUsage = billingService.getUploadedFilesSizeForLab(paul, labId, testTime);
        assertEquals(storageUsage, fileOneSize + fileTwoSize);

        instrumentManagement.removeFilesPermanently(paul, Sets.newHashSet(fileOneId));
        testTime = simulateHours(testTime, 24, false);

        storageUsage = billingService.getUploadedFilesSizeForLab(paul, labId, testTime);
        assertEquals(storageUsage, fileTwoSize);

        instrumentManagement.removeFilesPermanently(paul, Sets.newHashSet(fileTwoId));
        testTime = simulateHours(testTime, 24, false);

        storageUsage = billingService.getUploadedFilesSizeForLab(paul, labId, testTime);
        assertEquals(storageUsage, 0);

    }


    private long currentBalance(long actor, long lab) {
        return billingService.readLabDetails(actor, lab).storeBalance;
    }

    private long storageCost(long bytes) {
        final int volumes = paymentCalculations.calculateStorageVolumes(bytes);
        return paymentCalculations.calculateStorageCost(volumes);
    }

    private long archiveStorageCost(long bytes) {
        final int volumes = paymentCalculations.calculateArchiveStorageVolumes(bytes);
        return paymentCalculations.calculateArchiveStorageCost(volumes);
    }
*/
}
