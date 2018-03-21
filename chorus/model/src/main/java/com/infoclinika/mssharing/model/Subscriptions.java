package com.infoclinika.mssharing.model;

import java.util.Date;

/**
 * @author Pavel Kaplin
 */
public interface Subscriptions {
    Subscription get(long actor);

    void update(long actor, Subscription.Status status);

    class Subscription {
        public enum Status {NOT_SUBSCRIBED, SUBSCRIBED, PENDING}

        public final Status status;

        public final Date lastChange;

        public Subscription(Status status, Date lastChange) {
            this.status = status;
            this.lastChange = lastChange;
        }
    }
}
