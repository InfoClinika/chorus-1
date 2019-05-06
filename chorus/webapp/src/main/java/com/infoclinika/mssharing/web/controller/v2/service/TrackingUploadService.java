package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.entity.upload.UploadDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus;

/**
 * @author Vitalii Petkanych
 */
public interface TrackingUploadService {
    UploadDetails getTrackingDetails(long id);

    void startTracking(UploadDetails details);

    void updateFileProgress(String url, long bytes);

    void updateFileStatus(String url, UploadFileStatus status);

    void cancelFile(long uploadId, long fileId);

    void cancelUpload(long uploadId);

    UploadFileTracker startFileTracking(String url);

    default String trackingId(String host, String file) {
        return "//" + host + (file.charAt(0) == '/' ? "" : "/") + file;
    }

    interface UploadFileTracker {
        void updateFileProgress(long bytes);

        void updateFileStatus(UploadFileStatus status);

        boolean isFileCanceled();

        boolean isFileInterrupted();

        String getFileRecoveryId();

        void setFileRecoveryId(String recoveryId);

        void setFileId(long id);
    }
}
