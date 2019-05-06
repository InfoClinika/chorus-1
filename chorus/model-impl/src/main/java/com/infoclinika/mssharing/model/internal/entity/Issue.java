package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.platform.entity.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Vladislav Kovchug
 */

@Entity
@Table(name = "issue")
public class Issue extends AbstractPersistable {

    @Column(name = "name")
    private String name;

    @Column(name = "jira_id")
    private Long jiraId;

    @Column(name = "jira_key")
    private String jiraKey;

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private IssueType type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "status")
    private String status;

    @Column(name = "priority")
    private String priority;

    @Column(name = "reported_date")
    private Date reportedDate;

    @Column(name = "description")
    @Lob
    private String description;

    @Column(name = "steps_to_reproduce")
    @Lob
    private String stepsToReproduce;

    public Issue(
        String name,
        Long jiraId,
        String jiraKey,
        IssueType type,
        User owner,
        String status,
        String priority,
        Date reportedDate,
        String description,
        String stepsToReproduce
    ) {
        this.name = name;
        this.jiraId = jiraId;
        this.jiraKey = jiraKey;
        this.type = type;
        this.owner = owner;
        this.status = status;
        this.priority = priority;
        this.reportedDate = reportedDate;
        this.description = description;
        this.stepsToReproduce = stepsToReproduce;
    }

    public Issue() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getJiraId() {
        return jiraId;
    }

    public void setJiraId(Long jiraId) {
        this.jiraId = jiraId;
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public void setJiraKey(String jiraKey) {
        this.jiraKey = jiraKey;
    }

    public IssueType getType() {
        return type;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(Date reportedDate) {
        this.reportedDate = reportedDate;
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

    public enum IssueType {
        BUG,
        IMPROVEMENT
    }
}
