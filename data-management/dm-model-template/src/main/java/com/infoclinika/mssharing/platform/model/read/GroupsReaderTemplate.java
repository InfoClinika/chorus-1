package com.infoclinika.mssharing.platform.model.read;

import com.google.common.collect.ImmutableSet;

import java.util.Date;

/**
 * @author Herman Zamula
 */
public interface GroupsReaderTemplate<GROUP_LINE extends GroupsReaderTemplate.GroupLine> {

    /**
     * Read information about actor's groups.
     *
     * @param actor           - user ID that requested information
     * @param includeAllUsers - set "true" to list with special "All" users group
     * @return all groups
     */
    ImmutableSet<GROUP_LINE> readGroups(long actor, boolean includeAllUsers);

    class GroupLine {
        public final long id;
        public final String name;
        public final Date lastModified;
        public final int numberOfMembers;
        public final int numberOfProjects;

        public GroupLine(long id, String name, Date lastModified, int numberOfMembers, int numberOfProjects) {
            this.id = id;
            this.name = name;
            this.lastModified = lastModified;
            this.numberOfMembers = numberOfMembers;
            this.numberOfProjects = numberOfProjects;
        }
    }

}
