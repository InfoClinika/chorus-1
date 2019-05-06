package com.infoclinika.mssharing.model.write;

import com.infoclinika.mssharing.model.PaginationItems.PagedItem;
import com.infoclinika.mssharing.model.PaginationItems.PagedItemInfo;
import com.infoclinika.mssharing.model.jira.JiraService.IssueAttachment;

import java.util.Date;
import java.util.List;

/**
 * @author Vladislav Kovchug
 */
public interface IssuesService {

    long reportIssue(long actor, IssueType issueType, String issuePriority, String summary, String description,
                     String stepsToReproduce, List<IssueAttachment> attachments);

    long editIssue(long actor, long issueId, IssueType issueType, String issuePriority, String summary,
                   String description, String stepsToReproduce, List<IssueAttachment> attachments);

    void deleteIssue(long actor, long issueId);

    PagedItem<IssueShortDetails> readUserIssues(long actor, PagedItemInfo pagedItemInfo);

    PagedItem<IssueShortDetails> readAllIssues(long actor, PagedItemInfo pagedItemInfo);

    IssueDetails readIssueDetails(long actor, long id);

    void updateIssuesStatus();

    enum IssueType {
        BUG, IMPROVEMENT
    }

    class IssueShortDetails {
        public final long id;
        public final String name;
        public final String issueType;
        public final String owner;
        public final long ownerId;
        public final String status;
        public final String priority;
        public final Date reportedDate;
        public final String jiraKey;
        public final Long jiraId;

        public IssueShortDetails(long id, String name, String issueType, String owner, long ownerId, String status,
                                 String priority, Date reportedDate, String jiraKey, Long jiraId) {
            this.id = id;
            this.name = name;
            this.issueType = issueType;
            this.owner = owner;
            this.ownerId = ownerId;
            this.status = status;
            this.priority = priority;
            this.reportedDate = reportedDate;
            this.jiraKey = jiraKey;
            this.jiraId = jiraId;
        }
    }

    class IssueDetails {
        public final long id;
        public final String name;
        public final String issueType;
        public final String owner;
        public final long ownerId;
        public final String status;
        public final String priority;
        public final Date reportedDate;

        public final String description;
        public final String stepsToReproduce;
        public final List<IssueAttachment> attachments;

        public IssueDetails(long id, String name, String issueType, String owner, long ownerId, String status,
                            String priority, Date reportedDate, String description, String stepsToReproduce,
                            List<IssueAttachment> attachments) {
            this.id = id;
            this.name = name;
            this.issueType = issueType;
            this.owner = owner;
            this.ownerId = ownerId;
            this.status = status;
            this.priority = priority;
            this.reportedDate = reportedDate;
            this.description = description;
            this.stepsToReproduce = stepsToReproduce;
            this.attachments = attachments;
        }
    }
}
