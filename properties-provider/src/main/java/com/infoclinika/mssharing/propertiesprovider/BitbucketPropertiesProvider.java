package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BitbucketPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${issues.endpoint}")
    private String issuesEndpoint;

    @Value("${issues.bitbucket.username}")
    private String issuesBitbucketUsername;

    @Value("${issues.bitbucket.password}")
    private String issuesBitbucketPassword;

    @Value("${issue.support.email}")
    private String issueSupportEmail;

    @Value("${issues.component.name}")
    private String issuesComponentName;

    public String getIssuesEndpoint() {
        return issuesEndpoint;
    }

    public String getIssuesBitbucketUsername() {
        return issuesBitbucketUsername;
    }

    public String getIssuesBitbucketPassword() {
        return issuesBitbucketPassword;
    }

    public String getIssueSupportEmail() {
        return issueSupportEmail;
    }

    public String getIssuesComponentName() {
        return issuesComponentName;
    }
}
