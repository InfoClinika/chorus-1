package com.infoclinika.mssharing.web.controller.v2;

import com.infoclinika.mssharing.web.controller.v2.service.RestAuthClientService;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;

@RestController
@RequestMapping("/v2/auth")
public class AuthProxyController {

    public static final Logger LOGGER = Logger.getLogger(AuthProxyController.class);

    @Inject
    private RestAuthClientService restAuthClientService;


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
        LOGGER.info(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<Object>(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/cookie", method = RequestMethod.POST)
    public ResponseEntity<RestAuthClientService.AuthCookieDTO> getAuthCookie(@RequestBody RestAuthClientService.CredentialsDTO credentials) {
        return restAuthClientService.authenticateGetCookie(credentials.getUser(), credentials.getPassword());
    }






}
