package com.infoclinika.mssharing.platform.entity;

import javax.persistence.Basic;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author Pavel Kaplin
 */
@MappedSuperclass
public class AbstractMessage extends AbstractPersistable {

    @Basic(optional = false)
    protected Date date;

    @Lob
    @Basic(optional = false)
    protected String message;

    public AbstractMessage() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
