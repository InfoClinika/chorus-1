package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.platform.entity.LevelTemplate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author andrii.loboda
 */
@Entity
public class Level extends LevelTemplate<Factor> {

    public Level() {
    }

    public Level(String name, Factor factor) {
        setName(name);
        setFactor(factor);
    }
    @ManyToMany(mappedBy = "levels", cascade = {CascadeType.REFRESH, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Set<SampleCondition> sampleConditions = newHashSet();

    public Set<SampleCondition> getSampleConditions() {
        return sampleConditions;
    }

    public void setSampleConditions(Set<SampleCondition> sampleConditions) {
        this.sampleConditions = sampleConditions;
    }
}

