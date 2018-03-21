package com.infoclinika.sso.panorama;

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
public final class PanoramaAuthenticationHandler extends UserCredentialsAuthenticationHandler {
    private final PanoramaAuthenticator authenticator;

    @Autowired
    public PanoramaAuthenticationHandler(PanoramaAuthenticator authenticator, PanoramaProfileCreator profileCreator) {
        this.authenticator = authenticator;
        setProfileCreator(profileCreator);
    }

    @Override
    protected boolean supportsCredential(UserCredentialInForm credential) {
        return credential.getApplicationType() == ApplicationType.PANORAMA;
    }

    @Override
    protected Authenticator getAuthenticator(Credential credential) {
        return authenticator;
    }
}
