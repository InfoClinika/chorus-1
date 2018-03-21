package com.infoclinika.mssharing.platform.entity;

import javax.persistence.Basic;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
public abstract class AbstractAggregate extends AbstractPersistable {

    @Basic(optional = false)
    protected Date lastModification = new Date(); //united by some value to prevent NPE's

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

}