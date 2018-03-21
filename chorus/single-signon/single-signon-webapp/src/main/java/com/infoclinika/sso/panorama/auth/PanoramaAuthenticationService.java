package com.infoclinika.sso.panorama.auth;

import com.google.common.base.MoreObjects;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author Andrii Loboda
 */
@Path("/")
@Produces({"application/json"})
public interface PanoramaAuthenticationService {

    @POST
    @Path("loginApi.view")
    @Consumes(MediaType.APPLICATION_JSON)
    AuthenticateUserResponse authenticateUser(AuthenticateUserRequest request);

    class AuthenticateUserRequest {
        public final String email;
        public final String password;

        public AuthenticateUserRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("email", email)
                    .toString();
        }
    }

    class AuthenticateUserResponse {
        public boolean success;
        /**
         * All fields  below will be specified to null if authentication was NOT successful
         */
        public String returnUrl;
        public Map<String, Object> user;


        /**
         * All fields  below will be specified to null if authentication was successful
         */
        public String errorMessage;

        public AuthenticateUserResponse() {
        }

        public AuthenticateUserResponse(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
