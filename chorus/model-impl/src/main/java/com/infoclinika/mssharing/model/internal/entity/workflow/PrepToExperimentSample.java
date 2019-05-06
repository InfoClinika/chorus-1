package com.infoclinika.mssharing.model.internal.entity.workflow;

import com.infoclinika.mssharing.model.internal.entity.ExperimentPreparedSample;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSampleType;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

/**
 * @author andrii.loboda
 */
@Entity
public class PrepToExperimentSample extends AbstractPersistable<Long> {
    @ManyToOne(optional = false)
    private ExperimentPreparedSample preparedSample;
    @ManyToOne(optional = false)
    private ExperimentSample experimentSample;
    @Enumerated(value = EnumType.STRING)
    @Basic(optional = false)
    private ExperimentSampleType type;

    public PrepToExperimentSample(
        ExperimentPreparedSample preparedSample,
        ExperimentSample experimentSample,
        ExperimentSampleType type
    ) {
        this.preparedSample = preparedSample;
        this.experimentSample = experimentSample;
        this.type = type;
    }

    /*automatically generated getters, setters, default package constructor*/
    public PrepToExperimentSample() {
    }

    public ExperimentSample getExperimentSample() {
        return experimentSample;
    }

    public void setExperimentSample(ExperimentSample experimentSample) {
        this.experimentSample = experimentSample;
    }

    public ExperimentPreparedSample getPreparedSample() {
        return preparedSample;
    }

    public void setPreparedSample(ExperimentPreparedSample preparedSample) {
        this.preparedSample = preparedSample;
    }

    public ExperimentSampleType getType() {
        return type;
    }

    public void setType(ExperimentSampleType type) {
        this.type = type;
    }
}
