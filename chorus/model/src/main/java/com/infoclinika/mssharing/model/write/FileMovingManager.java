package com.infoclinika.mssharing.model.write;

import java.util.Collection;

/**
 * @author Elena Kurilina
 */
public interface FileMovingManager {

    /**
     * Unarchive the experiment files that don't have "UNARCHIVED" status
     *
     * @param experimentId experiment to unarchive
     * @param actors       users to send email when files will be ready to download
     */
    void requestExperimentFilesUnarchiving(long experimentId, Collection<Long> actors);

    void requestFilesUnarchiving(Collection<Long> files, Long actor);

    void requestFileUnarchiving(long file);
    //-----------

    /**
     * Deprecated since using s3 bucket policies for archive files.
     */
    @Deprecated
    public void moveFilesToStorageAndListen(Collection<Long> files, String listenerId);

    void moveToArchiveStorage(long file);

    @Deprecated
    void moveToArchiveStorage(Collection<Long> files);

    @Deprecated
    void moveToAnalysableStorage(Collection<Long> files);

    void downloadToAnalysableStorageRetrievedFile(long file);

    void updateAccessForExperiment(long experiment);

    void updateAccessForFile(Collection<Long> files);

    void deleteFromStorage(String key);

    void deleteFromArchiveStorage(String key);

    /**
     * For internal use of ArchiverJobs#checkFilesReadyToUnarchive
     */
    void moveReadyToUnarchiveToAnalysableStorage();

    void moveToArchiveExpiredUnarchivedFiles();

}
