package com.infoclinika.mssharing.platform.model.write;

import java.util.Collection;

/**
 * Lab head have rights to manipulate with users which are in this laboratory. He can remove user from laboratory.
 * In this case all projects, experiments, files should be re-assigned to lab head from removed user
 *
 * @author : Alexander Serebriyan
 */
public interface LabHeadManagementTemplate {
    /**
     * Removes user from laboratory. As user could be a lab head of several labs the lab parameter should be specified
     *
     * @param labHead - head of laboratory from which user should be eliminated
     * @param lab     - laboratory from which user should be eliminated
     * @param user    - user who should be removed from specified laboratory
     */
    void removeUserFromLab(long labHead, long lab, long user);

    boolean isLabHead(long actor);

    Collection<Long> findLabsForLabHead(long actor);
}
