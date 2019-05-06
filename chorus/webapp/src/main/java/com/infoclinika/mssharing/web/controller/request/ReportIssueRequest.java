package com.infoclinika.mssharing.web.controller.request;

import com.infoclinika.mssharing.model.jira.JiraService;
import com.infoclinika.mssharing.model.write.IssuesService.IssueType;

import java.util.List;
import java.util.Objects;

/**
 * @author Vladislav Kovchug
 */
public class ReportIssueRequest {
    private String title;
    private IssueType issueType;
    private String priority;
    private String description;
    private String stepsToReproduce;
    private List<JiraService.IssueAttachment> attachments;

    public ReportIssueRequest(
        String title, IssueType issueType, String priority, String description, String stepsToReproduce,
        List<JiraService.IssueAttachment> attachments
    ) {
        this.title = title;
        this.issueType = issueType;
        this.priority = priority;
        this.description = description;
        this.stepsToReproduce = stepsToReproduce;
        this.attachments = attachments;
    }

    public ReportIssueRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStepsToReproduce() {
        return stepsToReproduce;
    }

    public void setStepsToReproduce(String stepsToReproduce) {
        this.stepsToReproduce = stepsToReproduce;
    }

    public List<JiraService.IssueAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<JiraService.IssueAttachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, issueType, priority, description, stepsToReproduce, attachments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReportIssueRequest that = (ReportIssueRequest) o;
        return Objects.equals(title, that.title) &&
            Objects.equals(issueType, that.issueType) &&
            Objects.equals(priority, that.priority) &&
            Objects.equals(description, that.description) &&
            Objects.equals(stepsToReproduce, that.stepsToReproduce) &&
            Objects.equals(attachments, that.attachments);
    }

    @Override
    public String toString() {
        return "ReportIssueRequest{" +
            "title='" + title + '\'' +
            ", issueType=" + issueType +
            ", priority=" + priority +
            ", description='" + description + '\'' +
            ", stepsToReproduce='" + stepsToReproduce + '\'' +
            ", attachments=" + attachments +
            '}';
    }
}
