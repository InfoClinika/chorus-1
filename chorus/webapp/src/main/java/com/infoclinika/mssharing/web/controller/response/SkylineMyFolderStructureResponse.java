package com.infoclinika.mssharing.web.controller.response;

import com.infoclinika.mssharing.model.read.DashboardReader;

import java.util.Set;

/**
 * @author Herman Zamula
 */
public class SkylineMyFolderStructureResponse {

    public final Set<DashboardReader.ProjectStructure> myProjects;
    public final Set<DashboardReader.ExperimentStructure> myExperiments;
    public final Set<DashboardReader.UploadedFile> myFiles;

    public SkylineMyFolderStructureResponse(
        Set<DashboardReader.ProjectStructure> myProjects,
        Set<DashboardReader.ExperimentStructure> myExperiments,
        Set<DashboardReader.UploadedFile> myFiles
    ) {
        this.myProjects = myProjects;
        this.myExperiments = myExperiments;
        this.myFiles = myFiles;
    }
}