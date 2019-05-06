package com.infoclinika.mssharing.platform.security;

import java.util.Set;

/**
 * @author Pavel Kaplin
 */
public interface SecurityChecker {
    boolean hasReadAccessOnProject(long user, long projectId);

    boolean hasWriteAccessOnProject(long actor, long projectId);

    Set<Long> getProjectsWithReadAccess(long actor);

}
