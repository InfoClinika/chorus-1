package com.infoclinika.mssharing.platform.model.write;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface FileUploadManagementTemplate {

    void startMultipartUpload(long actor, long file, String uploadId, String destinationPath);

    void completeMultipartUpload(long actor, long file, String contentId);

    void cancelUpload(long actor, long file);

    boolean checkMultipleFilesValidForUpload(long instrument, List<String> files);

    boolean isFileAlreadyUploadedForInstrument(long actor, long instrument, String fileName);
}
