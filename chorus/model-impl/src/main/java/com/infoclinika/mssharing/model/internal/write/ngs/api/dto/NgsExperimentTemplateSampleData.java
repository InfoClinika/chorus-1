package com.infoclinika.mssharing.model.internal.write.ngs.api.dto;

import java.util.Map;

/**
 * @author timofei.kasianov 8/3/18
 */
public class NgsExperimentTemplateSampleData {

    private final String vendor;
    private final String vendorId;
    private final String vendorProjectName;
    private final String celgeneId;
    private final String daProjectId;
    private final String celgeneProjectDescription;
    private final String experiment;
    private final String displayName;
    private final String displayNameShort;
    private final String cellType;
    private final String cellLine;
    private final String tissue;
    // condition index to value
    private final Map<Integer, String> conditions;
    private final Boolean xenograft;
    private final String timeTreatment;
    // response description index to value
    private final Map<Integer, String> responseDescriptions;
    // response index to value
    private final Map<Integer, String> responses;
    // compound index to value
    private final Map<Integer, String> compounds;
    // dose index to value
    private final Map<Integer, String> doses;
    private final Long biologicalReplicatesGroup;
    private final Long technicalReplicatesGroup;
    private final String experimentType;
    private final String technology;
    private final String libraryPrep;
    private final String exomeBaitSet;
    private final String rnaSelection;
    private final String ntExtraction;
    private final String antibodyTarget;
    private final String referenceGenome;
    private final String hostGenome;
    private final String stranded;
    private final Boolean pairedEnd;
    private final String filename;

    public NgsExperimentTemplateSampleData(
        String vendor, String vendorId, String vendorProjectName, String celgeneId,
        String daProjectId, String celgeneProjectDescription, String experiment,
        String displayName, String displayNameShort, String cellType, String cellLine,
        String tissue, Map<Integer, String> conditions, Boolean xenograft,
        String timeTreatment, Map<Integer, String> responseDescriptions,
        Map<Integer, String> responses, Map<Integer, String> compounds,
        Map<Integer, String> doses, Long biologicalReplicatesGroup,
        Long technicalReplicatesGroup, String experimentType, String technology,
        String libraryPrep, String exomeBaitSet, String rnaSelection,
        String ntExtraction, String antibodyTarget, String referenceGenome,
        String hostGenome, String stranded, Boolean pairedEnd, String filename
    ) {
        this.vendor = vendor;
        this.vendorId = vendorId;
        this.vendorProjectName = vendorProjectName;
        this.celgeneId = celgeneId;
        this.daProjectId = daProjectId;
        this.celgeneProjectDescription = celgeneProjectDescription;
        this.experiment = experiment;
        this.displayName = displayName;
        this.displayNameShort = displayNameShort;
        this.cellType = cellType;
        this.cellLine = cellLine;
        this.tissue = tissue;
        this.conditions = conditions;
        this.xenograft = xenograft;
        this.timeTreatment = timeTreatment;
        this.responseDescriptions = responseDescriptions;
        this.responses = responses;
        this.compounds = compounds;
        this.doses = doses;
        this.biologicalReplicatesGroup = biologicalReplicatesGroup;
        this.technicalReplicatesGroup = technicalReplicatesGroup;
        this.experimentType = experimentType;
        this.technology = technology;
        this.libraryPrep = libraryPrep;
        this.exomeBaitSet = exomeBaitSet;
        this.rnaSelection = rnaSelection;
        this.ntExtraction = ntExtraction;
        this.antibodyTarget = antibodyTarget;
        this.referenceGenome = referenceGenome;
        this.hostGenome = hostGenome;
        this.stranded = stranded;
        this.pairedEnd = pairedEnd;
        this.filename = filename;
    }

