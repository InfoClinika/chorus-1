package com.infoclinika.sso.model.write;

import com.google.common.base.Optional;
import com.infoclinika.sso.model.ApplicationType;

/**
 * @author andrii.loboda
 */
public interface UserManagement {

    /**
     * @throws com.infoclinika.sso.model.exception.AccountIsAlreadyLinkedException
     */
    long addApplicationForUser(Optional<Long> userID, ApplicationType applicationType, String username, String userSecretKey);
}
