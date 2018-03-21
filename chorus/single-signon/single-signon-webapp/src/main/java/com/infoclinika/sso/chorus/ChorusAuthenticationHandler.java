package com.infoclinika.sso.chorus;

import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.UserCredentialInForm;
import com.infoclinika.sso.UserCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author andrii.loboda
 */
@Service
public final class ChorusAuthenticationHandler extends UserCredentialsAuthenticationHandler {

    private final ChorusAuthenticator authenticator;

    @Autowired
    public ChorusAuthenticationHandler(ChorusAuthenticator authenticator,
                                       ChorusProfileCreator profileCreator) {
        this.authenticator = authenticator;
        setProfileCreator(profileCreator);
    }

    @Override
    protected boolean supportsCredential(UserCredentialInForm credential) {
        return credential.getApplicationType() == ApplicationType.CHORUS;
    }

    @Override
    protected Authenticator getAuthenticator(Credential credential) {
        return authenticator;
    }

}
