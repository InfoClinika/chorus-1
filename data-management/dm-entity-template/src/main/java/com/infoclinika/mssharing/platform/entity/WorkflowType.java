package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;

/**
 * @author Pavel Kaplin
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class WorkflowType extends Dictionary {

    public boolean allow2dLc;

    @ManyToOne
    @JoinColumn(name = "experiment_type_id", insertable = false, updatable = false, nullable = false)
    public ExperimentType experimentType;

    public WorkflowType(String name, boolean allow2dLc, ExperimentType experimentType) {
        super(name);
        this.allow2dLc = allow2dLc;
        this.experimentType = experimentType;
    }

    public WorkflowType() {
    }
}
