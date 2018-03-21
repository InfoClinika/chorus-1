package com.infoclinika.mssharing.model.read;

import com.infoclinika.mssharing.platform.fileserver.model.NodePath;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author andrii.loboda
 */
public interface AdvertisementReader {

    @Nullable
    AdvertisementItem readAdvertisementToDisplay(Date currentDate);

    @Nullable
    AdvertisementDetailsItem readAdvertisement(long actor, long advertisement);

    Set<AdvertisementDetailsItem> readAll(long actor);

    List<AdvertisementAttachmentItem> readAttachment(long actor, long advertisement);

    AdvertisementImageToDownload readAdvertImageFile(long actor, long advertisement);

    NodePath readPathForImageUpload(long actor, long advertisement);

    class AdvertisementItem {
        public final long id;
        public final String title;
        public final String redirectLink;
        public final byte[] imageToDisplayRef;

        public AdvertisementItem(long id, String title, String redirectLink, byte[] imageToDisplayRef) {
            this.id = id;
            this.title = title;
            this.redirectLink = redirectLink;
            this.imageToDisplayRef = imageToDisplayRef;
        }
    }

    class AdvertisementDetailsItem {
        public final long id;
        public final String title;
        public final String redirectLink;
        public final long clickedCount;
        public final long displayedCount;
        public final Date startDate;
        public final Date endDate;
        public final boolean isEnabled;

        public AdvertisementDetailsItem(long id, String title, String redirectLink, long clickedCount, long displayedCount, Date startDate, Date endDate, boolean isEnabled) {
            this.id = id;
            this.title = title;
            this.redirectLink = redirectLink;
            this.clickedCount = clickedCount;
            this.displayedCount = displayedCount;
            this.startDate = startDate;
            this.endDate = endDate;
            this.isEnabled = isEnabled;
        }
    }

    class AdvertisementAttachmentItem {
        public final long id;
        public final String name;
        public final long sizeInBytes;
        public final Date uploadDate;
        public final String imageToDisplayRef;

        public AdvertisementAttachmentItem(long id, String name, long sizeInBytes, Date uploadDate, String imageToDisplayRef) {
            this.id = id;
            this.name = name;
            this.sizeInBytes = sizeInBytes;
            this.uploadDate = uploadDate;
            this.imageToDisplayRef = imageToDisplayRef;
        }
    }

    class AdvertisementImageToDownload {
        public final String name;
        public final File file;

        public AdvertisementImageToDownload(String name, File file) {
            this.name = name;
            this.file = file;
        }
    }

}
