package com.infoclinika.mssharing.model.write;

import com.google.common.base.Objects;

import javax.persistence.Embeddable;

/**
 * @author Pavel Kaplin
 */
@Embeddable
public class AnalysisBounds {
    private Double minMz;
    private Double maxMz;
    private Double minRt;
    private Double maxRt;

    public AnalysisBounds(Double minMz, Double maxMz, Double minRt, Double maxRt) {
        this.minMz = minMz;
        this.maxMz = maxMz;
        this.minRt = minRt;
        this.maxRt = maxRt;
    }

    public AnalysisBounds() {
    }

    public Double getMinMz() {
        return minMz;
    }

    public void setMinMz(Double minMz) {
        this.minMz = minMz;
    }

    public Double getMaxMz() {
        return maxMz;
    }

    public void setMaxMz(Double maxMz) {
        this.maxMz = maxMz;
    }

    public Double getMinRt() {
        return minRt;
    }

    public void setMinRt(Double minRt) {
        this.minRt = minRt;
    }

    public Double getMaxRt() {
        return maxRt;
    }

    public void setMaxRt(Double maxRt) {
        this.maxRt = maxRt;
    }

    @Override
    public String toString() {
        return "AnalysisBounds{" +
                "minMz=" + minMz +
                ", maxMz=" + maxMz +
                ", minRt=" + minRt +
                ", maxRt=" + maxRt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisBounds that = (AnalysisBounds) o;
        return Objects.equal(minMz, that.minMz) &&
                Objects.equal(maxMz, that.maxMz) &&
                Objects.equal(minRt, that.minRt) &&
                Objects.equal(maxRt, that.maxRt);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(minMz, maxMz, minRt, maxRt);
    }
}
