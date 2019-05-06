package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author andrii.loboda
 */
@Entity
@Table(name = "ExperimentLabel")
public class ExperimentLabel extends AbstractPersistable<Long> {
    @Basic(optional = false)
    private String acid;
    @Basic(optional = false)
    private String name;
    @ManyToOne(optional = false)
    private ExperimentLabelType type;

    public ExperimentLabel(String acid, String name, ExperimentLabelType type) {
        this.acid = acid;
        this.name = name;
        this.type = type;
    }

    public ExperimentLabel() {
    }

    public String getAcid() {
        return acid;
    }

    public void setAcid(String acid) {
        this.acid = acid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExperimentLabelType getType() {
        return type;
    }

    public void setType(ExperimentLabelType type) {
        this.type = type;
    }
}

