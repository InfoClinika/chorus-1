package com.infoclinika.mssharing.test;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.helper.AbstractBillingTest;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.read.PaymentHistoryReader;
import com.infoclinika.mssharing.services.billing.persistence.helper.DailySummaryUsageLogger;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.rest.api.model.DailyUsageLine;
import com.infoclinika.mssharing.services.billing.rest.api.model.HistoryForMonthReference;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testng.Assert.*;

/**
 * @author Elena Kurilina
 */
public class HistoryBillingTest extends AbstractBillingTest {

    @Inject
    private DailySummaryUsageLogger dailySummaryUsageLogger;
    @Inject
    private Transformers transformers;

    @Test
    public void testStoreDepositHistory() {

        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 1073741824);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 1073741824);
        simulateHours(getDaysInMonth() * 24);
        paymentManagement.depositStoreCredit(createPayment(2000, uc.getLab3()));

        ImmutableSet<PaymentHistoryReader.HistoryForMonth> lines = paymentHistoryReader.readTopUpBalance(uc.createPaul(), uc.getLab3());

        assertEquals(lines.iterator().next().lines.size(), 1);
    }

    @Test
    public void testTopUppedBalanceLogTotalSameToInvoiceTotal() {

        long bob = uc.createLab3AndBob();
        usageReader.readLabsForUser(uc.createPaul());
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 1073741824);
        billingManagement.enableProcessingForLabAccount(uc.createPaul(), uc.getLab3(), false);
        final int deposit = 2000;
        paymentManagement.depositStoreCredit(createPayment(deposit, uc.getLab3()));

        ImmutableSet<PaymentHistoryReader.HistoryForMonth> lines = paymentHistoryReader.readTopUpBalance(uc.createPaul(), uc.getLab3());
        ChargeableItemUsageReader.Invoice invoice = getInvoice(uc.createPaul(), uc.getLab3());
        assertEquals(lines.iterator().next().lines.iterator().next().balance, deposit - invoice.total);
    }


    @Test
    public void testDailyUsageHistory() {

        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 1073741824);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 1073741824);

        ImmutableSet<PaymentHistoryReader.HistoryForMonth> lines = paymentHistoryReader.readDailyUsage(uc.createPaul(), uc.getLab3());

        assertEquals(lines.iterator().next().lines.size(), 1);
    }


    @Test
    public void testEmptyHistory() {

        uc.createLab3AndBob();

        ImmutableSet<PaymentHistoryReader.HistoryForMonth> lines = paymentHistoryReader.readAll(uc.createPaul(), uc.getLab3()).months;

        assertEquals(lines.size(), 1);
    }

    @Test
    public void testCannotReadMonthReferenceForDateBeforeAccountCreation() {

        final long head = uc.createPaul();
        uc.createLab3AndBob();

        final Date month = new DateTime().minusMonths(1).toDate();
        final Optional<HistoryForMonthReference> reference = chargeableItemUsageReader.readMonthsReferences(head, uc.getLab3(), month);

        assertFalse(reference.isPresent());

    }

    @Test(enabled = false)
    public void testReadUsagesByDay() {

        final long testTime = System.currentTimeMillis();
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), BillingTest.GB_IN_BYTES);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), BillingTest.GB_IN_BYTES);

        simulateHours(testTime, 24 * getDaysInMonth(), false);

        dailySummaryUsageLogger.saveDay(new Date(testTime));

        final Optional<DailyUsageLine> dailyUsageLine = chargeableItemUsageReader.readDailyUsageLine(uc.getLab3(), new Date(testTime));

        assertTrue(dailyUsageLine.isPresent());

        assertEquals(dailyUsageLine.get().amount,
                2 * AbstractTest.TRANSLATION_PRICE // Translation request
        );
    }

    @Test(enabled = true)
    public void testCantSaveAlreadySavedDailyUsage() {

        long bob = uc.createLab3AndBob();

        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), BillingTest.GB_IN_BYTES);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), BillingTest.GB_IN_BYTES);

        final Date day = new Date();
        final boolean saved = dailySummaryUsageLogger.saveDay(day);
        assertTrue(saved);

        final boolean saved2 = dailySummaryUsageLogger.saveDay(day);
        assertFalse(saved2);

    }

    @Test
    public void testDailyUsagesCalculatesCorrectly() {
        final long testTime = System.currentTimeMillis();
        final long head = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        final Long labId = uc.getLab3();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, labId).get(), BillingTest.GB_IN_BYTES);
        long file2 = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, labId).get(), 4 * BillingTest.GB_IN_BYTES);

        final long afterHours = simulateHours(testTime, 24 * getDaysInMonth(), false);

        paymentManagement.logDownloadUsage(bob, file, labId);
        paymentManagement.logPublicDownload(bob, file2);
        depositStoreCredit(labId, 30000);
        billingManagement.makeLabAccountEnterprise(head, labId);
        billingManagement.enableProcessingForLabAccount(head, labId, false);

        dailySummaryUsageLogger.saveDay(new Date(testTime));

        final ChargeableItemUsageReader.Invoice invoice = chargeableItemUsageReader.readInvoice(head, labId, new Date(testTime), new Date(afterHours));

        final Optional<DailyUsageLine> dailyUsageLine = chargeableItemUsageReader.readDailyUsageLine(labId, new Date(testTime));

        assertTrue(dailyUsageLine.isPresent());

        assertThat(invoice.total, is(dailyUsageLine.get().amount));
    }

}
