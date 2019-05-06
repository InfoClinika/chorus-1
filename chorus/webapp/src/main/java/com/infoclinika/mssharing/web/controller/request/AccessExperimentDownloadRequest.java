package com.infoclinika.mssharing.web.controller.request;

/**
 * @author Nikita Matrosov
 */
public class AccessExperimentDownloadRequest {

    private long experimentId;
    private String downloadExperimentLink;

    public String getDownloadExperimentLink() {
        return downloadExperimentLink;
    }

    public void setDownloadExperimentLink(String downloadExperimentLink) {
        this.downloadExperimentLink = downloadExperimentLink;
    }

    public long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(long experimentId) {
        this.experimentId = experimentId;
    }
}
