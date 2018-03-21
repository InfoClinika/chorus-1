package com.infoclinika.mssharing.platform.model.write;

import java.util.Map;

/**
 * Project owner can share his project to application users and the sharing groups. Group is just a custom set of users.
 * In this case of sharing project all the experiments and the RAW files which are the part of this project
 * become available for those users for read access.
 * <p/>
 * <p/>
 * Also project owner can mark his project as public in this case it's become available to all system users.
 *
 * @author Stanislav Kurilin, Herman Zamula
 */
public interface SharingManagementTemplate {

    /**
     * Create new group.
     *
     * @param owner         - user id that will own new group
     * @param name          - name of new group. User shouldn't have group with such name before.
     * @param collaborators - initial group members
     * @return id of new group
     */
    long createGroup(long owner, String name, Iterable<Long> collaborators);

    void renameGroup(long actor, long group, String newName);

    /**
     * Create new group.
     *
     * @param actor         - user id that perform operation
     * @param group         - group ID
     * @param collaborators - new group members
     */
    void setCollaborators(long actor, long group, Iterable<Long> collaborators, boolean withEmailNotification);

    /**
     * Share project with colleagues
     *
     * @param actor                 - project owner user ID
     * @param project               - project ID
     * @param colleagues            - application users IDs-access level pair to with actor wants share his project
     * @param groups                - actor's groups-access level pair with actor wants share his project
     * @param withEmailNotification - specifies is system should send emails to new collaborators
     */
    void updateSharingPolicy(long actor, long project, Map<Long, Access> colleagues, Map<Long, Access> groups, boolean withEmailNotification);

    /**
     * Mark as public.
     * After making it public all sharing policies are removing.
     *
     * @param actor   - project owner user ID
     * @param project - project ID
     */
    void makeProjectPublic(long actor, long project);

    /**
     * Mark as public.
     * After making it private all sharing policies are removing.
     *
     * @param actor   - project owner user ID
     * @param project - project ID
     */
    void makeProjectPrivate(long actor, long project);

    /**
     * Removes sharing group.
     *
     * @param actor - user's id that is an owner of sharing group
     * @param group - group's id that should be removed
     */
    void removeGroup(long actor, long group);

    /**
     * Creates records that describes level of user access to list of projects;
     * <p>
     * TODO: Not part of API. Consider move it to separate service
     */
    void updateProjectAccessRecords(long projectId, Map<Long, Access> sharedTo);

    enum Access {READ, WRITE}
}