    public String getVendor() {
        return vendor;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getVendorProjectName() {
        return vendorProjectName;
    }

    public String getCelgeneId() {
        return celgeneId;
    }

    public String getDaProjectId() {
        return daProjectId;
    }

    public String getCelgeneProjectDescription() {
        return celgeneProjectDescription;
    }

    public String getExperiment() {
        return experiment;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNameShort() {
        return displayNameShort;
    }

    public String getCellType() {
        return cellType;
    }

    public String getCellLine() {
        return cellLine;
    }

    public String getTissue() {
        return tissue;
    }

    public Map<Integer, String> getConditions() {
        return conditions;
    }

    public Boolean getXenograft() {
        return xenograft;
    }

    public String getTimeTreatment() {
        return timeTreatment;
    }

    public Map<Integer, String> getResponseDescriptions() {
        return responseDescriptions;
    }

    public Map<Integer, String> getResponses() {
        return responses;
    }

    public Map<Integer, String> getCompounds() {
        return compounds;
    }

    public Map<Integer, String> getDoses() {
        return doses;
    }

    public Long getBiologicalReplicatesGroup() {
        return biologicalReplicatesGroup;
    }

    public Long getTechnicalReplicatesGroup() {
        return technicalReplicatesGroup;
    }

    public String getExperimentType() {
        return experimentType;
    }

    public String getTechnology() {
        return technology;
    }

    public String getLibraryPrep() {
        return libraryPrep;
    }

    public String getExomeBaitSet() {
        return exomeBaitSet;
    }

    public String getRnaSelection() {
        return rnaSelection;
    }

    public String getNtExtraction() {
        return ntExtraction;
    }

    public String getAntibodyTarget() {
        return antibodyTarget;
    }

    public String getReferenceGenome() {
        return referenceGenome;
    }

    public String getHostGenome() {
        return hostGenome;
    }

    public String getStranded() {
        return stranded;
    }

    public Boolean getPairedEnd() {
        return pairedEnd;
    }

    public String getFilename() {
        return filename;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String vendor;
        private String vendorId;
        private String vendorProjectName;
        private String celgeneId;
        private String daProjectId;
        private String celgeneProjectDescription;
        private String experiment;
        private String displayName;
        private String displayNameShort;
        private String cellType;
        private String cellLine;
        private String tissue;
        private Map<Integer, String> conditions;
        private Boolean xenograft;
        private String timeTreatment;
        private Map<Integer, String> responseDescriptions;
        private Map<Integer, String> responses;
        private Map<Integer, String> compounds;
        private Map<Integer, String> doses;
        private Long biologicalReplicatesGroup;
        private Long technicalReplicatesGroup;
        private String experimentType;
        private String technology;
        private String libraryPrep;
        private String exomeBaitSet;
        private String rnaSelection;
        private String ntExtraction;
        private String antibodyTarget;
        private String referenceGenome;
        private String hostGenome;
        private String stranded;
        private Boolean pairedEnd;
        private String filename;

        public NgsExperimentTemplateSampleData build() {
            return new NgsExperimentTemplateSampleData(
                vendor, vendorId, vendorProjectName, celgeneId, daProjectId, celgeneProjectDescription, experiment,
                displayName, displayNameShort, cellType, cellLine, tissue, conditions, xenograft, timeTreatment,
                responseDescriptions, responses, compounds, doses, biologicalReplicatesGroup,
                technicalReplicatesGroup, experimentType, technology, libraryPrep, exomeBaitSet, rnaSelection,
                ntExtraction, antibodyTarget, referenceGenome, hostGenome, stranded, pairedEnd, filename
            );
        }

        public Builder setVendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder setVendorId(String vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        public Builder setVendorProjectName(String vendorProjectName) {
            this.vendorProjectName = vendorProjectName;
            return this;
        }

        public Builder setCelgeneId(String celgeneId) {
            this.celgeneId = celgeneId;
            return this;
        }

        public Builder setDaProjectId(String daProjectId) {
            this.daProjectId = daProjectId;
            return this;
        }

        public Builder setCelgeneProjectDescription(String celgeneProjectDescription) {
            this.celgeneProjectDescription = celgeneProjectDescription;
            return this;
        }

        public Builder setExperiment(String experiment) {
            this.experiment = experiment;
            return this;
        }

        public Builder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setDisplayNameShort(String displayNameShort) {
            this.displayNameShort = displayNameShort;
            return this;
        }

        public Builder setCellType(String cellType) {
            this.cellType = cellType;
            return this;
        }

        public Builder setCellLine(String cellLine) {
            this.cellLine = cellLine;
            return this;
        }

        public Builder setTissue(String tissue) {
            this.tissue = tissue;
            return this;
        }

        public Builder setConditions(Map<Integer, String> conditions) {
            this.conditions = conditions;
            return this;
        }

        public Builder setXenograft(Boolean xenograft) {
            this.xenograft = xenograft;
            return this;
        }

        public Builder setTimeTreatment(String timeTreatment) {
            this.timeTreatment = timeTreatment;
            return this;
        }

        public Builder setResponseDescriptions(Map<Integer, String> responseDescriptions) {
            this.responseDescriptions = responseDescriptions;
            return this;
        }

        public Builder setResponses(Map<Integer, String> responses) {
            this.responses = responses;
            return this;
        }

        public Builder setCompounds(Map<Integer, String> compounds) {
            this.compounds = compounds;
            return this;
        }

        public Builder setDoses(Map<Integer, String> doses) {
            this.doses = doses;
            return this;
        }

        public Builder setBiologicalReplicatesGroup(Long biologicalReplicatesGroup) {
            this.biologicalReplicatesGroup = biologicalReplicatesGroup;
            return this;
        }

        public Builder setTechnicalReplicatesGroup(Long technicalReplicatesGroup) {
            this.technicalReplicatesGroup = technicalReplicatesGroup;
            return this;
        }

        public Builder setExperimentType(String experimentType) {
            this.experimentType = experimentType;
            return this;
        }

        public Builder setTechnology(String technology) {
            this.technology = technology;
            return this;
        }

        public Builder setLibraryPrep(String libraryPrep) {
            this.libraryPrep = libraryPrep;
            return this;
        }

        public Builder setExomeBaitSet(String exomeBaitSet) {
            this.exomeBaitSet = exomeBaitSet;
            return this;
        }

        public Builder setRnaSelection(String rnaSelection) {
            this.rnaSelection = rnaSelection;
            return this;
        }

        public Builder setNtExtraction(String ntExtraction) {
            this.ntExtraction = ntExtraction;
            return this;
        }

        public Builder setAntibodyTarget(String antibodyTarget) {
            this.antibodyTarget = antibodyTarget;
            return this;
        }

        public Builder setReferenceGenome(String referenceGenome) {
            this.referenceGenome = referenceGenome;
            return this;
        }

        public Builder setHostGenome(String hostGenome) {
            this.hostGenome = hostGenome;
            return this;
        }

        public Builder setStranded(String stranded) {
            this.stranded = stranded;
            return this;
        }

        public Builder setPairedEnd(Boolean pairedEnd) {
            this.pairedEnd = pairedEnd;
            return this;
        }

        public Builder setFilename(String filename) {
            this.filename = filename;
            return this;
        }
    }
}
