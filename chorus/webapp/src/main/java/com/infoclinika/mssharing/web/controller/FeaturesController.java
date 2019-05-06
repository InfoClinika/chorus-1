package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.infoclinika.mssharing.web.security.SsoProfile.*;

/**
 * @author Vladislav Kovchug
 */
@Controller
@RequestMapping("/features")
public class FeaturesController extends ErrorHandler {

    @Inject
    private FeaturesHelper featuresHelper;

    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    @RequestMapping(value = "/forumProperties")
    @ResponseBody
    public ForumProperties getForumProperties() {
        return new ForumProperties(
            chorusPropertiesProvider.getForumUrl(),
            chorusPropertiesProvider.isForumEnabled()
        );
    }

    @RequestMapping(value = "/sso")
    @ResponseBody
    public FeatureEnabledResponse isSSoEnabled() {
        return new FeatureEnabledResponse(false);
    }

    @RequestMapping(value = "/privateInstall")
    @ResponseBody
    public FeatureEnabledResponse isPrivateInstallEnabled() {
        return new FeatureEnabledResponse(chorusPropertiesProvider.isPrivateInstallation());
    }

    @RequestMapping(value = "/desktopUploader")
    @ResponseBody
    public Map<String, String> getDesktopUploaderUrl() {
        final Map<String, String> map = new HashMap<>();
        map.put("MAC", chorusPropertiesProvider.getDesktopUploaderMacUrl());
        map.put("WIN", chorusPropertiesProvider.getDesktopUploaderWinUrl());
        return map;
    }

    @RequestMapping(value = "/autoimporter")
    @ResponseBody
    public UrlResponse getAutoimporterUrl() {
        return new UrlResponse(chorusPropertiesProvider.getAutoimporterUrl());
    }

    @RequestMapping(value = "/alis")
    @ResponseBody
    public FeatureEnabledResponse isALISFeatureEnabled() {
        return new FeatureEnabledResponse(featuresHelper.isEnabled(ApplicationFeature.ALIS));
    }

    @RequestMapping(value = "/ltq")
    @ResponseBody
    public FeatureEnabledResponse isLTQFeatureEnabled() {
        return new FeatureEnabledResponse(featuresHelper.isEnabled(ApplicationFeature.LTQ));
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
