package com.infoclinika.sso.model.write;

import com.google.common.base.Optional;
import com.infoclinika.sso.model.ApplicationType;

/**
 * @author andrii.loboda
 */
public interface UserManagement {

    /**
     * Links user account with the specified ID to an application.
     * @throws com.infoclinika.sso.model.exception.AccountIsAlreadyLinkedException if account is already linked
     */
    long addApplicationForUser(Optional<Long> userID,
                               ApplicationType applicationType,
                               String username,
                               String userSecretKey);
}
