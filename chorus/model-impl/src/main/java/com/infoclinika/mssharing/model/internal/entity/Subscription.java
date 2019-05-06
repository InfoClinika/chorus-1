package com.infoclinika.mssharing.model.internal.entity;

import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import java.util.Date;

import static com.infoclinika.mssharing.model.Subscriptions.Subscription.Status;

/**
 * @author Pavel Kaplin
 */
@Embeddable
public class Subscription {
    @Enumerated
    private Status status = Status.NOT_SUBSCRIBED;

    private Date lastStatusChange;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(Date lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }
}
