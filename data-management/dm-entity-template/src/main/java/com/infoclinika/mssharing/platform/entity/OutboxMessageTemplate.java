package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Pavel Kaplin
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class OutboxMessageTemplate<U extends UserTemplate<?>> extends AbstractMessage {

    @ManyToOne(optional = false, targetEntity = UserTemplate.class)
    private U from;

    @Basic(optional = false)
    private String recipient;

    public OutboxMessageTemplate(String recipient, String message, Date date, U from) {
        this.recipient = recipient;
        this.message = message;
        this.date = date;
        this.from = from;
    }

    public OutboxMessageTemplate() {
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public U getFrom() {
        return from;
    }

    public void setFrom(U from) {
        this.from = from;
    }
}
