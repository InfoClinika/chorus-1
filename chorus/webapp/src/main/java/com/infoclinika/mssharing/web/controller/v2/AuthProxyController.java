package com.infoclinika.mssharing.web.controller.v2;

import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by slava on 6/16/17.
 */
// TODO SN split to DTO controller service files ...
@RestController
@RequestMapping("/v2/auth")
@Api(description = "Authentication", tags = {"authentication"})
public class AuthProxyController {

    @Inject
    RestAuthClient restAuthClient;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
        return new ResponseEntity<Object>(
            ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ApiOperation(
        value = "Authorizes a user by provided credentials and returns JSESSIONID value",
        response = AuthCookieDTO.class,
        httpMethod = "POST",
        produces = "application/json",
        consumes = "application/json",
        tags = {"authentication"}
    )
    @RequestMapping(value = "/cookie", method = RequestMethod.POST)
    public AuthCookieDTO getAuthCookie(
        @RequestBody
        @ApiParam(name = "credentials", required = true, value = "User credentials") CredentialsDTO credentials
    ) {
        return restAuthClient.authenticateGetCookie(credentials.getUser(), credentials.getPassword());
    }

    public static class CredentialsDTO {
        private String user;
        private String password;

        public CredentialsDTO() {
        }

        public CredentialsDTO(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class AuthCookieDTO {
        public String JSESSIONID;

        public AuthCookieDTO(String jSessionId) {
            this.JSESSIONID = jSessionId;
        }
    }

    @Service
    private static class RestAuthClient {

        @Inject
        private ChorusPropertiesProvider chorusPropertiesProvider;

        public AuthCookieDTO authenticateGetCookie(String user, String password) {
            HttpMessageConverter<MultiValueMap<String, ?>> formHttpMessageConverter = new FormHttpMessageConverter();

            HttpMessageConverter<String> stringHttpMessageConverternew = new StringHttpMessageConverter();

            List<HttpMessageConverter<?>> messageConverters = new LinkedList<HttpMessageConverter<?>>();

            messageConverters.add(formHttpMessageConverter);
            messageConverters.add(stringHttpMessageConverternew);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("j_username", user);
            map.add("j_password", password);

            String authURL = chorusPropertiesProvider.getBaseUrl() + "/j_spring_security_check";
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.setMessageConverters(messageConverters);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(
                map,
                requestHeaders
            );

            ResponseEntity<String> result = restTemplate.exchange(authURL, HttpMethod.POST, entity, String.class);
            HttpHeaders respHeaders = result.getHeaders();

            if (respHeaders.getFirst("Location").contains("login_error")) {
                throw new RuntimeException("Wrong credentials");
            }

            String authcookie = respHeaders.get("Set-Cookie").stream()
                .filter(v -> v.contains("JSESSIONID"))
                .map(v -> v.substring(0, v.indexOf(';')))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Failed to parse response"));
            return new AuthCookieDTO(authcookie.substring(authcookie.indexOf('=') + 1, authcookie.length()));
        }

    }
}
