package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadDetails;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Vitalii Petkanych
 */
@Transactional
public interface FileImportHelper {
    void importFinish(UploadDetails uploadDetails);

    void createFileMetadata(long userId, long instrumentId, FileDetails f);

    void finishFileMetadata(FileDetails file);

    void removeFileMetadata(long fileId);

    boolean isFileAlreadyUploaded(long userId, long instrumentId, String fullName);

    void saveFileDetails(FileDetails file);

    void saveUploadDetails(UploadDetails upload);

    long createFileReference(
        String bucket,
        String fullName,
        boolean readOnly,
        long size,
        String labels,
        long userId,
        long instrumentId,
        long specieId
    );
}
