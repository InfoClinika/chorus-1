package com.infoclinika.mssharing.model.write;

import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;

/**
 * @author Herman Zamula
 */
public class ProjectInfo extends ProjectManagementTemplate.ProjectInfoTemplate {

    public ProjectInfo(String name, String areaOfResearch, String description, Long lab) {
        super(lab, name, description, areaOfResearch);
    }

    public ProjectInfo(String name, String areaOfResearch, Long lab) {
        super(lab, name, "", areaOfResearch);
    }
}
