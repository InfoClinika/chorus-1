package com.infoclinika.mssharing.web.controller.response;

import com.infoclinika.mssharing.model.read.DashboardReader;

import java.util.Set;

/**
 * @author Herman Zamula
 */
public class SkylinePublicFolderStructureResponse {

    public final Set<DashboardReader.ProjectStructure> publicProjects;
    public final Set<DashboardReader.ExperimentStructure> publicExperiments;
    public final Set<DashboardReader.UploadedFile> publicFiles;

    public SkylinePublicFolderStructureResponse(
        Set<DashboardReader.ProjectStructure> publicProjects,
        Set<DashboardReader.ExperimentStructure> publicExperiments,
        Set<DashboardReader.UploadedFile> publicFiles
    ) {
        this.publicProjects = publicProjects;
        this.publicExperiments = publicExperiments;
        this.publicFiles = publicFiles;
    }
}