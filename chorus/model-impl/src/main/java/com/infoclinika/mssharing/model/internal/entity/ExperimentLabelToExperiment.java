package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

/**
 * @author andrii.loboda
 */
@Entity
@Table(name = "ExperimentLabelToExperiment")
public class ExperimentLabelToExperiment extends AbstractPersistable<Long> {
    @ManyToOne(optional = false)
    private AbstractExperiment experiment;

    @ManyToOne(optional = false)
    private ExperimentLabel experimentLabel;

    @Enumerated(value = EnumType.STRING)
    @Basic(optional = false)
    private ExperimentSampleType experimentLabelMixType;


    public ExperimentLabelToExperiment(AbstractExperiment experiment, ExperimentLabel experimentLabel, ExperimentSampleType experimentLabelMixType) {
        this.experiment = experiment;
        this.experimentLabel = experimentLabel;
        this.experimentLabelMixType = experimentLabelMixType;
    }

    /*automatically generated getters, setters, default package constructor*/

    public ExperimentLabelToExperiment() {
    }

    public AbstractExperiment getExperiment() {
        return experiment;
    }

    public void setExperiment(AbstractExperiment experiment) {
        this.experiment = experiment;
    }

    public ExperimentLabel getExperimentLabel() {
        return experimentLabel;
    }

    public void setExperimentLabel(ExperimentLabel experimentLabel) {
        this.experimentLabel = experimentLabel;
    }

    public ExperimentSampleType getExperimentLabelMixType() {
        return experimentLabelMixType;
    }

    public void setExperimentLabelMixType(ExperimentSampleType experimentLabelMixType) {
        this.experimentLabelMixType = experimentLabelMixType;
    }
}
