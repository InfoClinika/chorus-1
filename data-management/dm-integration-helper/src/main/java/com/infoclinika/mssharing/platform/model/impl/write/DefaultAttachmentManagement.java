package com.infoclinika.mssharing.platform.model.impl.write;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.Attachment;
import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.write.AttachmentsManager;
import com.infoclinika.mssharing.platform.model.write.AttachmentManagementTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;

/**
 * @author Herman Zamula
 */
@Transactional
@Component
public class DefaultAttachmentManagement<ATTACHMENT extends Attachment> implements AttachmentManagementTemplate {

    @Inject
    protected RuleValidator ruleValidator;
    @Inject
    protected AttachmentsManager<ATTACHMENT> attachmentsManager;
    @Inject
    protected StoredObjectPathsTemplate storedObjectPaths;

    protected NodePath projectAttachmentPath(ATTACHMENT attachment) {
        return storedObjectPaths.projectAttachmentPath(attachment.getOwner().getId(), attachment.getId());
    }

    protected NodePath experimentAttachmentPath(ATTACHMENT attachment) {
        return storedObjectPaths.experimentAttachmentPath(attachment.getOwner().getId(), attachment.getId());
    }


    @Override
    public long newAttachment(long actor, String fileName, long sizeInBytes) {

        return attachmentsManager
                .createAttachment(actor, fileName, sizeInBytes)
                .getId();

    }

    @Override
    public long copyAttachment(long originId, long actor, boolean isProject) {

        return attachmentsManager
                .copyAttachment(originId, actor, getNodePathFn(isProject))
                .getId();

    }

    @Override
    public void discardAttachment(long actor, long attachment) {

        beforeDiscardAttachment(actor, attachment);

        attachmentsManager.removeAttachment(attachment);

    }

    @Override
    public void updateExperimentAttachments(long actor, long experiment, Iterable<Long> attachments) {

        beforeUpdateExperimentAttachments(actor, experiment);

        attachmentsManager.updateExperimentAttachments(experiment, attachments);

    }

    @Override
    public void updateProjectAttachments(long actor, long project, Iterable<Long> attachments) {

        beforeUpdateProjectAttachments(actor, project);

        attachmentsManager.updateProjectAttachments(project, attachments);

    }

    private void beforeDiscardAttachment(long actor, long attachment) {

        if (!ruleValidator.canModifyAttachment(actor, attachment)) {
            throw new AccessDenied("User cannot discard the attachment. User ID = " + actor + ". Attachment ID = " + attachment);
        }

    }

    protected void beforeUpdateExperimentAttachments(long actor, long experiment) {

        checkAccess(ruleValidator.userHasEditPermissionsOnExperiment(actor, experiment),
                "User cannot edit the attachments for the experiment. User ID = " + actor + ". Experiment ID = " + experiment);


    }


    protected void beforeUpdateProjectAttachments(long actor, long project) {

        if (!ruleValidator.hasWriteAccessOnProject(actor, project)) {
            throw new AccessDenied("User cannot edit the attachments for the project. User ID = " + actor + ". Project ID = " + project);
        }

    }

    private Function<ATTACHMENT, NodePath> getNodePathFn(boolean isProject) {
        return isProject ? new Function<ATTACHMENT, NodePath>() {
            @Override
            public NodePath apply(ATTACHMENT input) {
                return projectAttachmentPath(input);
            }
        } : new Function<ATTACHMENT, NodePath>() {
            @Override
            public NodePath apply(ATTACHMENT input) {
                return experimentAttachmentPath(input);
            }
        };
    }


}
