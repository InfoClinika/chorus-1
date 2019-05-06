package com.infoclinika.mssharing.platform.entity;

import javax.persistence.Basic;
import javax.persistence.MappedSuperclass;

/**
 * @author Pavel Kaplin
 */
@MappedSuperclass
public abstract class Dictionary extends AbstractPersistable {

    @Basic(optional = false)
    private String name;

    public Dictionary(String name) {
        this.setName(name);
    }

    public Dictionary() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUnspecified() {
        return "Unspecified".equals(getName());
    }
}
