package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.Attachment;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentItem;
import com.infoclinika.mssharing.platform.repository.AttachmentRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class AttachmentsReaderHelper<ENTITY extends Attachment, LINE extends AttachmentItem>
    extends AbstractReaderHelper<ENTITY, LINE, AttachmentItem> {

    @Inject
    private AttachmentRepositoryTemplate<ENTITY> attachmentRepository;

    public AttachmentsReaderHelper() {
    }

    @Override
    public Function<ENTITY, AttachmentItem> getDefaultTransformer() {
        //noinspection unchecked
        return input -> new AttachmentItem(
            input.getId(),
            input.getName(),
            input.getSizeInBytes(),
            input.getUploadDate(),
            input.getOwner().getId()
        );
    }


    public SingleResultBuilder<ENTITY, LINE> readAttachment(long attachment) {
        return SingleResultBuilder.builder(attachmentRepository.findOne(attachment), getTransformer());
    }

    public ResultBuilder<ENTITY, LINE> readProjectAttachments(long project) {
        return ResultBuilder.builder(attachmentRepository.findByProject(project), getTransformer());
    }

    public ResultBuilder<ENTITY, LINE> readExperimentAttachments(long experiment) {
        return ResultBuilder.builder(attachmentRepository.findByExperiment(experiment), getTransformer());
    }
}
