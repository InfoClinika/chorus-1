package com.infoclinika.mssharing.platform.model.read;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;

import java.util.Date;
import java.util.SortedSet;

/**
 * @author Herman Zamula
 */
public interface ExperimentReaderTemplate<EXPERIMENT_LINE extends ExperimentReaderTemplate.ExperimentLineTemplate> {

    SortedSet<EXPERIMENT_LINE> readExperiments(long actor, Filter filter);

    SortedSet<EXPERIMENT_LINE> readExperimentsByProject(long actor, long projectId);

    PagedItem<EXPERIMENT_LINE> readExperiments(long actor, Filter genericFilter, PagedItemInfo pagedItemInfo);

    PagedItem<EXPERIMENT_LINE> readExperimentsByLab(long actor, long labId, PagedItemInfo pagedItemInfo);

    PagedItem<EXPERIMENT_LINE> readPagedExperimentsByProject(long actor, long projectId, PagedItemInfo pageInfo);

    class ExperimentLineTemplate {

        public final long id;
        public final String name;
        public final String project;
        public final long files;
        public final LabReaderTemplate.LabLineTemplate lab;
        public final String creator;
        public final Date modified;
        public final AccessLevel accessLevel;
        public final String downloadLink;
        public final long owner;

        public ExperimentLineTemplate(long id, String name, String project, long files, Date modified, LabReaderTemplate.LabLineTemplate lab, String downloadLink, String creator, AccessLevel accessLevel, long owner) {
            this.id = id;
            this.name = name;
            this.project = project;
            this.files = files;
            this.modified = modified;
            this.lab = lab;
            this.downloadLink = downloadLink;
            this.creator = creator;
            this.accessLevel = accessLevel;
            this.owner = owner;
        }

        public ExperimentLineTemplate(ExperimentLineTemplate other) {
            this.id = other.id;
            this.name = other.name;
            this.project = other.project;
            this.files = other.files;
            this.lab = other.lab;
            this.creator = other.creator;
            this.modified = other.modified;
            this.accessLevel = other.accessLevel;
            this.downloadLink = other.downloadLink;
            this.owner = other.owner;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExperimentLineTemplate)) return false;

            ExperimentLineTemplate that = (ExperimentLineTemplate) o;

            if (files != that.files) return false;
            if (id != that.id) return false;
            if (owner != that.owner) return false;
            if (accessLevel != that.accessLevel) return false;
            if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
            if (downloadLink != null ? !downloadLink.equals(that.downloadLink) : that.downloadLink != null)
                return false;
            if (lab != null ? !lab.equals(that.lab) : that.lab != null) return false;
            if (modified != null ? !modified.equals(that.modified) : that.modified != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (project != null ? !project.equals(that.project) : that.project != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (project != null ? project.hashCode() : 0);
            result = 31 * result + (int) (files ^ (files >>> 32));
            result = 31 * result + (lab != null ? lab.hashCode() : 0);
            result = 31 * result + (creator != null ? creator.hashCode() : 0);
            result = 31 * result + (modified != null ? modified.hashCode() : 0);
            result = 31 * result + (accessLevel != null ? accessLevel.hashCode() : 0);
            result = 31 * result + (downloadLink != null ? downloadLink.hashCode() : 0);
            result = 31 * result + (int) (owner ^ (owner >>> 32));
            return result;
        }
    }

}
