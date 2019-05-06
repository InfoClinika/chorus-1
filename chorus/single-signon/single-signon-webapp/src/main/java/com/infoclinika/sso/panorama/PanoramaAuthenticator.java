package com.infoclinika.sso.panorama;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.infoclinika.sso.UserCredentials;
import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.write.UserManagement;
import com.infoclinika.sso.panorama.auth.PanoramaAuthenticationService;
import com.infoclinika.sso.panorama.auth.PanoramaAuthenticationService.AuthenticateUserRequest;
import com.infoclinika.sso.panorama.auth.PanoramaAuthenticationService.AuthenticateUserResponse;
import org.apache.commons.io.IOUtils;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author andrii.loboda
 */
@Service
public final class PanoramaAuthenticator implements Authenticator<UserCredentials> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PanoramaAuthenticator.class);
    @Inject
    private UserManagement userManagement;
    @Resource(name = "panoramaAuthenticationService")
    private PanoramaAuthenticationService panoramaAuthenticationService;

    @Override
    public void validate(UserCredentials credentials) {

        try {
            final AuthenticateUserResponse response = authenticatePanoramaUser(credentials);
            if (!response.success) {
                throw new NotAuthorizedException(
                    "Authentication failed: success flag is false, error message: " + response.errorMessage
                );
            }
            userManagement.addApplicationForUser(
                Optional.fromNullable(credentials.getUniqueID()),
                ApplicationType.PANORAMA,
                credentials.getUsername(),
                credentials.getPassword()   //change password to real secret token once it available.
            );
        } catch (NotAuthorizedException notAuthorized) {
            final String errorMessage =
                "Panorama: credentials don't match to authenticate, login: " + credentials.getUsername() +
                    ", message:" + notAuthorized.getMessage();
            throw new CredentialsException(errorMessage, notAuthorized);
        } catch (RuntimeException e) {
            LOGGER.error("Exception during authenticating at Panorama endpoint, message:" + e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    private AuthenticateUserResponse authenticatePanoramaUser(UserCredentials credentials) {
        try {
            final AuthenticateUserRequest request =
                new AuthenticateUserRequest(credentials.getUsername(), credentials.getPassword());
            final AuthenticateUserResponse response = panoramaAuthenticationService.authenticateUser(request);
            return response;
        } catch (BadRequestException e) {
            final InputStream entity = (InputStream) e.getResponse().getEntity();
            try {
                final String bodyInString = IOUtils.toString(entity, StandardCharsets.UTF_8);
                return new AuthenticateUserResponse(bodyInString);
            } catch (IOException e1) {
                final String errorMessage = "Cannot read response, error message: " + e1.getMessage();
                LOGGER.error(errorMessage, e1);
                return new AuthenticateUserResponse(errorMessage);
            }
        }
    }
}
