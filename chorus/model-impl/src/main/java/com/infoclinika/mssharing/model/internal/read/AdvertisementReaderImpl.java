package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.AdMediaItemNotResolvableException;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.ad.Advertisement;
import com.infoclinika.mssharing.model.internal.repository.AdvertisementRepository;
import com.infoclinika.mssharing.model.read.AdvertisementReader;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author andrii.loboda
 */
@Service
public class AdvertisementReaderImpl implements AdvertisementReader {

    @Inject
    private AdvertisementRepository advertisementRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private StoredObjectPaths storedObjectPaths;

    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdvertisementReaderImpl.class);

    @Override
    @Nullable
    public AdvertisementItem readAdvertisementToDisplay(Date currentDate) {
        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();
        final List<Advertisement> adsWithValidDate = advertisementRepository.findAdsForDate(currentDate);
        if (adsWithValidDate.isEmpty()) {
            return null;
        }
        final Advertisement randomAd = adsWithValidDate.get(new SecureRandom().nextInt(adsWithValidDate.size()));

        try {
            byte[] imageInBytes = cloudStorageService.readBytesFromCloud(new CloudStorageItemReference(
                storedObjectPaths.getRawFilesBucket(),
                randomAd.getImageToDisplayRef()
            ));

            return new AdvertisementItem(
                randomAd.getId(),
                randomAd.getTitle(),
                randomAd.getRedirectLink(),
                imageInBytes
            );
        } catch (Exception e) {
            LOGGER.warn("Error can not resolve advertisement media: " + e.getMessage());
            throw new AdMediaItemNotResolvableException(e);
        }
    }

    @Override
    @Nullable
    public AdvertisementDetailsItem readAdvertisement(long actor, long advertisement) {
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to read advertisement"
        );
        final Advertisement ad = advertisementRepository.findOne(advertisement);

        return AS_ITEM_DETAILS_TRANSFORMER.apply(ad);
    }

    @Override
    public Set<AdvertisementDetailsItem> readAll(long actor) {
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to read advertisements list"
        );
        final Iterable<Advertisement> all = advertisementRepository.findAll();

        return newHashSet(Iterables.transform(all, AS_ITEM_DETAILS_TRANSFORMER));
    }

    @Override
    public List<AdvertisementAttachmentItem> readAttachment(long actor, long advertisement) {
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to read advertisements list"
        );
        final Advertisement ad = advertisementRepository.findOne(advertisement);
        List<AdvertisementAttachmentItem> advertisementAttachmentItemList = new ArrayList<>();
        advertisementAttachmentItemList.add(ADS_ATTACHMENT_ITEM_TRANSFORMER.apply(ad));

        return advertisementAttachmentItemList;
    }

    @Override
    public AdvertisementImageToDownload readAdvertImageFile(long actor, long id) {
        final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to download advertisement image"
        );
        final Advertisement ad = advertisementRepository.findOne(id);
        File file = cloudStorageService.readFromCloud(new CloudStorageItemReference(
            storedObjectPaths.getRawFilesBucket(),
            ad.getImageToDisplayRef()
        ));

        return new AdvertisementImageToDownload(ad.getImageName(), file);
    }

    @Override
    public NodePath readPathForImageUpload(long actor, long advertisementId) {
        ValidatorPreconditions.checkAccess(
            ruleValidator.hasAdminRights(actor),
            "User has no permission to read path for advertisement image upload"
        );

        return storedObjectPaths.advertisementImagesPath(advertisementId);
    }

    private static final Function<Advertisement, AdvertisementDetailsItem> AS_ITEM_DETAILS_TRANSFORMER =
        new Function<Advertisement, AdvertisementDetailsItem>() {
            @Override
            @Nullable
            public AdvertisementDetailsItem apply(@Nullable Advertisement ad) {
                if (ad == null) {
                    return null;
                }

                return new AdvertisementDetailsItem(
                    ad.getId(),
                    ad.getTitle(),
                    ad.getRedirectLink(),
                    ad.getClickedCount(),
                    ad.getDisplayedCount(),
                    ad.getStartRollDate(),
                    ad.getEndRollDate(),
                    ad.isEnabled()
                );
            }
        };

    private static final Function<Advertisement, AdvertisementAttachmentItem> ADS_ATTACHMENT_ITEM_TRANSFORMER =
        new Function<Advertisement, AdvertisementAttachmentItem>() {
            @Override
            @Nullable
            public AdvertisementAttachmentItem apply(@Nullable Advertisement ad) {
                if (ad == null) {
                    return null;
                }

                return new AdvertisementAttachmentItem(
                    ad.getId(),
                    ad.getImageName(),
                    ad.getImageSize(),
                    ad.getLastModification(),
                    ad.getImageToDisplayRef()
                );
            }
        };

}
