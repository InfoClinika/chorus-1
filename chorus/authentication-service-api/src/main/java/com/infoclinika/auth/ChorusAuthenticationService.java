package com.infoclinika.auth;

import com.google.common.base.MoreObjects;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author andrii.loboda
 */
@Path("/")
@Produces({"application/json"})
public interface ChorusAuthenticationService {
    String HEALTH_CHECK_RESPONSE = "If you see this message => service works.";

    /**
     * Authenticates user with given credentials.
     *
     * @return In case of successful authentication returns Single Sign-On(SSO) session attributes and user secret key with status 200.
     * The key is be stored inside SSO server and is used for repeated attribute fetch {@link #getAttributes}
     * In case of authentication failure 401 code(Unauthorized) is returned
     */
    @POST
    @Path("authenticateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    AuthenticateUserResponse authenticateUser(AuthenticateUserRequest request);


    /**
     * Gets attributes for Single Sign-On(SSO) session with given credentials(login and secret key)
     *
     * @return In case given credentials matches the user, returns the same set of SSO session attributes
     * as if {@link #authenticateUser} would have been used with status 200.
     * In case given credentials don't match the user, 401 code(Unauthorized) is returned
     */
    @POST
    @Path("getAttributes")
    @Consumes(MediaType.APPLICATION_JSON)
    GetAttributesResponse getAttributes(GetAttributesRequest request);

    @GET
    @Path("healthCheck")
    String healthCheck();


    class AuthenticateUserRequest {
        public UserLogin login;
        public UserPassword password;

        public AuthenticateUserRequest(UserLogin login, UserPassword password) {
            this.login = login;
            this.password = password;
        }

        AuthenticateUserRequest() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("login", login)
                    .toString();
        }
    }

    class AuthenticateUserResponse {
        public UserSecretKey userSecretKey;
        public Attributes attributes;

        public AuthenticateUserResponse(UserSecretKey userSecretKey, Attributes attributes) {
            this.userSecretKey = userSecretKey;
            this.attributes = attributes;
        }

        AuthenticateUserResponse() {
        }
    }

    class GetAttributesRequest {
        public String login;
        public UserSecretKey userSecretKey;

        public GetAttributesRequest(String login, UserSecretKey userSecretKey) {
            this.login = login;
            this.userSecretKey = userSecretKey;
        }

        public GetAttributesRequest() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("login", login)
                    .toString();
        }
    }

    class GetAttributesResponse {
        public Attributes attributes;

        public GetAttributesResponse(Attributes attributes) {
            this.attributes = attributes;
        }

        GetAttributesResponse() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("attributes", attributes)
                    .toString();
        }
    }

    class Attributes {
        public Map<String, Object> attributes;

        public Attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        Attributes() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("attributes", attributes)
                    .toString();
        }
    }

    class UserSecretKey {
        public String value;

        public UserSecretKey(String value) {
            this.value = value;
        }

        UserSecretKey() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("value", value)
                    .toString();
        }
    }

    class UserLogin {
        public String value;

        public UserLogin(String value) {
            this.value = value;
        }

        UserLogin() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("value", value)
                    .toString();
        }
    }

    class UserPassword {
        public String value;

        public UserPassword(String value) {
            this.value = value;
        }

        UserPassword() {
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("value", value)
                    .toString();
        }
    }

}
