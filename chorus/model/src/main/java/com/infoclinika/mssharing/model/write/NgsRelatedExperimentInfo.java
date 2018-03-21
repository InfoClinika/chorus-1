package com.infoclinika.mssharing.model.write;

/**
 * @author : Alexander Serebriyan
 */
public class NgsRelatedExperimentInfo {

    public final String experimentType;
    public final boolean multiplexing;
    public final int samplesCount;

    public NgsRelatedExperimentInfo(String experimentType, boolean multiplexing, int samplesCount) {
        this.experimentType = experimentType;
        this.multiplexing = multiplexing;
        this.samplesCount = samplesCount;
    }

    public NgsRelatedExperimentInfo() {
        experimentType = "";
        multiplexing = false;
        samplesCount = 0;
    }

    @Override
    public String toString() {
        return "NgsRelatedExperimentInfo{" +
                "experimentType=" + experimentType +
                ", multiplexing=" + multiplexing +
                ", samplesCount=" + samplesCount +
                '}';
    }
}
