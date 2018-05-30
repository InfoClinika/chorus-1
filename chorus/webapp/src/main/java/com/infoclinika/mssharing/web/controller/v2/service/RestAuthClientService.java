package com.infoclinika.mssharing.web.controller.v2.service;


import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class RestAuthClientService {

    public static final Logger LOGGER = Logger.getLogger(RestAuthClientService.class);

    @Value("${base.url}")
    private String baseUrl;

    @Inject
    private DetailsReader detailsReader;
    @Inject
    private ProcessingFileManagement processingFileManagement;

    @Inject
    private RuleValidator ruleValidator;

    @Data
    public static class CredentialsDTO {
        private String user;
        private String password;
    }

    @AllArgsConstructor
    public static class AuthCookieDTO {
        public String JSESSIONID;
    }


    public ResponseEntity<AuthCookieDTO> authenticateGetCookie(String user, String password){
        HttpMessageConverter<MultiValueMap<String, ?>> formHttpMessageConverter = new FormHttpMessageConverter();

        HttpMessageConverter<String> stringHttpMessageConverternew = new StringHttpMessageConverter();

        List<HttpMessageConverter<?>> messageConverters = new LinkedList<HttpMessageConverter<?>>();

        messageConverters.add(formHttpMessageConverter);
        messageConverters.add(stringHttpMessageConverternew);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("j_username", user);
        map.add("j_password", password);

        String authURL = baseUrl + "/j_spring_security_check";
        LOGGER.info(authURL + " #### Base url for Tomcat ####");
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setMessageConverters(messageConverters);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(map, requestHeaders);

        ResponseEntity<String> result = restTemplate.exchange(authURL, HttpMethod.POST, entity, String.class);
        HttpHeaders respHeaders = result.getHeaders();

        if (respHeaders.getFirst("Location").contains("login_error")) {
            LOGGER.info("Wrong credentials", new RuntimeException());
            return new ResponseEntity("Wrong credentials", HttpStatus.UNAUTHORIZED);

        }

        String authcookie = respHeaders.get("Set-Cookie").stream()
                .filter(v -> v.contains("JSESSIONID"))
                .map(v->v.substring(0, v.indexOf(';')))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Failed to parse response"));

        Date expdate = new Date ();
        expdate.setTime (expdate.getTime() + (7200 * 1000));
        String expiresCookie = authcookie + "; Expires="+expdate.toGMTString()+"; Path=/; HTTPOnly";

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).header("Set-Cookie", expiresCookie).body(new AuthCookieDTO(authcookie.substring(authcookie.indexOf('=') + 1, authcookie.length())));

    }

    public boolean isUserHasAcessToExperiment(long user, long experiment){
        boolean isUserCanReadExperiment = ruleValidator.isUserCanReadExperiment(user, experiment);

        if(isUserCanReadExperiment){
            final ExperimentItem experimentItem = detailsReader.readExperiment(user, experiment);
            return processingFileManagement.isUserLabMembership(user, experimentItem.lab);
        }
        return false;
    }

}
