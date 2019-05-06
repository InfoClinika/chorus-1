package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.platform.security.SecurityChecker;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelBasedPermissionEvaluator.class);

    @Inject
    @Named("validator")
    private SecurityChecker securityChecker;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPermission(
        Authentication authentication,
        Serializable targetId,
        String targetType,
        Object permission
    ) {
        LOGGER.debug(
            "Checking permission for {} to access {} #{}, asking for {}",
            authentication,
            targetType,
            targetId,
            permission
        );
        if (!authentication.isAuthenticated()) {
            LOGGER.debug("User is not authenticated, returning false");
            return false;
        }
        long user = ((RichUser) authentication.getPrincipal()).getId();
        if ("project".equalsIgnoreCase(targetType)) {
            if ("read".equalsIgnoreCase(permission.toString())) {
                return securityChecker.hasReadAccessOnProject(user, (Long) targetId);
            } else if ("write".equalsIgnoreCase(permission.toString())) {
                return securityChecker.hasWriteAccessOnProject(user, (Long) targetId);
            } else {
                LOGGER.warn("Unknown permission for project : {}", permission);
            }
        } else {
            LOGGER.warn("Unknown target type: {}", targetType);
        }
        return true;
    }
}
