package com.infoclinika.mssharing.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.infoclinika.mssharing.web.security.SsoProfile.*;
import static com.infoclinika.mssharing.web.security.SsoProfile.SSO_MERCK_PROD;

/**
 * @author Vladislav Kovchug
 */
@Controller
@RequestMapping("/features")
public class FeaturesController extends ErrorHandler {
    @Value("${forum.url}")
    private String forumUrl;
    @Value("${forum.enabled}")
    private boolean forumEnabled;
    @Value("${chorus.sso}")
    private String ssoProfile;
    @Value("${private.installation}")
    private boolean privateInstallEnabled;
    @Value("${desktop.uploader.url}")
    private String desktopUpladerUrl;
    @Value("${autoimporter.url}")
    private String autoimportetUrl;

    @RequestMapping(value = "/forumProperties")
    @ResponseBody
    public ForumProperties getForumProperties() {
        return new ForumProperties(forumUrl, forumEnabled);
    }

    @RequestMapping(value = "/sso")
    @ResponseBody
    public FeatureEnabledResponse isSSoEnabled() {

        return new FeatureEnabledResponse(
                ssoProfile.equals(SSO_BMS.getProfileName()) ||
                ssoProfile.equals(SSO_CELGENE.getProfileName()) ||
				ssoProfile.equals(SSO_MERCK_PROD.getProfileName()) ||
                ssoProfile.equals(SSO_MERCK.getProfileName()));
    }

    @RequestMapping(value = "/privateInstall")
    @ResponseBody
    public FeatureEnabledResponse isPrivateInstallEnabled() {
        return new FeatureEnabledResponse(privateInstallEnabled);
    }

    @RequestMapping(value = "/desktopUploader")
    @ResponseBody
    public UrlResponse getDesktopUploaderUrl() {
        return new UrlResponse(desktopUpladerUrl);
    }

    @RequestMapping(value = "/autoimporter")
    @ResponseBody
    public UrlResponse getAutoimporterUrl() {
        return new UrlResponse(autoimportetUrl);
    }


    public static class UrlResponse {
        public final String url;

        public UrlResponse(String url) {
            this.url = url;
        }
    }

    public static class FeatureEnabledResponse {
        public final boolean enabled;

        public FeatureEnabledResponse(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ForumProperties {
        public final String url;
        public final boolean enabled;

        public ForumProperties(String url, boolean enabled) {
            this.url = url;
            this.enabled = enabled;
        }
    }
}
