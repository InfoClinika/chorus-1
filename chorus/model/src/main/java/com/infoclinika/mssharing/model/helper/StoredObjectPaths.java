/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.helper;

import com.google.common.base.Joiner;
import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;
import static com.infoclinika.mssharing.platform.fileserver.StorageService.DELIMITER;

/**
 * @author Oleksii Tymchenko
 */
@Service
public class StoredObjectPaths extends StoredObjectPathsTemplate {
    private final AmazonPropertiesProvider amazonPropertiesProvider;
    private final ChorusPropertiesProvider chorusPropertiesProvider;
    private final BillingPropertiesProvider billingPropertiesProvider;

    @Inject
    public StoredObjectPaths(AmazonPropertiesProvider amazonPropertiesProvider,
                             ChorusPropertiesProvider chorusPropertiesProvider,
                             BillingPropertiesProvider billingPropertiesProvider) {
        this.amazonPropertiesProvider = amazonPropertiesProvider;
        this.chorusPropertiesProvider = chorusPropertiesProvider;
        this.billingPropertiesProvider = billingPropertiesProvider;

        setRawFilesPrefix(chorusPropertiesProvider.getRawFilesTargetFolder());
        setProjectAttachmentsPrefix(chorusPropertiesProvider.getProjectAttachmentsTargetFolder());
        setExperimentsAttachmentsPrefix(chorusPropertiesProvider.getExperimentsAttachmentsTargetFolder());
    }

    public NodePath hdf5FilePath(long user, long timestamp, String fileName) {
        return new NodePath(Joiner.on(DELIMITER).join(
            chorusPropertiesProvider.getHdf5FilesFolder(),
            user,
            timestamp,
            fileName
        ));
    }

    public NodePath proteinDatabasePath(long user, long proteinDatabaseId, String proteinDbName) {
        return new NodePath(Joiner.on(DELIMITER).join(
            chorusPropertiesProvider.getProteinDatabasesTargetFolder(),
            user,
            proteinDatabaseId + "-" + proteinDbName
        ));
    }

    public NodePath tempFilePath(long user, long lab, String realFileContentId) {
        return new NodePath(Joiner.on(DELIMITER).join(
            chorusPropertiesProvider.getRawFilesTempFolder(),
            lab,
            user,
            realFileContentId
        ));
    }

    public NodePath experimentAnnotationAttachmentPath(long actor, long annotationAttachmentID) {
        return new NodePath(Joiner.on(DELIMITER).join(
            chorusPropertiesProvider.getExperimentsAnnotationsTargetFolder(),
            actor,
            annotationAttachmentID
        ));
    }

    public NodePath advertisementImagesPath(long advertisementId) {
        return new NodePath(Joiner.on(DELIMITER).join(
            chorusPropertiesProvider.getAdvertisementImagesTargetFolder(),
            advertisementId
        ));
    }

    public NodePath labBillingDataPath(long lab) {
        return new NodePath(Joiner.on(DELIMITER).join(getBillingPrefix(), lab));
    }

    public NodePath ftpFilesPath(long actor, long instrument) {
        return new NodePath(Joiner.on(DELIMITER).join(
            chorusPropertiesProvider.getRawFilesTempFtpFolder(),
            actor,
            instrument
        ));
    }

    public NodePath ftpFilesPath(long actor, String accessionNumber) {
        return new NodePath(Joiner.on(DELIMITER).join(
            chorusPropertiesProvider.getRawFilesTempFtpFolder(),
            actor,
            accessionNumber
        ));
    }

    private static String checkHasNowDelimiter(String toCheck) {
        checkArgument(!toCheck.contains(DELIMITER));
        return toCheck;
    }

    public String getAmazonKey() {
        return amazonPropertiesProvider.getAccessKey();
    }

    public String getAmazonSecret() {
        return amazonPropertiesProvider.getSecretKey();
    }

    public String getRawFilesBucket() {
        return amazonPropertiesProvider.getActiveBucket();
    }

    public String getRawFilesBucket(String fileBucket) {
        return StringUtils.isEmpty(fileBucket) ? getRawFilesBucket() : fileBucket;
    }

    public String getArchiveBucket() {
        return amazonPropertiesProvider.getArchiveBucket();
    }

    public String getBillingPrefix() {
        return billingPropertiesProvider.getBillingPrefix();
    }

    public String getHdf5FilesPrefix() {
        return chorusPropertiesProvider.getHdf5FilesFolder();
    }


    @Override
    public void setExperimentsAttachmentsPrefix(String experimentsAttachmentsPrefix) {
        super.setExperimentsAttachmentsPrefix(experimentsAttachmentsPrefix);
    }

    @Override
    public void setRawFilesPrefix(String rawFilesPrefix) {
        super.setRawFilesPrefix(rawFilesPrefix);
    }

    @Override
    public void setProjectAttachmentsPrefix(String projectAttachmentsPrefix) {
        super.setProjectAttachmentsPrefix(projectAttachmentsPrefix);
    }
}
