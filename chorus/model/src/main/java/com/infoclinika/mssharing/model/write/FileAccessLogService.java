package com.infoclinika.mssharing.model.write;

import com.infoclinika.mssharing.model.helper.items.ChorusFileData;

/**
 * @author vladislav.kovchug
 */


public interface FileAccessLogService {
    void logFileUploadStart(long actor, long instrument, long fileId);

    void logFileUploadConfirm(long actor, long labId, long fileId);

    void logFileDelete(long fileId);

    void logFileDeletePermanently(long actor, long fileId);

    void logFileArchiveStart(long actor, long fileId);

    void logFileArchiveConfirm(long fileId);

    void logFileDownload(long actor, ChorusFileData fileData);
}
