package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;

/**
 * @author Pavel Kaplin
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@AttributeOverride(name = "name", column = @Column(unique = true))
public class ExperimentType extends Dictionary {

    @Basic(optional = false)
    public boolean allowed2dLC;

    @Basic(optional = false)
    public boolean labelsAllowed;

    public ExperimentType(String name, boolean allowed2dLC, boolean labelsAllowed) {
        super(name);
        this.allowed2dLC = allowed2dLC;
        this.labelsAllowed = labelsAllowed;
    }

    public ExperimentType() {
    }
}
