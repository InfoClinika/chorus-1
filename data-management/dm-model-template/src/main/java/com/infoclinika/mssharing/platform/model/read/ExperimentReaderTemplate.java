package com.infoclinika.mssharing.platform.model.read;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;

import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;

/**
 * @author Herman Zamula
 */
public interface ExperimentReaderTemplate<EXPERIMENT_LINE extends ExperimentReaderTemplate.ExperimentLineTemplate> {

    SortedSet<EXPERIMENT_LINE> readExperiments(long actor, Filter filter);

    PagedItem<EXPERIMENT_LINE> readExperiments(long actor, Filter genericFilter, PagedItemInfo pagedItemInfo);

    SortedSet<EXPERIMENT_LINE> readExperimentsByProject(long actor, long projectId);

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
        public final boolean failed;

        public ExperimentLineTemplate(long id,
                                      String name,
                                      String project,
                                      long files,
                                      Date modified,
                                      LabReaderTemplate.LabLineTemplate lab,
                                      String downloadLink,
                                      String creator,
                                      AccessLevel accessLevel,
                                      long owner,
                                      boolean failed) {
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
            this.failed = failed;
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
            this.failed = other.failed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ExperimentLineTemplate)) {
                return false;
            }
            ExperimentLineTemplate that = (ExperimentLineTemplate) o;
            return id == that.id &&
                files == that.files &&
                owner == that.owner &&
                failed == that.failed &&
                Objects.equals(name, that.name) &&
                Objects.equals(project, that.project) &&
                Objects.equals(lab, that.lab) &&
                Objects.equals(creator, that.creator) &&
                Objects.equals(modified, that.modified) &&
                accessLevel == that.accessLevel &&
                Objects.equals(downloadLink, that.downloadLink);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                id,
                name,
                project,
                files,
                lab,
                creator,
                modified,
                accessLevel,
                downloadLink,
                owner,
                failed
            );
        }
    }

}
