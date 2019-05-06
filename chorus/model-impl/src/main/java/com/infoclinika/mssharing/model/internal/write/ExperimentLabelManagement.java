package com.infoclinika.mssharing.model.internal.write;

import org.springframework.transaction.annotation.Transactional;

/**
 * Should not be used inside production code.
 *
 * @author andrii.loboda
 */
@Transactional
public interface ExperimentLabelManagement {

    long createLabelType(ExperimentTypeInfo typeToCreate);

    long createLabel(ExperimentLabelInfo labelToCreate);

    class ExperimentLabelInfo {
        public final String aminoAcid;
        public final String name;
        public final long type;

        public ExperimentLabelInfo(String aminoAcid, String name, long type) {
            this.aminoAcid = aminoAcid;
            this.name = name;
            this.type = type;
        }
    }

    class ExperimentTypeInfo {
        public final String name;
        public final int maxSamples;

        public ExperimentTypeInfo(String name, int maxSamples) {
            this.name = name;
            this.maxSamples = maxSamples;
        }
    }
}
