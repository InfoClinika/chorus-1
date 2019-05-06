package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Vitalii Petkanych
 */
@Component
public class ImportServiceFactory {
    @Inject
    public S3FileImportService s3ImportService;

    @Inject
    public FtpFileImportService ftpImportService;

    public FileImportService ofType(UploadType type) {
        switch (type) {
            case FTP:
                return ftpImportService;
            case S3_COPY:
            case S3_LINK:
                return s3ImportService;
            default:
                throw new IllegalArgumentException("uploadType=" + type);
        }
    }
}