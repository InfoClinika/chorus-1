package com.infoclinika.mssharing.model.internal.repository;

/**
 * @author andrii.loboda
 */
public class ExperimentAdditionalInfoRecord {
    public final long experiment;
    public final long userCanCreateExperimentsInProject;
    public final long countFilesReadyToDownload;
    public final long canArchiveExperiment;
    public final long canUnarchiveExperiment;
    public final long countArchivedFilesRequestedForDownloadOnly;
    public final long countArchivedFilesRequestedForUnArchiving;

    public ExperimentAdditionalInfoRecord(long experiment, long userCanCreateExperimentsInProject,
                                          long countFilesReadyToDownload,
                                          long canArchiveExperiment, long canUnarchiveExperiment,
                                          long countArchivedFilesRequestedForDownloadOnly,
                                          long countArchivedFilesRequestedForUnArchiving) {
        this.experiment = experiment;
        this.userCanCreateExperimentsInProject = userCanCreateExperimentsInProject;
        this.countFilesReadyToDownload = countFilesReadyToDownload;
        this.canArchiveExperiment = canArchiveExperiment;
        this.canUnarchiveExperiment = canUnarchiveExperiment;
        this.countArchivedFilesRequestedForDownloadOnly = countArchivedFilesRequestedForDownloadOnly;
        this.countArchivedFilesRequestedForUnArchiving = countArchivedFilesRequestedForUnArchiving;

    }
}
