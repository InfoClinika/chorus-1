package com.infoclinika.mssharing.services.billing.jobs;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import com.infoclinika.mssharing.services.billing.persistence.helper.DailySummaryUsageLogger;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageLogHelper;
import com.infoclinika.mssharing.services.billing.persistence.repository.StorageFeatureUsageRepository;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.joda.time.DateTimeZone.forTimeZone;
import static org.joda.time.Days.daysBetween;

/**
 * @author Herman Zamula
 */
public class StorageUsageLoggerJob {

    public static final String LOG_CRON_EXPRESSION = "1 0 * * * *"; //Every hour starting after one second delay
    public static final String SUM_LOGS_CRON_EXPRESSION = "0 30 4 * * *";//Every day at 4:30 AM
    public static final String DAILY_LOGS_CRON_EXPRESSION = "0 55 4 * * *";//Every day at 4:55 AM
    public static final String MISSED_SUM_LOGS_CRON_EXPRESSION = "0 30 5 * * *";//Every day at 5:30 AM

    public static final int MISSED_LOGS_HANDLING_DAYS_LIMIT = 3;

    public static final int LOG_INTERVAL = 60 * 60 * 1000; //1h
    private static final Logger LOGGER = Logger.getLogger(StorageUsageLoggerJob.class);

    @SuppressWarnings("all")
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    @SuppressWarnings("all")
    private final TimeZone timeZone;

    private FeaturesRepository featuresRepository;
    private final StorageFeatureUsageRepository<?> usageRepository;
    private StorageLogHelper storageLogHelper;
    @Inject
    private DailySummaryUsageLogger dailySummaryUsageLogger;


    private final Function<Date, String> dateToString = new Function<Date, String>() {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        @Override
        public String apply(Date input) {
            return dateFormat.format(input);
        }

    };

    protected StorageUsageLoggerJob(FeaturesRepository featuresRepository,
                                    StorageFeatureUsageRepository usageRepository,
                                    StorageLogHelper storageLogHelper,
                                    String timeZoneID) {

        this.featuresRepository = featuresRepository;
        this.usageRepository = usageRepository;
        this.storageLogHelper = storageLogHelper;
        this.timeZone = TimeZone.getTimeZone(timeZoneID);

        scheduler.setThreadFactory(new DaemonThreadFactory());
        scheduler.initialize();

        scheduler.schedule(this::log, new CronTrigger(LOG_CRON_EXPRESSION, this.timeZone));
        scheduler.schedule(this::sumLogs, new CronTrigger(SUM_LOGS_CRON_EXPRESSION, this.timeZone));
        scheduler.schedule(this::saveDailyUsages, new CronTrigger(DAILY_LOGS_CRON_EXPRESSION, this.timeZone));
        scheduler.schedule(this::handleMissedLogsCompressing, new CronTrigger(MISSED_SUM_LOGS_CRON_EXPRESSION, this.timeZone));

    }

    public void log() {
        try {
            LOGGER.info("*** Start logging storage feature usage.");
            storageLogHelper.log(LOG_INTERVAL);
            LOGGER.info("Logging storage usage were completed.");
        } catch (Exception e) {
            LOGGER.error("*** Error occurred when log storage usage: " + e.getMessage(), e);
        }
    }

    public void saveDailyUsages() {
        final Date dayToLog = new DateTime().minusDays(1).toDate();
        saveDailyUsages(dayToLog);
    }

    private void saveDailyUsages(Date dayToLog) {
        try {
            dailySummaryUsageLogger.saveDay(dayToLog);
        } catch (Exception e) {
            LOGGER.error("*** Error occurred when saving daily usages: " + e.getMessage(), e);
        }
    }

    public void sumLogs() {
        try {
            final Date dayToLog = getYesterday();
            doLogsCompressing(dayToLog);
        } catch (Exception e) {
            LOGGER.error("*** Error occurred when sum storage usage logs: " + e.getMessage(), e);
        }
    }

    protected Date getYesterday() {
        return new DateTime(new Date()).withZone(forTimeZone(timeZone)).minusDays(1).toDate();
    }

    public void handleMissedLogsCompressing() {
        LOGGER.debug("** Start handle missed logs compressing.");
        final List<Date> dates = usageRepository.datesWhereSumLogsWereMissed(daysSinceEpoch(getYesterday()));
        LOGGER.debug("** Count of days have missed logs compressing: " + dates.size());
        LOGGER.debug("** Next days have missed logs compressing: " + from(dates).transform(dateToString));

        final FluentIterable<Date> datesToHandle = from(dates).limit(MISSED_LOGS_HANDLING_DAYS_LIMIT);
        LOGGER.debug("** Next days with missed logs compressing will be handle: " + datesToHandle.transform(dateToString));

        for (Date date : datesToHandle) {
            doLogsCompressing(date);
            saveDailyUsages(date);
        }

        LOGGER.debug("** Missed logs compressing has been completed for dates: " + from(datesToHandle).transform(dateToString));
    }

    protected void doLogsCompressing(Date dayToLog) {
        try {
            LOGGER.info(format("*** Start sum logged storage feature usage for day %s.", dayToLog));
            storageLogHelper.sumLogs(dayToLog);
            LOGGER.info(format("Sum log storage table was completed for day %s.", dayToLog));

        } catch (Exception e) {
            LOGGER.error("*** Error occurred when sum storage usage logs for day + " + dayToLog + " : " + e.getMessage(), e);
        }
    }

    protected int daysSinceEpoch(Date timestamp) {
        return daysBetween(new DateTime(0).withZone(forTimeZone(timeZone)), new DateTime(timestamp)).getDays();
    }


}
