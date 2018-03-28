package com.infoclinika.mssharing.web.security;

import com.infoclinika.auth.ChorusAuthenticationService;
import com.infoclinika.auth.ChorusAuthenticationService.*;
import com.infoclinika.mssharing.model.helper.SecurityHelper.UserDetails;
import com.infoclinika.mssharing.web.helper.AbstractDataBasedTest;
import org.testng.annotations.Test;
import static com.infoclinika.mssharing.web.security.UserDetailsByCasTokenServiceWrapper.ATTRIBUTE_CHORUS_ID;
import static com.infoclinika.mssharing.web.security.UserDetailsByCasTokenServiceWrapper.ATTRIBUTE_CHORUS_USERNAME;
import static org.testng.Assert.*;


/**
 * @author Andrii Loboda
 */
public class ChorusAuthenticationServiceShouldTest extends AbstractDataBasedTest {

    private static final UserLogin USER_LOGIN = new UserLogin("pavel.kaplin@gmail.com");
    private static final UserPassword USER_PASSWORD = new UserPassword("pwd");



    @Test
    public void react_on_health_check() {
        final String response = chorusAuthenticationService.healthCheck();
        assertEquals(response, ChorusAuthenticationService.HEALTH_CHECK_RESPONSE);
    }

    @Test
    public void authenticate_user() {
        final AuthenticateUserRequest request = new AuthenticateUserRequest(USER_LOGIN, USER_PASSWORD);
        final AuthenticateUserResponse response = chorusAuthenticationService.authenticateUser(request);

        final UserDetails userDetails = securityHelper.getUserDetailsByEmail(request.login.value);
        assertEquals(response.userSecretKey.value, userDetails.secretToken);
        final Attributes attributes = response.attributes;
        checkAttributes(attributes, userDetails);
    }

    @Test
    public void fail_to_authenticate_user_with_wrong_login() {

        final UserLogin fakeLogin = new UserLogin("fake_login");
        final AuthenticateUserRequest request = new AuthenticateUserRequest(fakeLogin, USER_PASSWORD);
        final AuthenticateUserResponse response = chorusAuthenticationService.authenticateUser(request);
        assertNull(response);
    }

    @Test
    public void fail_to_authenticate_user_with_wrong_password() {

        final UserPassword fakePassword = new UserPassword("fake_password");
        final AuthenticateUserRequest request = new AuthenticateUserRequest(USER_LOGIN, fakePassword);
        final AuthenticateUserResponse response = chorusAuthenticationService.authenticateUser(request);
        assertNull(response);
    }

    @Test
    public void get_attributes_of_authenticated_user() {
        //authenticate user to ensure that secretToken is generated
        final AuthenticateUserRequest authRequest = new AuthenticateUserRequest(USER_LOGIN, USER_PASSWORD);
        chorusAuthenticationService.authenticateUser(authRequest);

        final UserDetails userDetails = securityHelper.getUserDetailsByEmail(authRequest.login.value);
        final UserSecretKey userSecretKey = new UserSecretKey(userDetails.secretToken);
        final GetAttributesRequest request = new GetAttributesRequest(authRequest.login.value, userSecretKey);
        final GetAttributesResponse response = chorusAuthenticationService.getAttributes(request);

        checkAttributes(response.attributes, userDetails);
    }

    @Test
    public void get_attributes_of_user_without_secret_token() {

        final UserDetails userDetailsForId = securityHelper.getUserDetailsByEmail(USER_LOGIN.value);
        userManagement.cleanSecretToken(userDetailsForId.id);
        final UserDetails userDetails = securityHelper.getUserDetailsByEmail(USER_LOGIN.value);
        assertNull(userDetails.secretToken);
        final UserSecretKey userSecretKey = new UserSecretKey(userDetails.secretToken);
        final GetAttributesRequest request = new GetAttributesRequest(USER_LOGIN.value, userSecretKey);

        final GetAttributesResponse response = chorusAuthenticationService.getAttributes(request);

        assertNull(response);
    }

    @Test
    public void get_attributes_of_user_with_wrong_token() {
        final UserSecretKey userSecretKey = new UserSecretKey("fake token");
        final GetAttributesRequest request = new GetAttributesRequest(USER_LOGIN.value, userSecretKey);
        final GetAttributesResponse response = chorusAuthenticationService.getAttributes(request);

        assertNull(response);
    }


    private static void checkAttributes(Attributes attributes, UserDetails userDetails) {
        assertTrue(attributes.attributes.containsKey(ATTRIBUTE_CHORUS_USERNAME));
        assertTrue(attributes.attributes.containsKey(ATTRIBUTE_CHORUS_ID));
        assertEquals(attributes.attributes.size(), 2);


        assertEquals(userDetails.id, attributes.attributes.get(ATTRIBUTE_CHORUS_ID));
        assertEquals(userDetails.email, attributes.attributes.get(ATTRIBUTE_CHORUS_USERNAME));
    }
}
