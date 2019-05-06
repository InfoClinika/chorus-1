package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.write.FileOperationsManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Vladislav Kovchug
 */
@Service
public class HandleUnsuccessfullyUploadedFilesJob {
    private final FileOperationsManager fileOperationsManager;

    @Inject
    public HandleUnsuccessfullyUploadedFilesJob(FileOperationsManager fileOperationsManager) {
        this.fileOperationsManager = fileOperationsManager;
    }

    /**
     * Scheduled task to mark unsuccessfully uploaded files as `toReplace`.
     * Default cron rate - two hours.
     */
    @Scheduled(fixedRateString = "${scheduled.mark.failed.files.to.replace.rate:7200000}")
    public void handleUnsuccessfullyUploadedFiles() {
        fileOperationsManager.markUnsuccessfullyUploadedFilesToReplaceAndCorrupted();
    }

}
