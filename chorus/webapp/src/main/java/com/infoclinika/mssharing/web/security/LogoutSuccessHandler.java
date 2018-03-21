package com.infoclinika.mssharing.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    @Inject
    private SessionRegistry sessionRegistry;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        HttpSession session = request.getSession();
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(session.getId());

        // we need to mark session as expired to be able to clean DC visualizer cache. See SearchResultsViewCacheCleaner.cleanCache().
        if (sessionInformation != null){
            sessionInformation.expireNow();
            session.invalidate();
        }

        super.onLogoutSuccess(request, response, authentication);
    }
}
