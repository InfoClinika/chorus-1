package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.platform.security.SecurityChecker;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import org.apache.log4j.Logger;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * @author Pavel Kaplin
 */
@Component
public class ModelBasedPermissionEvaluator implements PermissionEvaluator {

    private static final Logger LOG  = Logger.getLogger(ModelBasedPermissionEvaluator.class);

    @Inject
    @Named("validator")
    private SecurityChecker securityChecker;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        LOG.debug("Checking permission for " + authentication + " to access " + targetType + "#" + targetId + ", asking for " + permission);
        if (!authentication.isAuthenticated()) {
            LOG.debug("User is not authenticated, returning false");
            return false;
        }
        long user = ((RichUser) authentication.getPrincipal()).getId();
        if ("project".equalsIgnoreCase(targetType)) {
            if ("read".equalsIgnoreCase(permission.toString())) {
                return securityChecker.hasReadAccessOnProject(user, (Long) targetId);
            }
            else if ("write".equalsIgnoreCase(permission.toString())) {
                return securityChecker.hasWriteAccessOnProject(user, (Long) targetId);
            }
            else {
                LOG.warn("Unknown permission for project :" + permission);
            }
        }
        else {
            LOG.warn("Unknown target type: " + targetType);
        }
        return true;
    }
}
