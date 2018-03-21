package com.infoclinika.mssharing.services.billing.jobs;

import com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryCsvToS3Saver;
import com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryUsageLogger;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageUsageRemover;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Date;
import java.util.TimeZone;

import static org.joda.time.DateTimeZone.forTimeZone;

/**
 * @author Herman Zamula
 */
public class MonthlySummaryJobs {
    private static final String SUMMARY_CRON = "0 0 6 3 * *"; //Every third day of month at 6 AM
    private static final String REMOVE_OLD_CRON = "0 30 6 * * *"; //Every day at 6:30 AM
    private static final int FULL_MONTHS_TO_SAVE = 1;

    @SuppressWarnings("all")
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    @SuppressWarnings("all")
    private final TimeZone timeZone;
    @Inject
    private MonthlySummaryUsageLogger monthlySummaryUsageLogger;
    @Inject
    private StorageUsageRemover storageUsageRemover;
    @Inject
    private MonthlySummaryCsvToS3Saver monthlySummaryCsvSaver;


    protected MonthlySummaryJobs(@Value("${billing.server.timezone}") String timeZoneID) {

        this.timeZone = TimeZone.getTimeZone(timeZoneID);

    }

    @PostConstruct
    public void init() {

        scheduler.setThreadFactory(new DaemonThreadFactory());
        scheduler.initialize();

        scheduler.schedule(() -> {
            final Date previousMonth = new DateTime(forTimeZone(timeZone)).minusMonths(1).toDate(); //Previous month
            monthlySummaryUsageLogger.logMonth(previousMonth);
            monthlySummaryCsvSaver.saveToCloud(previousMonth);
        }, new CronTrigger(SUMMARY_CRON, this.timeZone));

        scheduler.schedule(() -> {
            final Date previousMonth = new DateTime(forTimeZone(timeZone)).minusMonths(FULL_MONTHS_TO_SAVE).dayOfMonth()
                    .withMinimumValue().millisOfDay().withMinimumValue().toDate();
            storageUsageRemover.removeTillDate(previousMonth); //Remove all befor previous month
        }, new CronTrigger(REMOVE_OLD_CRON));

    }
}
