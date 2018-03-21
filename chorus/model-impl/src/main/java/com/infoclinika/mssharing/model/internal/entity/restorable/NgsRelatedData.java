package com.infoclinika.mssharing.model.internal.entity.restorable;

import javax.persistence.*;

/**
 * @author : Alexander Serebriyan
 */
@Embeddable
public class NgsRelatedData {

    @Basic(optional = true)
    private Boolean multiplexing;

    @Basic(optional = true)
    @Enumerated(value = EnumType.STRING)
    private NgsExperimentType ngsExperimentType;

    public NgsRelatedData() {
    }

    public NgsRelatedData(Boolean multiplexing, NgsExperimentType ngsExperimentType) {
        this.multiplexing = multiplexing;
        this.ngsExperimentType = ngsExperimentType;
    }

    public Boolean isMultiplexing() {
        return multiplexing;
    }

    public void setMultiplexing(Boolean multiplexing) {
        this.multiplexing = multiplexing;
    }

    public NgsExperimentType getNgsExperimentType() {
        return ngsExperimentType;
    }

    public void setNgsExperimentType(NgsExperimentType ngsExperimentType) {
        this.ngsExperimentType = ngsExperimentType;
    }

    @Transient
    public String getNgsExperimentTypeName() {
        if (ngsExperimentType != null) {
            return ngsExperimentType.name();
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NgsRelatedData)) {
            return false;
        }

        NgsRelatedData that = (NgsRelatedData) o;

        if (multiplexing != null ? !multiplexing.equals(that.multiplexing) : that.multiplexing != null) {
            return false;
        }
        return ngsExperimentType == that.ngsExperimentType;
    }

    @Override
    public int hashCode() {
        int result = multiplexing != null ? multiplexing.hashCode() : 0;
        result = 31 * result + (ngsExperimentType != null ? ngsExperimentType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NgsRelatedData{" +
                "multiplexing=" + multiplexing +
                ", ngsExperimentType=" + ngsExperimentType +
                '}';
    }
}
