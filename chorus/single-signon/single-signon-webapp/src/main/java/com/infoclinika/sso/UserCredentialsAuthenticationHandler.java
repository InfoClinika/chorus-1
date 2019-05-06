package com.infoclinika.sso;

import org.jasig.cas.authentication.PreventedException;

import java.security.GeneralSecurityException;

/**
 * @author andrii.loboda
 */
public abstract class UserCredentialsAuthenticationHandler
    extends AbstractAuthenticationHandler<UserCredentialInForm, UserCredentials> {

    @Override
    protected final UserCredentials convertToPac4jCredentials(UserCredentialInForm credentials) {
        final String authHandlerClassname = getClass().getSimpleName();
        return new UserCredentials(
            credentials.getUniqueID(),
            credentials.getUsername(),
            credentials.getPassword(),
            authHandlerClassname,
            credentials.getApplicationType()
        );
    }

    @Override
    protected final Class<UserCredentialInForm> getCasCredentialsType() {
        return UserCredentialInForm.class;
    }
}
