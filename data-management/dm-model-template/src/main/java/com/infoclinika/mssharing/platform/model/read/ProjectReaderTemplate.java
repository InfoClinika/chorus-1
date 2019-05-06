package com.infoclinika.mssharing.platform.model.read;


import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate.LabLineTemplate;

import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;

/**
 * @author Herman Zamula
 */
public interface ProjectReaderTemplate<PROJECT_LINE extends ProjectReaderTemplate.ProjectLineTemplate> {

    PROJECT_LINE readProject(long userId, long projectID);

    SortedSet<PROJECT_LINE> readProjects(long actor, Filter genericFilter);

    PagedItem<PROJECT_LINE> readProjects(long actor, Filter genericFilter, PagedItemInfo pagedItemInfo);

    SortedSet<PROJECT_LINE> readProjectsAllowedForWriting(long user);

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

        public ProjectLineTemplate(long id,
                                   String name,
                                   Date modified,
                                   String areaOfResearch,
                                   String creatorEmail,
                                   AccessLevel accessLevel,
                                   LabLineTemplate lab,
                                   String creator) {
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
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ProjectLineTemplate)) {
                return false;
            }
            ProjectLineTemplate that = (ProjectLineTemplate) o;
            return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(modified, that.modified) &&
                Objects.equals(lab, that.lab) &&
                Objects.equals(creator, that.creator) &&
                Objects.equals(creatorEmail, that.creatorEmail) &&
                Objects.equals(areaOfResearch, that.areaOfResearch) &&
                accessLevel == that.accessLevel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, modified, lab, creator, creatorEmail, areaOfResearch, accessLevel);
        }
    }
}
