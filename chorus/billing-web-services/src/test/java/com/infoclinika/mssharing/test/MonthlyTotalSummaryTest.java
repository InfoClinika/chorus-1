package com.infoclinika.mssharing.test;

import com.infoclinika.mssharing.helper.AbstractBillingTest;
import com.infoclinika.mssharing.services.billing.persistence.enity.MonthlySummary;
import com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryUsageLogger;
import com.infoclinika.mssharing.services.billing.persistence.repository.MonthlySummaryRepository;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.Math.abs;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Herman Zamula
 */
public class MonthlyTotalSummaryTest extends AbstractBillingTest {

    @Inject
    private MonthlySummaryRepository monthlySummaryRepository;
    @Inject
    private MonthlySummaryUsageLogger monthlySummaryUsageLogger;

    @Test
    public void testEndMontBalanceAndMonthlyTotalAreCorrelates() {

        long bob = uc.createLab3AndBob();
        long file =
            uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 4 * GB_IN_BYTES);

        paymentManagement.logDownloadUsage(bob, file, uc.getLab3());

        monthlySummaryUsageLogger.logMonth(new Date());
        final MonthlySummary summary = monthlySummaryRepository.findAll().get(0);

        assertEquals(summary.getMonthlyTotal(), abs(summary.getEndMonthBalance()));

    }

}
