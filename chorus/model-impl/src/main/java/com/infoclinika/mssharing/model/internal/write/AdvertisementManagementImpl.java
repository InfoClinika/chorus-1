package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.ad.Advertisement;
import com.infoclinika.mssharing.model.internal.repository.AdvertisementRepository;
import com.infoclinika.mssharing.model.internal.repository.ApplicationSettingsRepository;
import com.infoclinika.mssharing.model.write.AdvertisementManagement;
import com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;

import static com.google.common.base.Preconditions.*;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;

/**
 * @author andrii.loboda
 */
@Service
public class AdvertisementManagementImpl implements AdvertisementManagement {

    @Inject
    private AdvertisementRepository advertisementRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private ApplicationSettingsRepository applicationSettingsRepository;
    @Inject
    private StoredObjectPaths storedObjectPaths;
    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    @Override
    public long createAdvertisement(long actor, AdvertisementInfo advertisementInfo) {
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to create advertisement"
        );
        checkArgument(advertisementInfo.startDate.before(advertisementInfo.endDate));
        checkNotNull(advertisementInfo.title);
        checkNotNull(advertisementInfo.redirectLink);
        checkState(advertisementInfo.currentDate.before(advertisementInfo.endDate));
        checkNotNull(advertisementInfo.imageName);

        return advertisementRepository.save(new Advertisement(
            advertisementInfo.title,
            advertisementInfo.redirectLink,
            advertisementInfo.startDate,
            advertisementInfo.endDate,
            advertisementInfo.imageName,
            advertisementInfo.imageSize,
            advertisementInfo.isEnabled
        )).getId();
    }

    @Override
    public void updateAdvertisement(long actor, long id, AdvertisementInfo advertisementInfo) {
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to update advertisement"
        );
        final Advertisement advertisement = checkPresence(advertisementRepository.findOne(id));
        checkArgument(advertisementInfo.startDate.before(advertisementInfo.endDate));
        checkNotNull(advertisementInfo.title);
        checkNotNull(advertisementInfo.redirectLink);
        checkState(advertisementInfo.currentDate.before(advertisementInfo.endDate));
        advertisement.setTitle(advertisementInfo.title);
        advertisement.setStartRollDate(advertisementInfo.startDate);
        advertisement.setEndRollDate(advertisementInfo.endDate);
        advertisement.setRedirectLink(advertisementInfo.redirectLink);
        if (advertisementInfo.imageName != null) {
            advertisement.setImageName(advertisementInfo.imageName);
        }
        if (advertisementInfo.imageSize != 0L) {
            advertisement.setImageSize(advertisementInfo.imageSize);
        }
        advertisement.setEnabled(advertisementInfo.isEnabled);
        advertisementRepository.save(advertisement);
    }

    @Override
    public void deleteAdvertisement(long actor, long advertisement) {
        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to delete advertisement"
        );
        final Advertisement ad = advertisementRepository.findOne(advertisement);
        checkNotNull(ad);
        advertisementRepository.delete(ad);
        cloudStorageService.deleteFromCloud(new CloudStorageItemReference(
            storedObjectPaths.getRawFilesBucket(),
            ad.getImageToDisplayRef()
        ));
    }

    @Override
    public void incrementDisplayedCount(long advertisement, Date currentDate) {
        final Advertisement ad = advertisementRepository.findOne(advertisement);
        checkNotNull(ad);
        final Date endDate = ad.getEndRollDate();
        final Date startDate = ad.getStartRollDate();
        checkState(
            currentDate.before(endDate),
            "Ad %s is expired. Its endDate: %s, now: %s",
            ad.getId(),
            currentDate,
            endDate
        );
        checkState(
            currentDate.after(startDate),
            "Ad %s is not in timeframe to display. Its startDate: %s, now: %s",
            ad.getId(),
            currentDate,
            startDate
        );
        ad.setDisplayedCount(ad.getDisplayedCount() + 1);
        advertisementRepository.save(ad);
    }

    @Override
    public void incrementClickedCount(long advertisement, Date currentDate) {
        final Advertisement ad = advertisementRepository.findOne(advertisement);
        checkNotNull(ad);
        final Date endDate = ad.getEndRollDate();
        final Date startDate = ad.getStartRollDate();
        checkState(
            currentDate.before(endDate),
            "Ad %s is expired. Its endDate: %s, now: %s",
            ad.getId(),
            currentDate,
            endDate
        );
        checkState(
            currentDate.after(startDate),
            "Ad %s is not in timeframe to display. Its startDate: %s, now: %s",
            ad.getId(),
            currentDate,
            startDate
        );
        ad.setClickedCount(ad.getClickedCount() + 1);
        advertisementRepository.save(ad);
    }

    @Override
    public void specifyAdvertisementContent(long actor, long id, String contentUrl) {
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to update advertisement"
        );
        final Advertisement advertisement = checkPresence(advertisementRepository.findOne(id));
        advertisement.setImageToDisplayRef(contentUrl);
        advertisementRepository.save(advertisement);
    }

    @Override
    public long getMaxAttachmentSize() {
        return applicationSettingsRepository.findMaxSize().getValue();
    }
}
