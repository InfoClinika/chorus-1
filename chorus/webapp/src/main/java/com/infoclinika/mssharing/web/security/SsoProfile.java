package com.infoclinika.mssharing.web.security;

/**
 * Represents SSO profile which specifies how authentication process will look like in Chorus application.
 *
 * @author Andrii Loboda
 */
public enum SsoProfile {
    SSO_DISABLED("sso-disabled");

    private final String profileName;

    SsoProfile(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }
}
