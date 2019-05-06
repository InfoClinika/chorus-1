package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author andrii.loboda
 */
@Component
public class AuthenticationSuccessHandlerImpl extends SimpleUrlAuthenticationSuccessHandler {
    public static final String REDIRECT_AFTER_LOGIN = "redirectAfterLogin";
    private static final Log LOGGER = LogFactory.getLog(AuthenticationSuccessHandlerImpl.class);

    private RequestCache requestCache = new HttpSessionRequestCache();

    private final UserManagement userManagement;
    private final ChorusPropertiesProvider chorusPropertiesProvider;

    @Inject
    public AuthenticationSuccessHandlerImpl(UserManagement userManagement,
                                            ChorusPropertiesProvider chorusPropertiesProvider) {
        this.userManagement = userManagement;
        this.chorusPropertiesProvider = chorusPropertiesProvider;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request, HttpServletResponse response,
        Authentication authentication
    ) throws ServletException, IOException {
        LOGGER.info("# Handling successfull authentication event.");
        final SavedRequest savedRequest = requestCache.getRequest(request, response);

        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(chorusPropertiesProvider.getSessionTimeout());

        final RichUser principal = (RichUser) authentication.getPrincipal();
        userManagement.resetUnsuccessfulLoginAttempts(principal.getId());

        String targetUrlParameter = getTargetUrlParameter();

        if (isAlwaysUseDefaultTargetUrl() ||
            (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {

            LOGGER.info("# Using default target URL.");
            requestCache.removeRequest(request, response);
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        clearAuthenticationAttributes(request);

        String sessionRedirectParam = (String) session.getAttribute(REDIRECT_AFTER_LOGIN);
        LOGGER.info("# Session attributes: " + session.getAttributeNames());
        LOGGER.info("# Session redirect param value: " + sessionRedirectParam + ", session: " + session);

        // Use the DefaultSavedRequest URL
        String redirectUrl = request.getParameter(REDIRECT_AFTER_LOGIN);

        LOGGER.info("# Redirect URL from original request: " + redirectUrl);

        String redirectFromSavedRequest = savedRequest != null ? savedRequest.getRedirectUrl() : "";

        LOGGER.info("# Redirect URL from saved request: " + redirectFromSavedRequest);

        redirectUrl = StringUtils.isEmpty(redirectUrl) ? sessionRedirectParam : redirectUrl;
        String targetUrl = StringUtils.isEmpty(redirectUrl) ? redirectFromSavedRequest : redirectUrl;

        if (StringUtils.isEmpty(targetUrl)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        LOGGER.info("# Redirecting to URL: " + targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}
