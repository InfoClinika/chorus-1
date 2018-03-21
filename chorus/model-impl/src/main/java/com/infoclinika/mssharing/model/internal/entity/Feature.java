package com.infoclinika.mssharing.model.internal.entity;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Pavel Kaplin
 */
@Table(name = "feature")
@Entity
public class Feature {

    @Id
    private String name;

    @Column(name = "enabled_state")
    @Enumerated(EnumType.ORDINAL)
    private FeatureState enabledState;

    @ManyToMany
    @JoinTable(name = "feature_lab",
            joinColumns = @JoinColumn(name = "feature_id"),
            inverseJoinColumns = @JoinColumn(name = "lab_id"))
    private Set<Lab> enabledLabs = newHashSet();

    public Feature() {
    }

    public Feature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FeatureState getEnabledState() {
        return enabledState;
    }

    public void setEnabledState(FeatureState enabled) {
        this.enabledState = enabled;
    }

    public Set<Lab> getEnabledLabs() {
        return enabledLabs;
    }

    public enum FeatureState {
        DISABLED, ENABLED, ENABLED_PER_LAB
    }
}
