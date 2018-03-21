package com.infoclinika.mssharing.web.controller.response;

import com.infoclinika.mssharing.model.read.DashboardReader;

import java.util.Set;

/**
 * @author Herman Zamula
 */
public class SkylineSharedFolderStructureResponse {

    public final Set<DashboardReader.ProjectStructure> sharedProjects;
    public final Set<DashboardReader.ExperimentStructure> sharedExperiments;
    public final Set<DashboardReader.UploadedFile> sharedFiles;

    public SkylineSharedFolderStructureResponse(Set<DashboardReader.ProjectStructure> sharedProjects,
                                                Set<DashboardReader.ExperimentStructure> sharedExperiments,
                                                Set<DashboardReader.UploadedFile> sharedFiles) {
        this.sharedProjects = sharedProjects;
        this.sharedExperiments = sharedExperiments;
        this.sharedFiles = sharedFiles;
    }
}