package com.infoclinika.mssharing.web.rest.auth;

import com.google.common.base.Strings;
import com.infoclinika.auth.ChorusAuthenticationService;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.helper.SecurityHelper.UserDetails;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.web.security.UserDetailsByCasTokenServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * @author andrii.loboda
 */
public class ChorusAuthenticationServiceImpl implements ChorusAuthenticationService {
    private static final Logger LOG = LoggerFactory.getLogger(ChorusAuthenticationServiceImpl.class);

    @Context  //injected response proxy supporting multiple threads
    private HttpServletResponse response;
    @Inject
    private SecurityHelper securityHelper;
    @Inject
    private PasswordEncoder passwordEncoder;
    @Inject
    private UserManagement userManagement;

    @Override
    public AuthenticateUserResponse authenticateUser(AuthenticateUserRequest request) {

        checkNotNull(request, "Request to authenticate user should not be null.");
        checkNotNull(request.login, "Login is not specified.");
        checkNotNull(request.password, "Password is not specified.");

        checkArgument(!Strings.isNullOrEmpty(request.login.value), "Login should not be empty.");
        checkArgument(!Strings.isNullOrEmpty(request.password.value), "Password should not be empty.");

        final UserDetails userDetails = securityHelper.getUserDetailsByEmail(request.login.value);
        if (userDetails != null) {
            if (userDetails.email.equals(request.login.value) && passwordEncoder.matches(request.password.value, userDetails.password)) {
                final UserSecretKey userSecretKey = getOrGenerateSecretToken(userDetails);
                final Attributes attributes = composeAttributes(userDetails);
                final AuthenticateUserResponse response = new AuthenticateUserResponse(userSecretKey, attributes);
                return response;
            }
        }

        setUnAuthorizedStatusForResponse(response);

        return noResult(); // no additional data is needed because of error code in response(Unauthorized)
    }


    @Override
    public GetAttributesResponse getAttributes(GetAttributesRequest request) {
        final UserDetails userDetails = securityHelper.getUserDetailsByEmailAndSecretToken(request.login, request.userSecretKey.value);
        if (userDetails != null) {
            final Attributes attributes = composeAttributes(userDetails);
            return new GetAttributesResponse(attributes);
        }

        setUnAuthorizedStatusForResponse(response);

        return noResult(); // no additional data is needed because of error code in response(Unauthorized)
    }


    @Override
    public String healthCheck() {
        return HEALTH_CHECK_RESPONSE;
    }


    private UserSecretKey getOrGenerateSecretToken(UserDetails userDetails) {
        final String secretToken;
        if (userDetails.secretToken == null) {
            userManagement.generateSecretToken(userDetails.id);
            secretToken = securityHelper.getUserDetails(userDetails.id).secretToken;
        } else {
            secretToken = userDetails.secretToken;
        }
        return new UserSecretKey(secretToken);
    }

    private void setUnAuthorizedStatusForResponse(HttpServletResponse response) {
        if (response != null) {
            response.setStatus(UNAUTHORIZED.getStatusCode());
        } else {
            LOG.warn("This code should be executed only in test environment.");
        }
    }

    private static Attributes composeAttributes(UserDetails userDetails) {
        final HashMap<String, Object> attributesMap = newHashMap();
        attributesMap.put(UserDetailsByCasTokenServiceWrapper.ATTRIBUTE_CHORUS_USERNAME, userDetails.email);
        attributesMap.put(UserDetailsByCasTokenServiceWrapper.ATTRIBUTE_CHORUS_ID, userDetails.id);

        return new Attributes(attributesMap);
    }

    private static <T> T noResult() {
        return null;
    }
}
