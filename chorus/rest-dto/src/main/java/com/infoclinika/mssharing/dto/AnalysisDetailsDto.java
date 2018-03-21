package com.infoclinika.mssharing.dto;

public class AnalysisDetailsDto {
    private String name;
    private Long runId;
    private String dataCreation;
    private String dataViewLayer;
    private String composedType;

    public AnalysisDetailsDto(
            String name,
            Long runId,
            String dataCreation,
            String dataViewLayer,
            String composedType
    ) {
        this.name = name;
        this.runId = runId;
        this.dataCreation = dataCreation;
        this.dataViewLayer = dataViewLayer;
        this.composedType = composedType;
    }

    public AnalysisDetailsDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public String getDataCreation() {
        return dataCreation;
    }

    public void setDataCreation(String dataCreation) {
        this.dataCreation = dataCreation;
    }

    public String getDataViewLayer() {
        return dataViewLayer;
    }

    public void setDataViewLayer(String dataViewLayer) {
        this.dataViewLayer = dataViewLayer;
    }

    public String getComposedType() {
        return composedType;
    }

    public void setComposedType(String composedType) {
        this.composedType = composedType;
    }
}
