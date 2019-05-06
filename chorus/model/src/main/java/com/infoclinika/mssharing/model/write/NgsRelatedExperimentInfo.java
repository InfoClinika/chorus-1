package com.infoclinika.mssharing.model.write;

/**
 * @author : Alexander Serebriyan
 */
public class NgsRelatedExperimentInfo {

    public final int experimentType;
    public final boolean multiplexing;
    public final int samplesCount;
    public final int pairedEnd;
    public final Integer experimentPrepMethod;
    public final boolean xenograft;
    public final String libraryPrep;
    public final Long ntExtractionMethod;

    public NgsRelatedExperimentInfo(int experimentType, boolean multiplexing, int samplesCount, int pairedEnd,
                                    Integer experimentPrepMethod, boolean xenograft, String libraryPrep,
                                    Long ntExtractionMethod) {
        this.experimentType = experimentType;
        this.multiplexing = multiplexing;
        this.samplesCount = samplesCount;
        this.pairedEnd = pairedEnd;
        this.experimentPrepMethod = experimentPrepMethod;
        this.xenograft = xenograft;
        this.libraryPrep = libraryPrep;
        this.ntExtractionMethod = ntExtractionMethod;
    }

    public NgsRelatedExperimentInfo() {
        experimentType = -1;
        multiplexing = false;
        samplesCount = 0;
        pairedEnd = 0;
        this.experimentPrepMethod = null;
        this.xenograft = false;
        this.libraryPrep = "UNDEFINED";
        this.ntExtractionMethod = null;
    }

    @Override
    public String toString() {
        return "NgsRelatedExperimentInfo{" +
            "experimentType=" + experimentType +
            ", multiplexing=" + multiplexing +
            ", samplesCount=" + samplesCount +
            ", pairedEnd=" + pairedEnd +
            '}';
    }
}
