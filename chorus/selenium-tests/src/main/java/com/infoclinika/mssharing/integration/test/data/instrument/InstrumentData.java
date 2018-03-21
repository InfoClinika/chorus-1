package com.infoclinika.mssharing.integration.test.data.instrument;

/**
 * @author Sergii Moroz
 */
public class InstrumentData {

    private final String name;
    private final String vendor;
    private final String model;
    private final String laboratory;
    private final String serialNumber;
    private OptionalFields optionalFields;

    private InstrumentData(Builder builder) {
        this.name = builder.name;
        this.vendor = builder.vendor;
        this.model = builder.model;
        this.laboratory = builder.laboratory;
        this.serialNumber = builder.serialNumber;
        this.optionalFields = builder.optionalFields;
    }

    public String getName() {
        return name;
    }

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    public String getLaboratory() {
        return laboratory;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public OptionalFields getOptionalFields() {
        return optionalFields;
    }

    public static class Builder {
        private String name;
        private String vendor;
        private String model;
        private String laboratory;
        private String serialNumber;
        private OptionalFields optionalFields;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder laboratory(String laboratory) {
            this.laboratory = laboratory;
            return this;
        }

        public Builder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public Builder optionalFields(OptionalFields optionalFields) {
            this.optionalFields = optionalFields;
            return this;
        }

        public InstrumentData build() {
            return new InstrumentData(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstrumentData that = (InstrumentData) o;

        if (laboratory != null ? !laboratory.equals(that.laboratory) : that.laboratory != null) return false;
        if (model != null ? !model.equals(that.model) : that.model != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (optionalFields != null ? !optionalFields.equals(that.optionalFields) : that.optionalFields != null)
            return false;
        if (serialNumber != null ? !serialNumber.equals(that.serialNumber) : that.serialNumber != null) return false;
        if (vendor != null ? !vendor.equals(that.vendor) : that.vendor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (vendor != null ? vendor.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (laboratory != null ? laboratory.hashCode() : 0);
        result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
        result = 31 * result + (optionalFields != null ? optionalFields.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InstrumentData{" +
                "name='" + name + '\'' +
                ", vendor='" + vendor + '\'' +
                ", model='" + model + '\'' +
                ", laboratory='" + laboratory + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", optionalFields=" + optionalFields +
                '}';
    }
}
