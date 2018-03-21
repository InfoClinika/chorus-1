package com.infoclinika.mssharing.integration.test.data.experiment;

/**
 * @author Alexander Orlov
 */
public class GeneralInfo {

    private final String name;
    private final String project;
    private final String species;
    private final String laboratory;
    private final String instrumentModel;
    private final String instrument;
    private final String description;

    private GeneralInfo(Builder builder) {
        this.name = builder.name;
        this.project = builder.project;
        this.species = builder.species;
        this.laboratory = builder.laboratory;
        this.instrumentModel = builder.instrumentModel;
        this.instrument = builder.instrument;
        this.description = builder.description;
    }

    public String getName() {
        return name;
    }

    public String getProject() {
        return project;
    }

    public String getSpecies() {
        return species;
    }

    public String getLaboratory() {
        return laboratory;
    }

    public String getInstrumentModel() {
        return instrumentModel;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getDescription() {
        return description;
    }

    public static class Builder {
        private String name;
        private String project;
        private String species;
        private String laboratory;
        private String instrumentModel;
        private String instrument;
        private String description;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder project(String project) {
            this.project = project;
            return this;
        }

        public Builder species(String species) {
            this.species = species;
            return this;
        }

        public Builder laboratory(String laboratory) {
            this.laboratory = laboratory;
            return this;
        }

        public Builder instrumentModel(String instrumentModel) {
            this.instrumentModel = instrumentModel;
            return this;
        }

        public Builder instrument(String instrument) {
            this.instrument = instrument;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public GeneralInfo build() {
            return new GeneralInfo(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralInfo that = (GeneralInfo) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (instrument != null ? !instrument.equals(that.instrument) : that.instrument != null) return false;
        if (instrumentModel != null ? !instrumentModel.equals(that.instrumentModel) : that.instrumentModel != null)
            return false;
        if (laboratory != null ? !laboratory.equals(that.laboratory) : that.laboratory != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (species != null ? !species.equals(that.species) : that.species != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (species != null ? species.hashCode() : 0);
        result = 31 * result + (laboratory != null ? laboratory.hashCode() : 0);
        result = 31 * result + (instrumentModel != null ? instrumentModel.hashCode() : 0);
        result = 31 * result + (instrument != null ? instrument.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GeneralInfo{" +
                "name='" + name + '\'' +
                ", project='" + project + '\'' +
                ", species='" + species + '\'' +
                ", laboratory='" + laboratory + '\'' +
                ", instrumentModel='" + instrumentModel + '\'' +
                ", instrument='" + instrument + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
