package com.infoclinika.mssharing.platform.model.write;

import com.google.common.base.Optional;

/**
 * @author Herman Zamula
 */
public interface ProjectManagementTemplate<PROJECT_INFO extends ProjectManagementTemplate.ProjectInfoTemplate> {

    /**
     * Create new Project.
     *
     * @param creator     - user ID with performs creation operation.
     * @param projectInfo - project specific information
     * @return created project ID
     */
    long createProject(long creator, PROJECT_INFO projectInfo);

    void removeProject(long actor, long project);

    void updateProject(long actor, long project, PROJECT_INFO projectInfo);

    long copyProject(long actor, CopyProjectInfoTemplate copyProjectInfoTemplate);

    class ProjectInfoTemplate {

        public final Optional<Long> lab;
        public final String name;
        public final String description;
        public final String areaOfResearch;

        public ProjectInfoTemplate(Long lab, String name, String description, String areaOfResearch) {
            this.lab = Optional.fromNullable(lab);
            this.name = name;
            this.description = description;
            this.areaOfResearch = areaOfResearch;
        }
    }


    static class CopyProjectInfoTemplate {
        private final long project;
        private final long newOwner;
        private final long owner;
        private final boolean notification;

        public CopyProjectInfoTemplate(long project, long newOwner, long owner, boolean notification) {
            this.project = project;
            this.newOwner = newOwner;
            this.owner = owner;
            this.notification = notification;
        }

        public long getProject() {
            return project;
        }

        public long getNewOwner() {
            return newOwner;
        }

        public long getOwner() {
            return owner;
        }

        public boolean isNotification() {
            return notification;
        }
    }
}
