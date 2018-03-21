package com.infoclinika.mssharing.services.billing.jobs;

import com.infoclinika.mssharing.services.billing.persistence.helper.StorageAndProcessingFeaturesUsageAnalyser;
import org.apache.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/**
 * @author timofey 21.03.16.
 */
public class AccountantJob {

    private static final String PROCESSING_FEATURE_CRON = "0 29/30 * * * *";
    private static final String STORAGE_VOLUME_FEATURE_CRON = "0 1/30 * * * *";
    private static final String ARCHIVE_STORAGE_VOLUME_FEATURE_CRON = "0 15/30 * * * *";
    private static final Logger LOGGER = Logger.getLogger(AccountantJob.class);
    private final TimeZone timeZone;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final StorageAndProcessingFeaturesUsageAnalyser storageAndProcessingFeaturesUsageAnalyser;

    public AccountantJob(StorageAndProcessingFeaturesUsageAnalyser storageAndProcessingFeaturesUsageAnalyser,
                         String timeZoneId) {
        this.storageAndProcessingFeaturesUsageAnalyser = storageAndProcessingFeaturesUsageAnalyser;
        timeZone = TimeZone.getTimeZone(ZoneId.of(timeZoneId));
    }

    @PostConstruct
    private void init() {
        scheduler.setThreadFactory(new DaemonThreadFactory());
        scheduler.initialize();
        scheduler.schedule(this::analyseProcessingUsage, new CronTrigger(PROCESSING_FEATURE_CRON, timeZone));
        scheduler.schedule(this::analyseStorageVolumeUsage, new CronTrigger(STORAGE_VOLUME_FEATURE_CRON, timeZone));
        scheduler.schedule(this::analyseArchiveStorageVolumeUsage, new CronTrigger(ARCHIVE_STORAGE_VOLUME_FEATURE_CRON, timeZone));
    }

    private void analyseProcessingUsage(){
        LOGGER.debug("Starting analyse of processing feature usage.");
        storageAndProcessingFeaturesUsageAnalyser.analyseProcessingUsage(ZonedDateTime.now(timeZone.toZoneId()).toInstant().toEpochMilli());
        LOGGER.debug("Analyse of processing feature usage is over.");
    }

    private void analyseStorageVolumeUsage(){
        LOGGER.debug("Starting analyse of storage volumes feature usage.");
        storageAndProcessingFeaturesUsageAnalyser.analyseStorageVolumeUsage(ZonedDateTime.now(timeZone.toZoneId()).toInstant().toEpochMilli());
        LOGGER.debug("Analyse of storage volumes feature usage is over.");
    }

    private void analyseArchiveStorageVolumeUsage(){
        LOGGER.debug("Starting analyse of archive storage volumes feature usage.");
        storageAndProcessingFeaturesUsageAnalyser.analyseArchiveStorageVolumeUsage(ZonedDateTime.now(timeZone.toZoneId()).toInstant().toEpochMilli());
        LOGGER.debug("Analyse of archive storage volumes feature usage is over.");
    }

}
