package com.infoclinika.mssharing.model.read;

import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate;

import java.util.Date;

/**
 * @author Herman Zamula
 */
public class ProjectLine extends ProjectReaderTemplate.ProjectLineTemplate {
    public final boolean blogEnabled;
    public final DashboardReader.ProjectColumns columns;

    public ProjectLine(ProjectReaderTemplate.ProjectLineTemplate other, boolean blogEnabled,
                       DashboardReader.ProjectColumns columns) {
        super(other);
        this.blogEnabled = blogEnabled;
        this.columns = columns;
    }

    public ProjectLine(long id, String name, Date modified, String areaOfResearch, String creatorEmail,
                       AccessLevel accessLevel, LabReaderTemplate.LabLineTemplate lab, String creator,
                       boolean blogEnabled) {
        super(id, name, modified, areaOfResearch, creatorEmail, accessLevel, lab, creator);
        this.blogEnabled = blogEnabled;
        this.columns = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectLine)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ProjectLine that = (ProjectLine) o;

        return blogEnabled == that.blogEnabled;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (blogEnabled ? 1 : 0);
        return result;
    }
}
