package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "copy_project_request")
public class CopyProjectRequest extends AbstractPersistable<Long> {

    @ManyToOne
    private User receiver;
    @ManyToOne
    private User sender;
    @ManyToOne
    private ActiveProject project;
    @Column(name = "date_sent")
    private Date dateSent;

    public CopyProjectRequest(User receiver, User sender, ActiveProject project, Date dateSent) {
        this.receiver = receiver;
        this.sender = sender;
        this.project = project;
        this.dateSent = dateSent;
    }

    protected CopyProjectRequest() {
    }

    public ActiveProject getProject() {
        return project;
    }

    public User getReceiver() {
        return receiver;
    }

    public User getSender() {
        return sender;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }
}
