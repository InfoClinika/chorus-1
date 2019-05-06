package com.infoclinika.mssharing.services.billing.jobs;

import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import com.infoclinika.mssharing.services.billing.persistence.helper.AnalysableStorageLogHelper;
import com.infoclinika.mssharing.services.billing.persistence.helper.ArchiveStorageLogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.TimeZone;

/**
 * @author Herman Zamula
 */
public class StorageUsageLoggerJob {

    private static final String LOG_ANALYSABLE_STORAGE_USAGE_CRON_EXPRESSION = "0 30 1 * * *"; //Every day at 1:30 AM
    private static final String LOG_ARCHIVE_STORAGE_USAGE_CRON_EXPRESSION = "0 0 1 * * *"; //Every day at 1:00 AM
    private static final int LOG_INTERVAL = 60 * 60 * 1000; //1h
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageUsageLoggerJob.class);

    @Inject
    private AnalysableStorageLogHelper analysableStorageLogHelper;

    @Inject
    private ArchiveStorageLogHelper archiveStorageLogHelper;

    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;

    @PostConstruct
    @SuppressWarnings("FutureReturnValueIgnored")
    private void init() {
        final TimeZone timeZone = TimeZone.getTimeZone(billingPropertiesProvider.getTimeZoneId());
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadFactory(new DaemonThreadFactory());
        scheduler.initialize();
        scheduler.schedule(this::logAnalysableStorageUsage,
            new CronTrigger(LOG_ANALYSABLE_STORAGE_USAGE_CRON_EXPRESSION, timeZone)
        );
        scheduler.schedule(this::logArchiveStorageUsage,
            new CronTrigger(LOG_ARCHIVE_STORAGE_USAGE_CRON_EXPRESSION, timeZone)
        );
    }

    private void logAnalysableStorageUsage() {
        try {
            LOGGER.info("*** Start logging analysable storage usage.");
            analysableStorageLogHelper.log(LOG_INTERVAL);
        } catch (Exception e) {
            LOGGER.error("*** Error occurred while logging analysable storage usage", e);
        }
    }

    private void logArchiveStorageUsage() {
        try {
            LOGGER.info("*** Start logging archive storage usage.");
            archiveStorageLogHelper.log(LOG_INTERVAL);
        } catch (Exception e) {
            LOGGER.error("*** Error occurred while logging archive storage usage", e);
        }
    }

}
