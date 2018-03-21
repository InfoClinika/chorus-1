package com.infoclinika.mssharing.platform.model.write;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Optional;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface ExperimentManagementTemplate<EXPERIMENT_INFO extends ExperimentManagementTemplate.ExperimentInfoTemplate> {

    long createExperiment(long actor, EXPERIMENT_INFO experimentInfo);

    /**
     * Removes private experiment
     *
     * @param experiment - private experiment
     */
    void deleteExperiment(long actor, long experiment);

    void updateExperiment(long actor, long experiment, EXPERIMENT_INFO experimentInfo);

    class ExperimentInfoTemplate<META_FACTOR extends MetaFactorTemplate, FILE_ITEM extends FileItemTemplate> {
        public final Long lab;
        public final long project;
        public final String name;
        public final String description;
        public final List<META_FACTOR> factors;
        public final List<FILE_ITEM> files;
        public final long specie;
        public final boolean is2dLc;
        public final Restriction restriction;
        public final long experimentType;


        public ExperimentInfoTemplate(Long lab,
                                      String name,
                                      String description,
                                      long project,
                                      List<META_FACTOR> factors,
                                      List<FILE_ITEM> files,
                                      long specie,
                                      boolean is2dLc,
                                      Restriction restriction,
                                      long experimentType) {
            this.lab = lab;
            this.name = name;
            this.description = description;
            this.project = project;
            this.factors = factors;
            this.files = files;
            this.specie = specie;
            this.is2dLc = is2dLc;
            this.restriction = restriction;
            this.experimentType = experimentType;
        }
    }

    @Deprecated
    class Annotations {
        public final String fractionNumber;
        public final String sampleId;

        public Annotations(String fractionNumber, String sampleId) {
            this.fractionNumber = fractionNumber;
            this.sampleId = sampleId;
        }
    }

    class MetaFactorTemplate {
        public final String name;
        public final String units;
        public final boolean isNumeric;
        public final long experimentId;

        public MetaFactorTemplate(String name, String units, boolean numeric, long experimentId) {
            this.name = name;
            this.units = units;
            isNumeric = numeric;
            this.experimentId = experimentId;
        }
    }

    class AnnotationTemplate {
        public final String name;
        public final String value;
        public final String units;
        public final boolean isNumeric;

        public AnnotationTemplate(String name, String value, String units, boolean isNumeric) {
            this.name = name;
            this.value = value;
            this.units = units;
            this.isNumeric = isNumeric;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AnnotationTemplate that = (AnnotationTemplate) o;

            if (isNumeric != that.isNumeric) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (value != null ? !value.equals(that.value) : that.value != null) return false;
            return !(units != null ? !units.equals(that.units) : that.units != null);

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (units != null ? units.hashCode() : 0);
            result = 31 * result + (isNumeric ? 1 : 0);
            return result;
        }
    }

    /**
     * @author Herman Zamula
     */
    class FileItemTemplate {
        public final long id;
        public final List<String> factorValues;
        public final List<AnnotationTemplate> annotationValues;
        public final boolean copy;


        public FileItemTemplate(long id, List<String> factorValues, List<AnnotationTemplate> annotations, boolean copy) {
            this.id = id;
            this.factorValues = factorValues;
            this.annotationValues = annotations;
            this.copy = copy;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Restriction {

        public final Long technologyType;
        public final Long vendor;
        public final Long instrumentType;
        public final Long instrumentModel;
        public final Optional<Long> instrument;

        public Restriction() {
            this.instrumentModel = null;
            this.instrument = Optional.absent();
            this.technologyType = null;
            this.vendor = null;
            this.instrumentType = null;
        }

        public Restriction(Long instrumentModel, Optional<Long> instrument) {
            this.instrumentModel = instrumentModel;
            this.instrument = instrument;
            this.technologyType = null;
            this.vendor = null;
            this.instrumentType = null;
        }

        public Restriction(long technologyType, long vendor, long instrumentType, long instrumentModel, Optional<Long> instrument) {
            this.technologyType = technologyType;
            this.vendor = vendor;
            this.instrumentModel = instrumentModel;
            this.instrument = instrument;
            this.instrumentType = instrumentType;
        }


    }
}
