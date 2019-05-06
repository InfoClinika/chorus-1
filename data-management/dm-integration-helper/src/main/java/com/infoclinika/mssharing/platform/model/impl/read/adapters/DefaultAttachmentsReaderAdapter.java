package com.infoclinika.mssharing.platform.model.impl.read.adapters;

import com.infoclinika.mssharing.platform.entity.Attachment;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultAttachmentsReader;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentItem;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class DefaultAttachmentsReaderAdapter extends DefaultAttachmentsReader<Attachment, AttachmentItem> {
    @Override
    public AttachmentItem transform(Attachment attachment) {
        return attachmentsReaderHelper.getDefaultTransformer().apply(attachment);
    }
}
