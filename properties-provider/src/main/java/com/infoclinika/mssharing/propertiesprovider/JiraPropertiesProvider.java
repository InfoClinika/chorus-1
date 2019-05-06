package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JiraPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${jira.server}")
    private String jiraServerUrl;

    @Value("${jira.username}")
    private String jiraUsername;

    @Value("${jira.password}")
    private String jiraPassword;

    @Value("${jira.project.key}")
    private String jiraProjectKey;

    public String getJiraServerUrl() {
        return jiraServerUrl;
    }

    public String getJiraUsername() {
        return jiraUsername;
    }

    public String getJiraPassword() {
        return jiraPassword;
    }

    public String getJiraProjectKey() {
        return jiraProjectKey;
    }
}
