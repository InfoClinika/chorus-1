package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.internal.helper.ExportedFilesCleaner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author timofei.kasianov 6/25/18
 */
@Service
public class ExportedFilesCleanerJob {

    @Inject
    private ExportedFilesCleaner exportedFilesCleaner;

    /**
     * Scheduled task to remove old exported files.
     * Default cron rate - one day (24 * 60 * 60 * 1000).
     */
    @Scheduled(fixedRateString = "${scheduled.exported.files.removal.rate:86400000}")
    public void removeOldExportedDataCubes() {
        exportedFilesCleaner.removeOldFiles();
    }

}
