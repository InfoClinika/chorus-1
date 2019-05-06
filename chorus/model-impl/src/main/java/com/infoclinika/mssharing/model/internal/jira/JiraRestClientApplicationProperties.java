package com.infoclinika.mssharing.model.internal.jira;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URI;
import java.util.Date;

/**
 * @author Vladislav Kovchug
 */
public class JiraRestClientApplicationProperties implements ApplicationProperties {
    private final String baseUrl;

    public JiraRestClientApplicationProperties(URI jiraURI) {
        this.baseUrl = jiraURI.getPath();
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    @Nonnull
    public String getBaseUrl(UrlMode urlMode) {
        return this.baseUrl;
    }

    @Nonnull
    public String getDisplayName() {
        return "Atlassian JIRA Rest Java Client";
    }

    @Nonnull
    public String getPlatformId() {
        return "jira";
    }

    @Nonnull
    public String getVersion() {
        return "custom";
    }

    @Nonnull
    public Date getBuildDate() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public String getBuildNumber() {
        return String.valueOf(0);
    }

    public File getHomeDirectory() {
        return new File(".");
    }

    public String getPropertyValue(String s) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
