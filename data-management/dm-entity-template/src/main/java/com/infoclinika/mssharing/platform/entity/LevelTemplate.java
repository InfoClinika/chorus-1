package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static javax.persistence.CascadeType.*;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class LevelTemplate<FACTOR extends FactorTemplate<?, ?>> extends AbstractPersistable {
    private static final int LONG_STRING = 4000;
    @Column(name = "level_name", length = LONG_STRING)

    private String name;

    @ManyToOne
    @JoinColumn(name = "factor_id")
    private FACTOR factor;

    @ManyToMany(mappedBy = "levels", cascade = {REFRESH, DETACH, PERSIST, MERGE, REMOVE})
    private Set<Condition<?, ?, ?>> conditions = newHashSet();

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public FACTOR getFactor() {
        return factor;
    }

    public void setFactor(FACTOR factor) {
        this.factor = factor;
    }

    public Set<Condition<?, ?, ?>> getConditions() {
        return conditions;
    }

    public void setConditions(Set<Condition<?, ?, ?>> conditions) {
        this.conditions = conditions;
    }
}
