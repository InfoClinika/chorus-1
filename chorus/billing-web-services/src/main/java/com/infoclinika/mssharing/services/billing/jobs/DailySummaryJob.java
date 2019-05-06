package com.infoclinika.mssharing.services.billing.jobs;

import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import com.infoclinika.mssharing.services.billing.persistence.helper.AnalysableStorageLogHelper;
import com.infoclinika.mssharing.services.billing.persistence.helper.ArchiveStorageLogHelper;
import com.infoclinika.mssharing.services.billing.persistence.helper.DailySummaryUsageLogger;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageLogHelper;
import com.infoclinika.mssharing.services.billing.persistence.repository.HourlyAnalyseStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.HourlyArchiveStorageUsageRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.joda.time.DateTimeZone.forTimeZone;
import static org.joda.time.Days.daysBetween;

/**
 * @author timofei.kasianov 7/4/18
 */
public class DailySummaryJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailySummaryJob.class);
    private static final String SUM_ANALYSABLE_LOGS_CRON_EXPRESSION = "0 30 3 * * *";//Every day at 3:30 AM
    private static final String SUM_ARCHIVE_LOGS_CRON_EXPRESSION = "0 0 4 * * *";//Every day at 4:00 AM
    private static final String DAILY_LOGS_CRON_EXPRESSION = "0 30 4 * * *";//Every day at 4:30 AM
    private static final String MISSED_ANALYSABLE_SUM_LOGS_CRON_EXPRESSION = "0 0 5 * * *";//Every day at 5:00 AM
    private static final String MISSED_ARCHIVE_SUM_LOGS_CRON_EXPRESSION = "0 30 5 * * *";//Every day at 5:30 AM
    private static final int MISSED_LOGS_HANDLING_DAYS_LIMIT = 3;

    @Inject
    private DailySummaryUsageLogger dailySummaryUsageLogger;

    @Inject
    private HourlyAnalyseStorageUsageRepository hourlyAnalyseStorageUsageRepository;

    @Inject
    private AnalysableStorageLogHelper analysableStorageLogHelper;

    @Inject
    private HourlyArchiveStorageUsageRepository hourlyArchiveStorageUsageRepository;

    @Inject
    private ArchiveStorageLogHelper archiveStorageLogHelper;

    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;

    private TimeZone timeZone = null;

    @PostConstruct
    @SuppressWarnings("FutureReturnValueIgnored")
    private void init() {
        timeZone = TimeZone.getTimeZone(billingPropertiesProvider.getTimeZoneId());
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadFactory(new DaemonThreadFactory());
        scheduler.initialize();
        scheduler.schedule(this::sumAnalysableLogs, new CronTrigger(SUM_ANALYSABLE_LOGS_CRON_EXPRESSION, timeZone));
        scheduler.schedule(this::sumArchiveLogs, new CronTrigger(SUM_ARCHIVE_LOGS_CRON_EXPRESSION, timeZone));
        scheduler.schedule(this::saveDailyUsages, new CronTrigger(DAILY_LOGS_CRON_EXPRESSION, timeZone));
        scheduler.schedule(
            this::handleAnalysableMissedLogsCompressing,
            new CronTrigger(MISSED_ANALYSABLE_SUM_LOGS_CRON_EXPRESSION, timeZone)
        );
        scheduler.schedule(
            this::handleArchiveMissedLogsCompressing,
            new CronTrigger(MISSED_ARCHIVE_SUM_LOGS_CRON_EXPRESSION, timeZone)
        );
    }

    private void sumAnalysableLogs() {
        final Date dayToLog = getYesterday();
        try {
            LOGGER.info("*** Start summarizing analysable storage usages for {}", dayToLog);
            doLogsCompressing(dayToLog, analysableStorageLogHelper);
        } catch (Exception e) {
            LOGGER.error("*** Error occurred while summarizing analysable storage usages for {}", dayToLog, e);
        }
    }

    private void sumArchiveLogs() {
        final Date dayToLog = getYesterday();
        try {
            LOGGER.info("*** Start summarizing archive storage usages for {}", dayToLog);
            doLogsCompressing(dayToLog, archiveStorageLogHelper);
        } catch (Exception e) {
            LOGGER.error("*** Error occurred while summarizing archive storage usages for {}", dayToLog, e);
        }
    }

    private void saveDailyUsages() {
        final Date dayToLog = new DateTime().minusDays(1).toDate();
        saveDailyUsages(dayToLog);
    }

    private void saveDailyUsages(Date dayToLog) {
        try {
            LOGGER.info("*** Start saving daily storage usages for {}", dayToLog);
            dailySummaryUsageLogger.saveDay(dayToLog);
        } catch (Exception e) {
            LOGGER.error("*** Error occurred while saving daily storage usages for {}", dayToLog, e);
        }
    }

    private void handleAnalysableMissedLogsCompressing() {

        final Date yesterday = getYesterday();
        final int sinceEpoch = daysSinceEpoch(yesterday);

        try {

            LOGGER.info("*** Start handling uncompressed analysable storage usage logs.");

            final List<Date> datesForAnalysable =
                hourlyAnalyseStorageUsageRepository.datesWhereSumLogsWereMissed(sinceEpoch);

            datesForAnalysable.stream().limit(MISSED_LOGS_HANDLING_DAYS_LIMIT).forEach(date -> {
                LOGGER.info("*** Start compressing analysable storage usage logs for {}", date);
                doLogsCompressing(date, analysableStorageLogHelper);
                saveDailyUsages(date);
            });
        } catch (Exception e) {
            LOGGER.error("*** Error occurred while handling uncompressed analysable storage usage logs.");
        }
    }

    private void handleArchiveMissedLogsCompressing() {

        final Date yesterday = getYesterday();
        final int sinceEpoch = daysSinceEpoch(yesterday);

        try {

            LOGGER.info("*** Start handling uncompressed archive storage usage logs.");

            final List<Date> datesForArchive =
                hourlyArchiveStorageUsageRepository.datesWhereSumLogsWereMissed(sinceEpoch);

            datesForArchive.stream().limit(MISSED_LOGS_HANDLING_DAYS_LIMIT).forEach(date -> {
                LOGGER.info("*** Start compressing archive storage usage logs for {}", date);
                doLogsCompressing(date, archiveStorageLogHelper);
                saveDailyUsages(date);
            });
        } catch (Exception e) {
            LOGGER.error("*** Error occurred while handling uncompressed archive storage usage logs.");
        }
    }

    private void doLogsCompressing(Date dayToLog, StorageLogHelper storageLogHelper) {
        storageLogHelper.sumLogs(dayToLog);
    }

    private int daysSinceEpoch(Date timestamp) {
        return daysBetween(new DateTime(0).withZone(forTimeZone(timeZone)), new DateTime(timestamp)).getDays();
    }

    private Date getYesterday() {
        return new DateTime(new Date()).withZone(forTimeZone(timeZone)).minusDays(1).toDate();
    }
}
