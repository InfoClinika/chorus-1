package com.infoclinika.mssharing.model.jira;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Vladislav Kovchug
 */
public interface JiraService {

    JiraIssueDetails reportIssue(long actor, Long issueType, Long issuePriority, String summary, String description,
                                 String stepsToReproduce, List<IssueAttachment> attachments);

    JiraIssueDetails editIssue(long actor, String issueKey, Long issueType, Long issuePriority, String summary,
                               String description, String stepsToReproduce, List<IssueAttachment> attachments);

    void addComment(long actor, String issueKey, String message);

    List<BasicIssueProperty> getPriorities();

    List<BasicIssueProperty> getIssueTypes();

    Map<Long, JiraIssueDetails> getIssuesDetails(List<Long> ids);

    List<IssueAttachment> getIssueAttachments(String issueKey);

    class BasicIssueProperty {
        public final Long id;
        public final String name;
        public final String alias;

        public BasicIssueProperty(Long id, String name, String alias) {
            this.id = id;
            this.name = name;
            this.alias = alias;
        }

        @Override
        public String toString() {
            return "BasicIssueProperty{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                '}';
        }
    }

    class IssueAttachment {
        public final String name;
        public final String base64Content;
        public final String type;
        public final String thumbnailBase64Content;

        public IssueAttachment(String name, String base64Content, String type, String thumbnailBase64Content) {
            this.name = name;
            this.base64Content = base64Content;
            this.type = type;
            this.thumbnailBase64Content = thumbnailBase64Content;
        }

        public IssueAttachment() {
            name = null;
            base64Content = null;
            type = null;
            thumbnailBase64Content = null;
        }

        @Override
        public String toString() {
            return "IssueAttachment{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IssueAttachment that = (IssueAttachment) o;
            return Objects.equals(name, that.name) &&
                Objects.equals(base64Content, that.base64Content) &&
                Objects.equals(type, that.type) &&
                Objects.equals(thumbnailBase64Content, that.thumbnailBase64Content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, base64Content, type, thumbnailBase64Content);
        }
    }

    class JiraIssueDetails {
        public final long id;
        public final String key;
        public final String status;

        public JiraIssueDetails(long id, String key, String status) {
            this.id = id;
            this.key = key;
            this.status = status;
        }

        public JiraIssueDetails() {
            id = 0;
            key = null;
            status = null;
        }
    }

}
