package com.infoclinika.mssharing.platform.model.helper;

import com.google.common.collect.ImmutableSortedSet;

import java.util.List;

/**
 * Contains useful read methods on project sharing.
 *
 * @author Herman Zamula
 */
public interface SharingProjectHelperTemplate {

    /**
     * Users can share their projects with anyone in the system by email.
     * For implementing auto-completion or selection from list use these method.
     *
     * @return all users available for sharing
     */
    List<UserDetails> getAvailableUsers();

    /**
     * Users can share their projects with anyone in the system by email.
     * For implementing auto-completion or selection from list by query use these method.
     * *
     *
     * @param query query to find users. Search performed by firstName, lastName and email
     * @return users available for sharing starting with query. Empty list if query is null or empty
     */
    List<UserDetails> getAvailableUsersStartingWith(String query);

    /**
     * Users can share their projects with groups.
     * For implementing auto-completion or selection from list use these method.
     *
     * @return all groups available for sharing
     */
    ImmutableSortedSet<GroupDetails> getAvailableGroups(long actor);

    /**
     * Gets all collaborators of experiment's parent project.
     *
     * @return list of users who are collaborators of experiment's parent project
     */
    List<UserDetails> getCollaborators(long actor, long experiment);

    /**
     * Represents user on sharing screen.
     */
    class UserDetails {
        public final long id;
        public final String name;
        public final String email;

        public UserDetails(long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    /**
     * Represents group on sharing screen.
     */
    class GroupDetails {
        public final long id;
        public final String name;
        public final int numberOfMembers;


        public GroupDetails(long id, String name, int numberOfMembers) {
            this.id = id;
            this.name = name;
            this.numberOfMembers = numberOfMembers;
        }
    }
}
