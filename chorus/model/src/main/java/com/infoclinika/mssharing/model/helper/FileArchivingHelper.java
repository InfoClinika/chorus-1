package com.infoclinika.mssharing.model.helper;

/**
 * @author Herman Zamula
 */
public interface FileArchivingHelper {

    String moveToArchiveStorage(String filePath);

    boolean isOnGoingToRestore(String archiveId);

    boolean requestUnarchive(String archiveId, boolean forDownloadOnly);

    boolean isArchiveReadyToRestore(String archiveId);

    String moveToAnalyzableStorage(String archiveId);

    String moveArchivedFileToTempStorage(String archiveId, String destination);

    String moveNotArchivedFileToTempStorage(String filePath, String destination);

    boolean isArchived(String archiveId);
}
