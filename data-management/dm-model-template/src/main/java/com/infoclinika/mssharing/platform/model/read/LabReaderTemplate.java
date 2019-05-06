package com.infoclinika.mssharing.platform.model.read;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.common.items.LabItem;

import java.util.Date;
import java.util.Objects;
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

        public LabLineTemplate(long id,
                               String name,
                               long labHead,
                               String institutionUrl,
                               String laboratoryHeadName,
                               Date modified) {
            this.id = id;
            this.name = name;
            this.labHead = labHead;
            this.institutionUrl = institutionUrl;
            this.laboratoryHeadName = laboratoryHeadName;
            this.modified = modified;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LabLineTemplate)) {
                return false;
            }
            LabLineTemplate that = (LabLineTemplate) o;
            return id == that.id &&
                labHead == that.labHead &&
                Objects.equals(name, that.name) &&
                Objects.equals(institutionUrl, that.institutionUrl) &&
                Objects.equals(laboratoryHeadName, that.laboratoryHeadName) &&
                Objects.equals(modified, that.modified);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, labHead, institutionUrl, laboratoryHeadName, modified);
        }
    }
}
