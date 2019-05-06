package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.internal.entity.workflow.PrepToExperimentSample;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author andrii.loboda
 */
@Entity
public class ExperimentPreparedSample extends AbstractPersistable<Long> {
    @Basic(optional = false)
    private String name;

    @OneToMany(mappedBy = "preparedSample")
    private Set<PrepToExperimentSample> samples = newHashSet();

    public ExperimentPreparedSample(String name, Set<PrepToExperimentSample> samples) {
        this.name = name;
        this.samples = samples;
    }

    public ExperimentPreparedSample() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PrepToExperimentSample> getSamples() {
        return samples;
    }

    public void setSamples(Set<PrepToExperimentSample> samples) {
        this.samples = samples;
    }
}
