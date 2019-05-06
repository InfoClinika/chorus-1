package com.infoclinika.mssharing.model.write;

import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * @author Herman Zamula
 */
@Transactional
public interface FileOperationsManager {

    void markFilesToArchive(long actor, Set<Long> files);

    void markFilesToUnarchive(long actor, Set<Long> files);

    void markExperimentFilesToArchive(long actor, long experiment);

    void markExperimentFilesToUnarchive(long actor, long experiment);

    void archiveMarkedFiles();

    void unarchiveMarkedFiles();

    void makeFilesAvailableForDownload(long actor, Set<Long> files);

    void makeExperimentFilesAvailableForDownload(long actor, long experiment);

    void checkIsFilesConsistent(long actor);

    void checkIsFileConsistent(long actor, long file);

    void markUnsuccessfullyUploadedFilesToReplaceAndCorrupted();
}
