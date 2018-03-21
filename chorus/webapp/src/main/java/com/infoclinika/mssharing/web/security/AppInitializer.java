package com.infoclinika.mssharing.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.web.security.ApplicationPropertiesReader.SSO_ENABLED_PROPERTY;
import static com.infoclinika.mssharing.web.security.SsoProfile.SSO_DISABLED;
import static com.infoclinika.mssharing.web.security.SsoProfile.SSO_PANORAMA;

/**
 * Specifies SSO spring profile as an initial parameter on application startup.
 *
 * @author Andrii Loboda
 */
public class AppInitializer implements WebApplicationInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(AppInitializer.class);


    /**
     * That's the parameter Spring accesses when processes configuration properties and finds profile tag
     */
    private static final String SPRING_PROFILES_PARAM_KEY = "spring.profiles.active";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        LOG.info("Initializing Application...");

        final String ssoProfile = ApplicationPropertiesReader.getProperty(SSO_ENABLED_PROPERTY);
        checkNotNull(ssoProfile, "Can't match %s to SsoProfile", SSO_ENABLED_PROPERTY);

        LOG.info("Adding spring profile for SSO as an initial parameter, profile: " + ssoProfile);
        servletContext.setInitParameter(SPRING_PROFILES_PARAM_KEY, ssoProfile);
    }
}
