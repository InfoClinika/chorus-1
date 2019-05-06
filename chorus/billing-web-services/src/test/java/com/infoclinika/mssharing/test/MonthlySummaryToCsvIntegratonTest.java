package com.infoclinika.mssharing.test;

import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryCsvSaver;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.rest.api.model.HistoryForMonthReference;
import org.joda.time.DateTime;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.File;
import java.util.Date;
import java.util.Optional;

import static com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryCsvSaver.monthFormat;
import static com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryCsvSaver.SUMMARY_CSV;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Herman Zamula
 */
@Configuration
@ImportResource({
    "mysql.cfg.xml",
    "persistence.cfg.xml",
    "billing-persistence.cfg.xml",
    "billing-mysql.cfg.xml",
    "test.cfg.xml"
})
@ComponentScan(basePackages = {
    "com.infoclinika.mssharing.services.billing.persistence",
    "com.infoclinika.mssharing.model",
    "com.infoclinika.mssharing.platform",
    "com.infoclinika.mssharing.propertiesprovider"},
    excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.*DefaultRuleValidator*")})
@ContextConfiguration(classes = MonthlySummaryToCsvIntegratonTest.class)
public class MonthlySummaryToCsvIntegratonTest extends AbstractTestNGSpringContextTests {

    @Inject
    private MonthlySummaryCsvSaver monthlySummaryCsvSaver;

    @Inject
    private ChargeableItemUsageReader chargeableItemUsageReader;

    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;

    /**
     * Using the REAL data from mysql source
     */
    @Test(enabled = false)
    public void testSummarySaved() {

        final Date month = new DateTime().minusMonths(1).toDate();

        final File file = monthlySummaryCsvSaver.saveUsagesToCsv(month);

        assertTrue(file.list().length > 0);

        Optional<File> firstLabDir = asList(requireNonNull(file.listFiles())).stream()
            .findFirst();

        assertTrue(firstLabDir.isPresent());

        Optional<File> loggedMonthDir = firstLabDir.map(
            (dir) -> asList(dir.listFiles((dir1, name) -> name.equals(monthFormat.format(month)))).stream()
                .findFirst()).get();

        assertTrue(loggedMonthDir.isPresent());

        Optional<File> summaryCsv = loggedMonthDir.map(
            (monthDir) -> asList(monthDir.listFiles((dir1, name) -> name.equals(SUMMARY_CSV))).stream().findFirst())
            .get();

        assertTrue(summaryCsv.isPresent());

        assertTrue(summaryCsv.get().length() > 0);

    }

    @Test(enabled = false)
    public void testReadingMonthlySummaryForLab() {

        final Date month = new DateTime().minusMonths(1).toDate();

        final HistoryForMonthReference reference = chargeableItemUsageReader.readMonthReference(-1, 6, month);

        assertEquals(
            reference.csvDataReference,
            billingPropertiesProvider.getBillingPrefix() + "/6/" + monthFormat.format(month)
        );

    }


}
