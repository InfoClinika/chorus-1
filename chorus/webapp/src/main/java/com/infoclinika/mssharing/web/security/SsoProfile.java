package com.infoclinika.mssharing.web.security;

/**
 * Represents SSO profile which specifies how authentication process will look like in Chorus application.
 *
 * @author Andrii Loboda
 */
public enum SsoProfile {
    SSO_PANORAMA("sso-enabled"),
    SSO_DISABLED("sso-disabled"),
    SSO_CELGENE("sso-celgene"),
    SSO_BMS("sso-bms"),
    SSO_MERCK("sso-merck"),
    SSO_MERCK_PROD("sso-merck-prod");

    private final String profileName;

    SsoProfile(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }
}
