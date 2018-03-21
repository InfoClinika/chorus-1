package com.infoclinika.mssharing.platform.model.read;


import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate.LabLineTemplate;

import java.util.Date;
import java.util.SortedSet;

/**
 * @author Herman Zamula
 */
public interface ProjectReaderTemplate<PROJECT_LINE extends ProjectReaderTemplate.ProjectLineTemplate> {

    PROJECT_LINE readProject(long userId, long projectID);

    SortedSet<PROJECT_LINE> readProjects(long actor, Filter genericFilter);

    SortedSet<PROJECT_LINE> readProjectsAllowedForWriting(long user);

    PagedItem<PROJECT_LINE> readProjects(long actor, Filter genericFilter, PagedItemInfo pagedItemInfo);

    PagedItem<PROJECT_LINE> readProjectsByLab(long actor, Long lab, PagedItemInfo pagedItemInfo);

    class ProjectLineTemplate {

        public final long id;
        public final String name;
        public final Date modified;
        public final LabLineTemplate lab;
        public final String creator;
        public final String creatorEmail;
        public final String areaOfResearch;
        public final AccessLevel accessLevel;

        public ProjectLineTemplate(long id, String name, Date modified, String areaOfResearch, String creatorEmail, AccessLevel accessLevel, LabLineTemplate lab, String creator) {
            this.id = id;
            this.name = name;
            this.modified = modified;
            this.areaOfResearch = areaOfResearch;
            this.creatorEmail = creatorEmail;
            this.accessLevel = accessLevel;
            this.lab = lab;
            this.creator = creator;
        }


        public ProjectLineTemplate(ProjectLineTemplate other) {
            this.id = other.id;
            this.name = other.name;
            this.modified = other.modified;
            this.lab = other.lab;
            this.creator = other.creator;
            this.creatorEmail = other.creatorEmail;
            this.areaOfResearch = other.areaOfResearch;
            this.accessLevel = other.accessLevel;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProjectLineTemplate)) return false;

            ProjectLineTemplate that = (ProjectLineTemplate) o;

            if (id != that.id) return false;
            if (accessLevel != that.accessLevel) return false;
            if (areaOfResearch != null ? !areaOfResearch.equals(that.areaOfResearch) : that.areaOfResearch != null)
                return false;
            if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
            if (creatorEmail != null ? !creatorEmail.equals(that.creatorEmail) : that.creatorEmail != null)
                return false;
            if (lab != null ? !lab.equals(that.lab) : that.lab != null) return false;
            if (modified != null ? !modified.equals(that.modified) : that.modified != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (modified != null ? modified.hashCode() : 0);
            result = 31 * result + (lab != null ? lab.hashCode() : 0);
            result = 31 * result + (creator != null ? creator.hashCode() : 0);
            result = 31 * result + (creatorEmail != null ? creatorEmail.hashCode() : 0);
            result = 31 * result + (areaOfResearch != null ? areaOfResearch.hashCode() : 0);
            result = 31 * result + (accessLevel != null ? accessLevel.hashCode() : 0);
            return result;
        }
    }
}
