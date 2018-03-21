package com.infoclinika.mssharing.dto;

public class DownloadDataCubeRequest {
    private long runId;
    private String reference;

    public DownloadDataCubeRequest(long runId, String reference) {
        this.runId = runId;
        this.reference = reference;
    }

    public DownloadDataCubeRequest() {
    }

    public long getRunId() {
        return runId;
    }

    public void setRunId(long runId) {
        this.runId = runId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
