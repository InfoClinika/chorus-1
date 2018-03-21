package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import com.infoclinika.mssharing.model.internal.repository.FileLastAccess;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.ENABLED;

/**
 * @author Herman Zamula
 */
@Service
public class ArchiverJobs {

    public static final int EVERY_FIVE_MINUTES_RATE = 1000 * 60 * 5;
    public static final int EVERY_HOUR_RATE = 1000 * 60 * 60;

    @Inject
    private FileMovingManager fileMovingManager;
    //TODO: Move this value to DB?
    @Value("${amazon.archiving.expiration.hours}")
    private int hoursToBecomeOld; //1000 Days
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    @Named("cachedFeaturesRepository")
    private FeaturesRepository featuresRepository;
    @Inject
    private FileOperationsManager fileOperationsManager;
    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;

    private static final Logger LOG = Logger.getLogger(ArchiverJobs.class);

    @Scheduled(fixedRate = EVERY_FIVE_MINUTES_RATE)
    public void checkFilesReadyToUnarchive() {

        doIfGlacierEnabled(fileOperationsManager::unarchiveMarkedFiles,
                "*** Start Check Files to Unarchive job ***");
    }

    @Scheduled(fixedRate = EVERY_FIVE_MINUTES_RATE)
    public void archiveMarkedFiles() {

        doIfGlacierEnabled(fileOperationsManager::archiveMarkedFiles,
                "*** Archive marked files job started ***");
    }

    @Scheduled(fixedRate = EVERY_FIVE_MINUTES_RATE)
    public void processExpiredUnarchivedFiles() {

        doIfGlacierEnabled(fileMovingManager::moveToArchiveExpiredUnarchivedFiles,
                "*** Move to archive expired unarchived files job started ***");

    }

    @Scheduled(fixedRate = EVERY_HOUR_RATE)
    public void checkFilesIsOldEnough() {
        if (isFeatureEnabled(ApplicationFeature.BILLING.getFeatureName()) && isFeatureEnabled(ApplicationFeature.GLACIER.getFeatureName())) {

            Iterable<FileLastAccess> metaDatas = fileMetaDataRepository.findLastAccessForAll();
            final Set<Long> enterpriseLabIds = labPaymentAccountRepository.findEnterprise().stream()
                    .map(a -> a.getLab().getId())
                    .collect(Collectors.toSet());

            for (FileLastAccess data : metaDatas) {
                try {
                    if (data.contentId != null && isFileOldEnough(data.lastAccess) && enterpriseLabIds.contains(data.lab)) {
                        fileMovingManager.moveToArchiveStorage(data.id);
                    }
                } catch (Exception e) {
                    LOG.error("Checking for file: " + data.id + " is failed.", e);
                }
            }
        }
    }

    private boolean isFeatureEnabled(String feature) {
        return featuresRepository.get().get(feature).getEnabledState().equals(ENABLED);
    }

    private void doIfGlacierEnabled(ActionExecutor function, String enabledMessage) {

        if (isFeatureEnabled(ApplicationFeature.GLACIER.getFeatureName())) {
            LOG.debug(enabledMessage);
            function.execute();
        } else {
            LOG.debug("Ignoring action: " + enabledMessage);
        }
    }

    private boolean isFileOldEnough(Date date) {
        final long diffHours = Math.abs(date.getTime() - new Date().getTime()) / (1000 * 60 * 60); //hours
        return diffHours >= hoursToBecomeOld;
    }

    private interface ActionExecutor {
        void execute();
    }

}
