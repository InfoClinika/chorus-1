package com.infoclinika.mssharing.model.write.ngs.dto;

/**
 * @author timofei.kasianov 8/3/18
 */
public class NgsExperimentImportRequest {

    private final String experimentName;
    private final long projectId;
    private final long labId;
    private final long instrumentId;
    private final byte[] experimentTemplateFileInBytes;

    public NgsExperimentImportRequest(String experimentName, long projectId, long labId, long instrumentId,
                                      byte[] experimentTemplateFileInBytes) {
        this.experimentName = experimentName;
        this.projectId = projectId;
        this.labId = labId;
        this.instrumentId = instrumentId;
        this.experimentTemplateFileInBytes = experimentTemplateFileInBytes;
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

    public byte[] getExperimentTemplateFileInBytes() {
        return experimentTemplateFileInBytes;
    }
}
