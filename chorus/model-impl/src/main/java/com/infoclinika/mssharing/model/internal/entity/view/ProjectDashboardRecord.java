package com.infoclinika.mssharing.model.internal.entity.view;

import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;

import javax.persistence.*;
import java.util.Date;


/**
 * @author Herman Zamula
 *         <p/>
 *         View for mapping Project records in dashboard. Read only.
 *         See classpath*: views.sql
 */
@Table(name = "project_dashboard_record")
@Entity
@Deprecated
public class ProjectDashboardRecord {

    @Id
    private long id;

    @ManyToOne
    @JoinColumn(name = "lab_id")
    private Lab lab;
    private String name;
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
    private String areaOfResearch;

    private long experiments;
    private Date lastModification;
    private String labName;
    private boolean deleted;

    @OneToOne
    @JoinColumn(name = "id")
    private ActiveProject project;


    public ActiveProject getProject() {
        return project;
    }

    public long getId() {
        return id;
    }

    public Lab getLab() {
        return lab;
    }

    public String getName() {
        return name;
    }

    public User getCreator() {
        return creator;
    }

    public String getAreaOfResearch() {
        return areaOfResearch;
    }

    public long getExperiments() {
        return experiments;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public String getLabName() {
        return labName;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
