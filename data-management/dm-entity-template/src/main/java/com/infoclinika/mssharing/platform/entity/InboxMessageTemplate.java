package com.infoclinika.mssharing.platform.entity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * @author Pavel Kaplin
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class InboxMessageTemplate<U extends UserTemplate<?>> extends AbstractMessage {

    @ManyToOne(optional = false, targetEntity = UserTemplate.class)
    private U from;

    @ManyToOne(optional = false, targetEntity = UserTemplate.class)
    private U to;

    public InboxMessageTemplate() {
    }

    public InboxMessageTemplate(U from, U to, Date date, String message) {
        this.from = from;
        this.to = to;
        this.date = date;
        this.message = message;
    }

    public U getFrom() {
        return from;
    }

    public void setFrom(U from) {
        this.from = from;
    }

    public U getTo() {
        return to;
    }

    public void setTo(U to) {
        this.to = to;
    }
}
