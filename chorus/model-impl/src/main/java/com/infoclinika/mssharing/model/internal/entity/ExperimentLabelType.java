package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author andrii.loboda
 */
@Entity
@Table(name = "ExperimentLabelType")
public class ExperimentLabelType extends AbstractPersistable<Long> {
    @Basic(optional = false)
    private String name;
    @Basic(optional = false)
    private int maxSamples;

    public ExperimentLabelType(String name, int maxSamples) {
        this.name = name;
        this.maxSamples = maxSamples;
    }

    public ExperimentLabelType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxSamples() {
        return maxSamples;
    }

    public void setMaxSamples(int maxSamples) {
        this.maxSamples = maxSamples;
    }
}
