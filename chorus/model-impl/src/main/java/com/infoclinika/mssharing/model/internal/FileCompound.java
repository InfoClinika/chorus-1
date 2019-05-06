package com.infoclinika.mssharing.model.internal;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author vladimir.moiseiev.
 */

@Entity
@Table(name = "file_compound")
public class FileCompound {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "compound_id")
    private String compoundId;

    @Column(name = "formula")
    private String formula;

    @Column(name = "weight")
    private double weight;

    public FileCompound() {
    }

    public FileCompound(String compoundId, String formula, double weight) {
        this.compoundId = compoundId;
        this.formula = formula;
        this.weight = weight;
    }

    public String getCompoundId() {
        return compoundId;
    }

    public void setCompoundId(String compoundId) {
        this.compoundId = compoundId;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "FileCompound{" +
            "id=" + id +
            ", compoundId='" + compoundId + '\'' +
            ", formula='" + formula + '\'' +
            ", weight=" + weight +
            '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileCompound)) {
            return false;
        }
        FileCompound that = (FileCompound) o;
        return Double.compare(that.weight, weight) == 0 &&
            Objects.equals(id, that.id) &&
            Objects.equals(compoundId, that.compoundId) &&
            Objects.equals(formula, that.formula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, compoundId, formula, weight);
    }
}
