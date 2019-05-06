package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.internal.repository.FileLastAccess;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.features.ApplicationFeature.BILLING;
import static com.infoclinika.mssharing.model.features.ApplicationFeature.GLACIER;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Herman Zamula
 */
@Service
public class ArchiverJobs {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiverJobs.class);

    private final FileMovingManager fileMovingManager;
    private final FileMetaDataRepository fileMetaDataRepository;
    private final FileOperationsManager fileOperationsManager;
    private final FeaturesHelper featuresHelper;
    private final BillingFeaturesHelper billingFeaturesHelper;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public ArchiverJobs(FileMovingManager fileMovingManager,
                        FileMetaDataRepository fileMetaDataRepository,
                        FileOperationsManager fileOperationsManager,
                        FeaturesHelper featuresHelper,
                        BillingFeaturesHelper billingFeaturesHelper,
                        AmazonPropertiesProvider amazonPropertiesProvider) {
        this.fileMovingManager = fileMovingManager;
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.fileOperationsManager = fileOperationsManager;
        this.featuresHelper = featuresHelper;
        this.billingFeaturesHelper = billingFeaturesHelper;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    /**
     * Scheduled task to check files ready to unarchive.
     * Default cron rate - five minutes.
     */
    @Scheduled(fixedRateString = "${scheduled.check.files.to.unarchive.rate:300000}")
    public void checkFilesReadyToUnarchive() {

        doIfGlacierEnabled(
            fileOperationsManager::unarchiveMarkedFiles,
            "*** Start Check Files to Unarchive job ***"
        );
    }

    /**
     * Scheduled task to archive marked files.
     * Default cron rate - five minutes.
     */
    @Scheduled(fixedRateString = "${scheduled.marked.files.archivation.rate:300000}")
    public void archiveMarkedFiles() {

        doIfGlacierEnabled(
            fileOperationsManager::archiveMarkedFiles,
            "*** Archive marked files job started ***"
        );
    }

    /**
     * Scheduled task to process expired unarchived files.
     * Default cron rate - five minutes.
     */
    @Scheduled(fixedRateString = "${scheduled.expired.files.processing.rate:300000}")
    public void processExpiredUnarchivedFiles() {

        doIfGlacierEnabled(
            fileMovingManager::moveToArchiveExpiredUnarchivedFiles,
            "*** Move to archive expired unarchived files job started ***"
        );

    }

    /**
     * Scheduled task to check files, that are old enough to be archived.
     * Default cron rate - one hour.
     */
    @Scheduled(fixedRateString = "${scheduled.check.files.to.archive.rate:3600000}")
    public void checkFilesAreOldEnough() {
        if (featuresHelper.isEnabled(GLACIER)) {

            final List<FileLastAccess> fileLastAccessList = fileMetaDataRepository.findLastAccessForAll();
            final List<FileLastAccess> filesToBeArchived;

            if (featuresHelper.isEnabled(BILLING)) {
                filesToBeArchived = fileLastAccessList
                    .stream()
                    .filter(f -> isNotEmpty(f.contentId) && isFileOldEnough(f.lastAccess) &&
                        billingFeaturesHelper.isFeatureEnabled(f.lab, BillingFeature.ARCHIVE_STORAGE))
                    .collect(Collectors.toList());
            } else {
                filesToBeArchived = fileLastAccessList
                    .stream()
                    .filter(f -> isNotEmpty(f.contentId) && isFileOldEnough(f.lastAccess))
                    .collect(Collectors.toList());
            }

            filesToBeArchived.forEach(f -> {
                try {
                    fileMovingManager.moveToArchiveStorage(f.id);
                } catch (Exception e) {
                    LOGGER.error("Couldn't move file to archive storage. File ID: {}", f.id);
                }
            });
        }
    }

    private void doIfGlacierEnabled(ActionExecutor function, String enabledMessage) {

        if (featuresHelper.isEnabled(GLACIER)) {
            LOGGER.debug(enabledMessage);
            function.execute();
        } else {
            LOGGER.debug("Ignoring action: {}", enabledMessage);
        }
    }

    private boolean isFileOldEnough(Date date) {
        final long diffHours = Math.abs(date.getTime() - new Date().getTime()) / (1000 * 60 * 60); //hours

        return diffHours >= amazonPropertiesProvider.getArchivingExpirationHours();
    }

    private interface ActionExecutor {
        void execute();
    }

}
