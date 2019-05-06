package com.infoclinika.mssharing.platform.model.write;

/**
 * @author Herman Zamula
 */
public interface ProjectSharingRequestManagement {

    long newProjectSharingRequest(long actor, long experimentId, String downloadExperimentLink);

    void approveSharingProject(long actor, long project, long requester);

    void refuseSharingProject(long actor, long project, long requester, String refuseComment);

}
