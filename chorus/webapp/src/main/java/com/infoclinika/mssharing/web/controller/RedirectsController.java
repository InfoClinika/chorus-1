package com.infoclinika.mssharing.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.infoclinika.mssharing.web.security.AuthenticationSuccessHandlerImpl.REDIRECT_AFTER_LOGIN;


@Controller
@RequestMapping("/redirects")
public class RedirectsController {


    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectsController.class);

    @RequestMapping(method = RequestMethod.GET)
    public void handleRedirects(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("# Got a request to redirect.");
        // http://localhost:8080/redirects?url=http://localhost:8080/pages/dashboard.html&hash=/experiments/my
        final String url = request.getParameter("url");
        final String hash = request.getParameter("hash");
        final HttpSession session = request.getSession();
        LOGGER.info("# Adding redirect params into the session. Url:" + url + ", hash: " + hash + ", session: " +
            session.getId());
        final String redirectUrl = url + "#" + hash;
        session.setAttribute(REDIRECT_AFTER_LOGIN, redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
