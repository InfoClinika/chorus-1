package com.infoclinika.mssharing.platform.model.write;

/**
 * @author Herman Zamula
 */
public interface AttachmentManagementTemplate {

    long newAttachment(long actor, String fileName, long sizeInBytes);

    void updateExperimentAttachments(long actor, long experiment, Iterable<Long> attachments);

    void updateProjectAttachments(long actor, long project, Iterable<Long> attachments);

    void discardAttachment(long actor, long attachment);

    long copyAttachment(long originId, long actor, boolean isProject);
}
