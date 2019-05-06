package com.infoclinika.mssharing.model.read;


import com.google.common.collect.ImmutableSet;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public interface TrashReader {


    /**
     * Read trash items where user with specified id is direct owner
     */
    ImmutableSet<TrashLine> readByOwner(long actor);

    /**
     * Read trash items related to labs where user with specified id is labhead.
     * If user has no labhead privilege then return trash items by owner
     */
    ImmutableSet<TrashLine> readByOwnerOrLabHead(long actor);

    ImmutableSet<TrashLineShort> readNotRestorableProjects(long actor, List<Long> projects);

    ImmutableSet<TrashLineShort> readNotRestorableExperiments(long actor, List<Long> experiments);

    ImmutableSet<TrashLineShort> readNotRestorableFiles(long actor, List<Long> files);

    class TrashLine {
        public final long id;
        public final String title;
        public final String type;
        public final String labName;
        public final Date deletionDate;

        public TrashLine(Date deletionDate, String labName, String title, long id, String type) {
            this.deletionDate = deletionDate;
            this.labName = labName;
            this.title = title;
            this.id = id;
            this.type = type;
        }
    }

    class TrashLineShort {
        public final long id;
        public final String title;

        public TrashLineShort(long id, String title) {
            this.id = id;
            this.title = title;
        }
    }

}
