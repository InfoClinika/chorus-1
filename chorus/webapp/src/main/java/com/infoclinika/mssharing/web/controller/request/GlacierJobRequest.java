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
package com.infoclinika.mssharing.web.controller.request;

/**
 * @author Elena Kurilina
 */
@Deprecated
public class GlacierJobRequest {
    private String action;
    private String archiveId;
    private String archiveSizeInBytes;
    private String completed;
    private String completionDate;
    private String creationDate;
    private String inventorySizeInBytes;
    private String jobDescription;
    private String jobId;
    private String sha256TreeHash;
    private String snsTopic;
    private String statusCode;
    private String statusMessage;
    private String vaultARN;

    public GlacierJobRequest(
        String action, String archiveId, String archiveSizeInBytes, String completed,
        String completionDate, String creationDate, String inventorySizeInBytes,
        String jobDescription, String jobId, String sha256TreeHash, String snsTopic,
        String statusCode, String statusMessage, String vaultARN
    ) {
        this.action = action;
        this.archiveId = archiveId;
        this.archiveSizeInBytes = archiveSizeInBytes;
        this.completed = completed;
        this.completionDate = completionDate;
        this.creationDate = creationDate;
        this.inventorySizeInBytes = inventorySizeInBytes;
        this.jobDescription = jobDescription;
        this.jobId = jobId;
        this.sha256TreeHash = sha256TreeHash;
        this.snsTopic = snsTopic;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.vaultARN = vaultARN;
    }

    public String getAction() {
        return action;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public String getArchiveSizeInBytes() {
        return archiveSizeInBytes;
    }

    public String getCompleted() {
        return completed;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getInventorySizeInBytes() {
        return inventorySizeInBytes;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getJobId() {
        return jobId;
    }

    public String getSha256TreeHash() {
        return sha256TreeHash;
    }

    public String getSnsTopic() {
        return snsTopic;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getVaultARN() {
        return vaultARN;
    }

    @Override
    public String toString() {
        return "GlacierJobRequest{" +
            "action='" + action + '\'' +
            ", archiveId='" + archiveId + '\'' +
            ", archiveSizeInBytes='" + archiveSizeInBytes + '\'' +
            ", completed='" + completed + '\'' +
            ", completionDate='" + completionDate + '\'' +
            ", creationDate='" + creationDate + '\'' +
            ", inventorySizeInBytes='" + inventorySizeInBytes + '\'' +
            ", jobDescription='" + jobDescription + '\'' +
            ", jobId='" + jobId + '\'' +
            ", sha256TreeHash='" + sha256TreeHash + '\'' +
            ", snsTopic='" + snsTopic + '\'' +
            ", statusCode='" + statusCode + '\'' +
            ", statusMessage='" + statusMessage + '\'' +
            ", vaultARN='" + vaultARN + '\'' +
            '}';
    }
}
