package com.infoclinika.mssharing.model.internal.entity;


import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author andrii.loboda
 */
@Entity
public class ExperimentSample extends AbstractPersistable<Long> {
    @Basic(optional = false)
    private String name;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> factorValues = newArrayList();

    @ManyToMany(mappedBy = "samples", cascade = {CascadeType.REFRESH, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Set<SampleCondition> sampleConditions = newHashSet();

    public ExperimentSample(String name, List<String> factorValues) {
        this.name = name;
        this.factorValues = factorValues;
    }

    public ExperimentSample() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getFactorValues() {
        return factorValues;
    }

    public void setFactorValues(List<String> factorValues) {
        this.factorValues = factorValues;
    }

    public Set<SampleCondition> getSampleConditions() {
        return sampleConditions;
    }

    public void setSampleConditions(Set<SampleCondition> sampleConditions) {
        this.sampleConditions = sampleConditions;
    }
}
