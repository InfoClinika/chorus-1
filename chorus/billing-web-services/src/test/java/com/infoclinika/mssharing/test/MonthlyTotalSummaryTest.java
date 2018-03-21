package com.infoclinika.mssharing.test;

import com.infoclinika.mssharing.helper.AbstractBillingTest;
import com.infoclinika.mssharing.services.billing.persistence.enity.MonthlySummary;
import com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryCsvSaver;
import com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryUsageLogger;
import com.infoclinika.mssharing.services.billing.persistence.repository.MonthlySummaryRepository;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.test.BillingTest.GB_IN_BYTES;
import static java.lang.Math.abs;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

/**
 * @author Herman Zamula
 */
public class MonthlyTotalSummaryTest extends AbstractBillingTest {

    @Inject
    private MonthlySummaryRepository monthlySummaryRepository;
    @Inject
    private MonthlySummaryUsageLogger monthlySummaryUsageLogger;

    @Test
    public void testMonthlySummary() {

        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 4 * GB_IN_BYTES);

        // download is free
        paymentManagement.logDownloadUsage(bob, file, uc.getLab3());

        assertTrue(monthlySummaryRepository.findAll().isEmpty());

        monthlySummaryUsageLogger.logMonth(new Date());
        final List<MonthlySummary> summaries = monthlySummaryRepository.findAll();
        Assert.assertTrue(summaries.size() == 1);
        assertEquals(summaries.get(0).getMonthlyTotal(),
                (4 * (450 * 2)) //2 translations
        );

    }

    @Test
    public void testMonthlySummaryPerLab() {

        long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 4 * GB_IN_BYTES);
        final long file2 = uc.saveFileWithSize(kate, uc.createInstrumentAndApproveIfNeeded(kate, uc.getLab2()).get(), GB_IN_BYTES);

        // download is free
        paymentManagement.logDownloadUsage(bob, file, uc.getLab3());

        // download is free
        paymentManagement.logDownloadUsage(kate, file2, uc.getLab2());

        assertTrue(monthlySummaryRepository.findAll().isEmpty());

        monthlySummaryUsageLogger.logMonth(new Date());
        final List<MonthlySummary> summaries = monthlySummaryRepository.findAll();
        Assert.assertTrue(summaries.size() == 2);

        assertEquals(from(summaries)
                .firstMatch(s -> s.getLabId() == uc.getLab3()).get().getMonthlyTotal(), 4 * 450);

        assertEquals(from(summaries)
                .firstMatch(s -> s.getLabId() == uc.getLab2()).get().getMonthlyTotal(), 450);

    }

    @Test
    public void testEndMontBalanceAndMonthlyTotalAreCorrelates() {

        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 4 * GB_IN_BYTES);

        paymentManagement.logDownloadUsage(bob, file, uc.getLab3());

        monthlySummaryUsageLogger.logMonth(new Date());
        final MonthlySummary summary = monthlySummaryRepository.findAll().get(0);

        assertEquals(summary.getMonthlyTotal(), abs(summary.getEndMonthBalance()));

    }

}
