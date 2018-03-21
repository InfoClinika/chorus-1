package com.infoclinika.mssharing.web.controller.request;

import java.util.List;

/**
 * @author Nikita Matrosov
 */
public class ReadNotRestorableItemsRequest {
    private List<Long> projectIds;
    private List<Long> experimentIds;
    private List<Long> fileIds;

    public ReadNotRestorableItemsRequest() {
    }

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }

    public List<Long> getExperimentIds() {
        return experimentIds;
    }

    public void setExperimentIds(List<Long> experimentIds) {
        this.experimentIds = experimentIds;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }
}
