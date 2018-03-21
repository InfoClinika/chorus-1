package com.infoclinika.mssharing.platform.model.read;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.common.items.LabItem;

import java.util.Date;
import java.util.SortedSet;

/**
 * @author : Alexander Serebriyan
 */
public interface LabReaderTemplate<LAB_LINE extends LabReaderTemplate.LabLineTemplate> {

    ImmutableSet<LAB_LINE> readUserLabs(long actor);

    LAB_LINE readLab(long id);

    LAB_LINE readLabByName(String name);

    ImmutableSet<LAB_LINE> readAllLabs(long actor);

    //TODO: Consider move this method to separate service
    SortedSet<LabItem> readLabItems(long userId);

    class LabLineTemplate {
        public final long id;
        public final String name;
        public final long labHead;
        public final String institutionUrl;
        public final String laboratoryHeadName;
        public final Date modified;

        public LabLineTemplate(long id, String name, long labHead, String institutionUrl, String laboratoryHeadName, Date modified) {
            this.id = id;
            this.name = name;
            this.labHead = labHead;
            this.institutionUrl = institutionUrl;
            this.laboratoryHeadName = laboratoryHeadName;
            this.modified = modified;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LabLineTemplate that = (LabLineTemplate) o;

            if (id != that.id) return false;
            if (labHead != that.labHead) return false;
            if (institutionUrl != null ? !institutionUrl.equals(that.institutionUrl) : that.institutionUrl != null)
                return false;
            if (laboratoryHeadName != null ? !laboratoryHeadName.equals(that.laboratoryHeadName) : that.laboratoryHeadName != null)
                return false;
            if (modified != null ? !modified.equals(that.modified) : that.modified != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (int) (labHead ^ (labHead >>> 32));
            result = 31 * result + (institutionUrl != null ? institutionUrl.hashCode() : 0);
            result = 31 * result + (laboratoryHeadName != null ? laboratoryHeadName.hashCode() : 0);
            result = 31 * result + (modified != null ? modified.hashCode() : 0);
            return result;
        }
    }
}
