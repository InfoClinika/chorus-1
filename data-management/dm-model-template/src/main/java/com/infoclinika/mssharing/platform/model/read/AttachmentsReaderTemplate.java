package com.infoclinika.mssharing.platform.model.read;

import java.util.Date;
import java.util.List;

/**
 * @author Herman Zamula
 */
public interface AttachmentsReaderTemplate<ATTACHMENT extends AttachmentsReaderTemplate.AttachmentItem> {

    ATTACHMENT readAttachment(long actor, long attachment);

    List<ATTACHMENT> readAttachments(AttachmentType type, long actor, long itemId);

    enum AttachmentType {
        PROJECT,
        EXPERIMENT
    }

    class AttachmentItem {
        public final long id;
        public final String name;
        public final long sizeInBytes;
        public final Date uploadDate;
        public final long ownerId;

        public AttachmentItem(long id, String name, long sizeInBytes, Date uploadDate, long ownerId) {
            this.id = id;
            this.name = name;
            this.sizeInBytes = sizeInBytes;
            this.uploadDate = uploadDate;
            this.ownerId = ownerId;
        }
    }
}
