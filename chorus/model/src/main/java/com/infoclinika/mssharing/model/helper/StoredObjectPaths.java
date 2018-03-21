/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.helper;

import com.google.common.base.Joiner;
import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static com.infoclinika.mssharing.platform.fileserver.StorageService.DELIMITER;

/**
 * @author Oleksii Tymchenko
 */
@Service
public class StoredObjectPaths extends StoredObjectPathsTemplate {

    @Value("${raw.files.temp.folder}")
    private String tempRawFilesPrefix;

    @Value("${raw.files.temp.ftp.folder}")
    private String tempFilesFtpPrefix;

    @Value("${experiment.annotation.target.folder}")
    private String experimentsAnnotationsPrefix;

    @Value("${protein.attachments.target.folder}")
    private String processingRunPluginAttachmentPrefix;

    @Value("${protein.search.attachments.target.folder}")
    private String proteinSearchAttachmentPrefix;

    @Value("${protein.dbs.target.folder}")
    private String proteinDatabasesPrefix;

    @Value("${unfinished.chunks.target.folder}")
    private String unfinishedChunksPrefix;

    @Value("${advertisement.images.target.folder}")
    private String advertisementImagesPrefix;

    @Value("${amazon.key}")
    private String amazonKey;

    @Value("${amazon.secret}")
    private String amazonSecret;

    @Value("${amazon.active.bucket}")
    private String rawFilesBucket;

    @Value("${amazon.archive.bucket}")
    private String archiveBucket;

    @Value("${amazon.billing.prefix}")
    private String billingPrefix;

    public NodePath proteinDatabasePath(long user, long proteinDatabaseId, String proteinDbName) {
        return new NodePath(Joiner.on(DELIMITER).join(proteinDatabasesPrefix, user, proteinDatabaseId + "-" + proteinDbName));
    }

    public NodePath tempFilePath(long user, long lab, String realFileContentId) {
        return new NodePath(Joiner.on(DELIMITER).join(tempRawFilesPrefix, lab, user, realFileContentId));
    }

    public NodePath experimentAnnotationAttachmentPath(long actor, long annotationAttachmentID) {
        return new NodePath(Joiner.on(DELIMITER).join(experimentsAnnotationsPrefix, actor, annotationAttachmentID));
    }

    public NodePath processingRunPluginAttachmentPath(long actor, long processingRunPluginAttachmentID) {
        return new NodePath(Joiner.on(DELIMITER).join(processingRunPluginAttachmentPrefix, actor, processingRunPluginAttachmentID));
    }

    public NodePath proteinSearchAttachmentPath(long actor, String searchPrefix, String attachmentPrefix, String filename) {
        return new NodePath(Joiner.on(DELIMITER).skipNulls().join(proteinSearchAttachmentPrefix, actor, searchPrefix, attachmentPrefix, filename));
    }

    public NodePath advertisementImagesPath(long advertisementId){
        return new NodePath(Joiner.on(DELIMITER).join(advertisementImagesPrefix, advertisementId ));
    }

    public NodePath labBillingDataPath(long lab) {
        return new NodePath(Joiner.on(DELIMITER).join(getBillingPrefix(), lab));
    }

    public NodePath ftpFilesPath(long actor, long instrument) {
        return new NodePath(Joiner.on(DELIMITER).join(tempFilesFtpPrefix, actor, instrument));
    }

    public NodePath ftpFilesPath(long actor, String accessionNumber) {
        return new NodePath(Joiner.on(DELIMITER).join(tempFilesFtpPrefix, actor, accessionNumber));
    }

    private static String checkHasNowDelimiter(String toCheck) {
        checkArgument(!toCheck.contains(DELIMITER));
        return toCheck;
    }

    public String getAmazonKey() {
        return amazonKey;
    }

    public String getAmazonSecret() {
        return amazonSecret;
    }

    public String getRawFilesBucket() {
        return rawFilesBucket;
    }

    public String getArchiveBucket() {
        return archiveBucket;
    }

    public String getBillingPrefix() {
        return billingPrefix;
    }

    @Override
    @Value("${experiment.attachment.target.folder}")
    public void setExperimentsAttachmentsPrefix(String experimentsAttachmentsPrefix) {
        super.setExperimentsAttachmentsPrefix(experimentsAttachmentsPrefix);
    }

    @Override
    @Value("${raw.files.target.folder}")
    public void setRawFilesPrefix(String rawFilesPrefix) {
        super.setRawFilesPrefix(rawFilesPrefix);
    }

    @Override
    @Value("${project.attachments.target.folder}")
    public void setProjectAttachmentsPrefix(String projectAttachmentsPrefix) {
        super.setProjectAttachmentsPrefix(projectAttachmentsPrefix);
    }
}
