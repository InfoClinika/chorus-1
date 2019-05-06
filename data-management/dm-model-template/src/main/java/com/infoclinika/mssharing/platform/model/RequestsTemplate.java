package com.infoclinika.mssharing.platform.model;

/**
 * @author Herman Zamula
 */

import com.google.common.collect.ImmutableSortedSet;

import java.util.Date;

/**
 * @author Pavel Kaplin
 */
public interface RequestsTemplate {

    ImmutableSortedSet<InboxItem> getInboxItems(long user);

    ImmutableSortedSet<OutboxItem> getOutboxItems(long user);

    void removeInboxItem(long actor, String request);

    void removeOutboxItem(long actor, String request);

    void approve(long actor, String request);

    void refuse(long actor, String request, String comment);

    void addOutboxItem(long from, String to, String message, Date date);

    class InboxItem {
        public final String id;
        public final String from;
        public final String description;
        public final Date sent;
        public final Actions availableActions;

        public InboxItem(String id, String from, String description, Date sent, Actions availableActions) {
            this.id = id;
            this.from = from;
            this.description = description;
            this.sent = sent;
            this.availableActions = availableActions;
        }

        public enum Actions {
            APPROVE_REFUSE,
            OK
        }

    }

    class OutboxItem {

        public final String to;
        public final String id;
        public final String description;
        public final Date sent;

        public OutboxItem(String id, String to, String description, Date sent) {
            this.id = id;
            this.sent = sent;
            this.description = description;
            this.to = to;
        }

    }
}
