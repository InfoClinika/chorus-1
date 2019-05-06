package com.infoclinika.mssharing.model.write;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author andrii.loboda
 */
@Transactional
public interface AdvertisementManagement {

    long createAdvertisement(long actor, AdvertisementInfo advertisementInfo);

    void updateAdvertisement(long actor, long advertisementId, AdvertisementInfo advertisementInfo);

    void deleteAdvertisement(long actor, long advertisement);

    void incrementDisplayedCount(long advertisement, Date currentDate);

    void incrementClickedCount(long advertisement, Date currentDate);

    void specifyAdvertisementContent(long actor, long advertisement, String contentUrl);

    long getMaxAttachmentSize();

    public static class AdvertisementInfo {
        public final String title;
        public final Date startDate;
        public final Date endDate;
        public final String redirectLink;
        public final Date currentDate;
        public final String imageName;
        public final long imageSize;
        public final boolean isEnabled;

        public AdvertisementInfo(String title, Date startDate, Date endDate, String redirectLink, Date currentDate,
                                 String imageName, long imageSize, boolean isEnabled) {
            this.title = title;
            this.startDate = startDate;
            this.endDate = endDate;
            this.redirectLink = redirectLink;
            this.currentDate = currentDate;
            this.imageName = imageName;
            this.imageSize = imageSize;
            this.isEnabled = isEnabled;
        }
    }

}
