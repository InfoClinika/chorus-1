package com.infoclinika.mssharing.platform.fileserver;

import com.google.common.base.Joiner;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;

import static com.google.common.base.Preconditions.checkArgument;
import static com.infoclinika.mssharing.platform.fileserver.StorageService.DELIMITER;

/**
 * @author Herman Zamula
 */
public class StoredObjectPathsTemplate {

    private String rawFilesPrefix;
    private String experimentsAttachmentsPrefix;
    private String projectAttachmentsPrefix;

    public StoredObjectPathsTemplate() {
    }

    private static String checkHasNowDelimiter(String toCheck) {
        checkArgument(!toCheck.contains(DELIMITER));
        return toCheck;
    }

    public NodePath rawFilePath(long user, long instrumentId, String fileName) {
        return new NodePath(Joiner.on(DELIMITER)
            .join(rawFilesPrefix, user, instrumentId, checkHasNowDelimiter(fileName)));
    }

    public NodePath experimentAttachmentPath(long user, long attachmentId) {
        return new NodePath(Joiner.on(DELIMITER).join(experimentsAttachmentsPrefix, user, attachmentId));
    }

    public NodePath projectAttachmentPath(long user, long attachmentId) {
        return new NodePath(Joiner.on(DELIMITER).join(projectAttachmentsPrefix, user, attachmentId));
    }

    public String getRawFilesPrefix() {
        return rawFilesPrefix;
    }

    public void setRawFilesPrefix(String rawFilesPrefix) {
        this.rawFilesPrefix = rawFilesPrefix;
    }

    public String getExperimentsAttachmentsPrefix() {
        return experimentsAttachmentsPrefix;
    }

    public void setExperimentsAttachmentsPrefix(String experimentsAttachmentsPrefix) {
        this.experimentsAttachmentsPrefix = experimentsAttachmentsPrefix;
    }

    public String getProjectAttachmentsPrefix() {
        return projectAttachmentsPrefix;
    }

    public void setProjectAttachmentsPrefix(String projectAttachmentsPrefix) {
        this.projectAttachmentsPrefix = projectAttachmentsPrefix;
    }
}
