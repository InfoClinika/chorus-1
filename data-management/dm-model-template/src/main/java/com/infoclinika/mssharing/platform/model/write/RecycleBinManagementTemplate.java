package com.infoclinika.mssharing.platform.model.write;

import java.util.Set;

/**
 * User can remove projects, experiments and files temporarily or permanently.
 * Temporarily removed items are placed in the Trash List. User can restore them or they will be automatically removed permanently
 * after one week.
 *
 * @author Herman Zamula
 */
public interface RecycleBinManagementTemplate<ID> {

    /**
     * Removes temporarily the specified projects. User can remove only private projects
     *
     * @param actor Initiator
     * @param items Projects ids to remove
     */
    void moveProjectsToTrash(ID actor, Set<ID> items);

    /**
     * Restores projects from Trash.
     *
     * @param actor Initiator
     * @param items Restorable projects ids
     */
    void restoreProjects(ID actor, Set<ID> items);

    /**
     * Removes temporarily the specified experiments. User can remove only private experiments
     *
     * @param actor Initiator
     * @param items Experiments ids to remove
     */
    void moveExperimentsToTrash(ID actor, Set<ID> items);

    /**
     * Restores experiments from Trash.
     *
     * @param actor Initiator
     * @param items Restorable experiments ids
     */
    void restoreExperiments(ID actor, Set<ID> items);

    /**
     * Removes temporarily the specified files. User can remove only private files that not used in experiments.
     *
     * @param actor Initiator
     * @param items Files ids to remove
     */
    void moveFilesToTrash(ID actor, Set<ID> items);

    /**
     * Restores files from Trash.
     *
     * @param actor Initiator
     * @param items Restorable files ids
     */
    void restoreFiles(ID actor, Set<ID> items);

}
