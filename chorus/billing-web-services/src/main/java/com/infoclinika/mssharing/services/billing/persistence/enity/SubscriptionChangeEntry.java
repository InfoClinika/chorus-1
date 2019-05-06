package com.infoclinika.mssharing.services.billing.persistence.enity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author timofei.kasianov 1/11/17
 */
@Entity
@Table(name = "subscription_change_entry")
public class SubscriptionChangeEntry extends AbstractPersistable<Long> {

    @Basic(optional = false)
    private long userId;
    @Basic(optional = false)
    private long labId;
    @Basic(optional = false)
    private String subscription;
    @Basic(optional = false)
    private long timestamp;

    public SubscriptionChangeEntry(long userId, long labId, String subscription, long timestamp) {
        this.userId = userId;
        this.labId = labId;
        this.subscription = subscription;
        this.timestamp = timestamp;
    }

    public SubscriptionChangeEntry() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getLabId() {
        return labId;
    }

    public void setLabId(long labId) {
        this.labId = labId;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SubscriptionChangeEntry that = (SubscriptionChangeEntry) o;
        return userId == that.userId &&
            labId == that.labId &&
            timestamp == that.timestamp &&
            Objects.equals(subscription, that.subscription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId, labId, subscription, timestamp);
    }
}
