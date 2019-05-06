package com.infoclinika.mssharing.model.write;

/**
 * @author yevhen.panko
 */
public interface ArchiveSynchronizationManagement<S> {

    void synchronizeS3StateWithDB();

    void synchronizeS3StateWithDB(String activeBucket, String archiveBucket);

    void cancelSynchronization();

    S checkSynchronizationState();
}
