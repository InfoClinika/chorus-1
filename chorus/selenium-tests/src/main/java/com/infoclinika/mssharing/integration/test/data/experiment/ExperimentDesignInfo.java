package com.infoclinika.mssharing.integration.test.data.experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Orlov
 */
public class ExperimentDesignInfo {

    private final String factorName;
    private final FactorType valueType;
    private final String units;
    private List<String> factors = new ArrayList<>();
    private List<String> fractionNumber = new ArrayList<>();
    private List<String> sampleId = new ArrayList<>();

    private ExperimentDesignInfo(Builder builder) {
        this.factorName = builder.factorName;
        this.valueType = builder.valueType;
        this.units = builder.units;
        this.factors = builder.factors;
        this.fractionNumber = builder.fractionNumber;
        this.sampleId = builder.sampleId;
    }

    public String getFactorName() {
        return factorName;
    }

    public FactorType getValueType() {
        return valueType;
    }

    public String getUnits() {
        return units;
    }

    public List<String> getFactors() {
        return factors;
    }

    public List<String> getFractionNumber() {
        return fractionNumber;
    }

    public List<String> getSampleId() {
        return sampleId;
    }

    public static class Builder {
        private String factorName;
        private FactorType valueType;
        private String units;
        private List<String> factors = new ArrayList<>();
        private List<String> fractionNumber = new ArrayList<>();
        private List<String> sampleId = new ArrayList<>();


        public Builder factorName(String factorName) {
            this.factorName = factorName;
            return this;
        }

        public Builder valueType(FactorType valueType) {
            this.valueType = valueType;
            return this;
        }

        public Builder units(String units) {
            this.units = units;
            return this;
        }

        public Builder factors(List<String> factors) {
            this.factors = factors;
            return this;
        }

        public Builder fractionNumber(List<String> fractionNumber) {
            this.fractionNumber = fractionNumber;
            return this;
        }

        public Builder sampleId(List<String> sampleId) {
            this.sampleId = sampleId;
            return this;
        }

        public ExperimentDesignInfo build() {
            return new ExperimentDesignInfo(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentDesignInfo that = (ExperimentDesignInfo) o;

        if (factorName != null ? !factorName.equals(that.factorName) : that.factorName != null) return false;
        if (factors != null ? !factors.equals(that.factors) : that.factors != null) return false;
        if (fractionNumber != null ? !fractionNumber.equals(that.fractionNumber) : that.fractionNumber != null)
            return false;
        if (sampleId != null ? !sampleId.equals(that.sampleId) : that.sampleId != null) return false;
        if (units != null ? !units.equals(that.units) : that.units != null) return false;
        if (valueType != that.valueType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = factorName != null ? factorName.hashCode() : 0;
        result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
        result = 31 * result + (units != null ? units.hashCode() : 0);
        result = 31 * result + (factors != null ? factors.hashCode() : 0);
        result = 31 * result + (fractionNumber != null ? fractionNumber.hashCode() : 0);
        result = 31 * result + (sampleId != null ? sampleId.hashCode() : 0);
        return result;
    }


}
