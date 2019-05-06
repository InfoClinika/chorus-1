package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class ProjectSharingRequestTemplate extends AbstractPersistable {

    @Basic()
    protected Long experimentId;
    @ElementCollection(fetch = FetchType.EAGER)
    protected List<String> downloadExperimentLinks = new ArrayList<>();
    @Basic(optional = false)
    private Long projectId;
    @Basic(optional = false)
    private Long requesterId;
    private Date requestDate;

    public ProjectSharingRequestTemplate() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public List<String> getDownloadExperimentLinks() {
        return downloadExperimentLinks;
    }

    public void setDownloadExperimentLinks(List<String> downloadExperimentLinks) {
        this.downloadExperimentLinks = downloadExperimentLinks;
    }

    public Long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Long experimentId) {
        this.experimentId = experimentId;
    }
}
