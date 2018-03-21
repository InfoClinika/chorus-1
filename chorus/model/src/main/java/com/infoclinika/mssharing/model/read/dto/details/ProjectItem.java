package com.infoclinika.mssharing.model.read.dto.details;

import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ProjectItemTemplate;

/**
* @author Herman Zamula
*/
public class ProjectItem extends ProjectItemTemplate {
    public final long projectId;
    public final boolean blogEnabled;

    public ProjectItem(ProjectItemTemplate template, boolean blogEnabled) {
        super(template);
        this.projectId = template.id;
        this.blogEnabled = blogEnabled;
    }
}
