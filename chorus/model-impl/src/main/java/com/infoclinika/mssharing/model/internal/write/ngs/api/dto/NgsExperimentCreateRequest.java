package com.infoclinika.mssharing.model.internal.write.ngs.api.dto;

/**
 * @author timofei.kasianov 8/6/18
 */
public class NgsExperimentCreateRequest {

    private final String experimentName;
    private final long projectId;
    private final long labId;
    private final long instrumentId;
    private final NgsExperimentTemplateData data;

    public NgsExperimentCreateRequest(String experimentName, long projectId, long labId, long instrumentId,
                                      NgsExperimentTemplateData data) {
        this.experimentName = experimentName;
        this.projectId = projectId;
        this.labId = labId;
        this.instrumentId = instrumentId;
        this.data = data;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public long getProjectId() {
        return projectId;
    }

    public long getLabId() {
        return labId;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public NgsExperimentTemplateData getData() {
        return data;
    }
}
