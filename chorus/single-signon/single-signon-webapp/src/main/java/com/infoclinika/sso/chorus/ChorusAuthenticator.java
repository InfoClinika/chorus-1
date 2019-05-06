package com.infoclinika.sso.chorus;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.infoclinika.auth.ChorusAuthenticationService;
import com.infoclinika.auth.ChorusAuthenticationService.AuthenticateUserRequest;
import com.infoclinika.auth.ChorusAuthenticationService.AuthenticateUserResponse;
import com.infoclinika.auth.ChorusAuthenticationService.UserLogin;
import com.infoclinika.auth.ChorusAuthenticationService.UserPassword;
import com.infoclinika.sso.UserCredentials;
import com.infoclinika.sso.model.ApplicationType;
import com.infoclinika.sso.model.exception.ApplicationAuthenticationServerErrorException;
import com.infoclinika.sso.model.write.UserManagement;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;

/**
 * @author andrii.loboda
 */
@Service
public final class ChorusAuthenticator implements Authenticator<UserCredentials> {
    private static final Logger LOG = LoggerFactory.getLogger(ChorusAuthenticator.class);

    @Resource(name = "chorusAuthenticationService")
    private ChorusAuthenticationService chorusAuthenticationService;
    @Inject
    private UserManagement userManagement;

    @Override
    public void validate(UserCredentials credentials) {

        final UserLogin login = new UserLogin(credentials.getUsername());
        final UserPassword password = new UserPassword(credentials.getPassword());
        final AuthenticateUserRequest request = new AuthenticateUserRequest(login, password);

        try {
            final AuthenticateUserResponse response = chorusAuthenticationService.authenticateUser(request);
            userManagement.addApplicationForUser(
                Optional.fromNullable(credentials.getUniqueID()),
                ApplicationType.CHORUS,
                credentials.getUsername(),
                response.userSecretKey.value
            );

        } catch (NotAuthorizedException notAuthorized) {
            final String errorMessage =
                "Chorus: credentials don't match to authenticate, login: " + credentials.getUsername() + ", message:" +
                    notAuthorized.getMessage();
            throw new CredentialsException(errorMessage, notAuthorized);
        } catch (javax.ws.rs.InternalServerErrorException e) {
            final String errorMessage = "Exception during authenticating at Chorus endpoint, message:" + e.getMessage();
            LOG.error(errorMessage, e);
            throw new ApplicationAuthenticationServerErrorException(errorMessage, e);
        } catch (RuntimeException e) {
            LOG.error("Exception during authentication using ChorusAuthenticator, message:" + e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }


}
