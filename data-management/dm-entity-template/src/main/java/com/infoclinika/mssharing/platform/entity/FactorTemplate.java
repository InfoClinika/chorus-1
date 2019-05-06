package com.infoclinika.mssharing.platform.entity;

import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class FactorTemplate<LEVEL extends LevelTemplate<?>,
    EXPERIMENT extends ExperimentTemplate<?, ?, ?, ?, ?, ?>> extends AbstractPersistable {
    private static final int LONG_STRING = 4000;

    @Column(name = "factor_name", length = LONG_STRING)
    private String name;
    @Column(name = "factor_type")
    private Type type;
    @Column(name = "unit_name", length = LONG_STRING)
    private String units;
    @Column(name = "default_factor")
    private boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "experiment_id")
    private EXPERIMENT experiment;

    @OneToMany(mappedBy = "factor", orphanRemoval = true, cascade = {CascadeType.ALL},
        targetEntity = LevelTemplate.class)
    private Set<LEVEL> levels = newHashSet();

    public FactorTemplate() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EXPERIMENT getExperiment() {
        return experiment;
    }

    public void setExperiment(EXPERIMENT experiment) {
        this.experiment = experiment;
    }

    public Set<LEVEL> getLevels() {
        return levels;
    }

    public void setLevels(Set<LEVEL> levels) {
        this.levels = levels;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public enum Type {
        STRING, INTEGER
    }
}
