package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "adduct")
public class Adduct extends AbstractPersistable<Long> {

    @Column(name = "formula")
    private String formula;

    @Column(name = "mass")
    private double mass;

    @Column(name = "sort_order")
    private long sortOrder;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "is_positive")
    private boolean isPositive;

    @Column(name = "adduct_type")
    @Enumerated(EnumType.STRING)
    private AdductType adductType;

    public Adduct() {
    }

    public Adduct(
        String formula,
        double mass,
        long sortOrder,
        boolean enabled,
        boolean isPositive,
        AdductType adductType
    ) {
        this.formula = formula;
        this.mass = mass;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.isPositive = isPositive;
        this.adductType = adductType;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }

    public long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(long sortOrder) {
        this.sortOrder = sortOrder;
    }

    public AdductType getType() {
        return adductType;
    }

    public void setType(AdductType adductType) {
        this.adductType = adductType;
    }

    public enum AdductType {
        PEPTIDE,
        COMPOUND
    }
}
