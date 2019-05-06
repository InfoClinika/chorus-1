package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.Attachment;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ActionsNotAllowedException;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.read.AttachmentsReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentItem;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultAttachmentsReader<ENTITY extends Attachment, ITEM extends AttachmentItem>
    implements AttachmentsReaderTemplate<ITEM>, DefaultTransformingTemplate<ENTITY, ITEM> {

    @Inject
    protected AttachmentsReaderHelper<ENTITY, ITEM> attachmentsReaderHelper;
    @Inject
    protected RuleValidator ruleValidator;

    @PostConstruct
    private void init() {
        attachmentsReaderHelper.setTransformer(this::transform);
    }

    @Override
    public ITEM readAttachment(long actor, long attachment) {

        return attachmentsReaderHelper.readAttachment(attachment).transform();
    }

    @Override
    public List<ITEM> readAttachments(AttachmentType type, long actor, long itemId) {

        beforeReadAttachments(type, actor, itemId);

        return read(type, itemId).transform().toList();
    }

    protected void beforeReadAttachments(AttachmentType type, long actor, long item) {

        if (!ruleValidator.canUserPerformActions(actor)) {
            throw new ActionsNotAllowedException(actor);
        }

        switch (type) {

            case PROJECT:

                if (!ruleValidator.hasReadAccessOnProject(actor, item)) {
                    throw new AccessDenied("Project read restricted");
                }
                break;

            case EXPERIMENT:

                if (!ruleValidator.isUserCanReadExperiment(actor, item)) {
                    throw new AccessDenied("Can't read experiment");
                }
                break;

            default:
                throw new AssertionError(type);

        }

    }

    private ResultBuilder<ENTITY, ITEM> read(AttachmentType type, long item) {

        switch (type) {
            case PROJECT:
                return attachmentsReaderHelper.readProjectAttachments(item);
            case EXPERIMENT:
                return attachmentsReaderHelper.readExperimentAttachments(item);
            default:
                throw new AssertionError(type);
        }

    }
}
