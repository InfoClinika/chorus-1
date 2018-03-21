package com.infoclinika.mssharing.dto;

import java.util.Date;

public class DataCubeDetailsDto {
    private Long runId;
    private String name;
    private Date dateCreated;
    private String type;
    private String pipeline;
    private String dataView;
    private String composedType;
    private String reference;

    public DataCubeDetailsDto(Long runId, String name, Date dateCreated, String type, String pipeline, String dataView, String composedType, String reference) {
        this.runId = runId;
        this.name = name;
        this.dateCreated = dateCreated;
        this.type = type;
        this.pipeline = pipeline;
        this.dataView = dataView;
        this.composedType = composedType;
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public String getDataView() {
        return dataView;
    }

    public void setDataView(String dataView) {
        this.dataView = dataView;
    }

    public String getComposedType() {
        return composedType;
    }

    public void setComposedType(String composedType) {
        this.composedType = composedType;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public String getReference() {
        return reference;
    }
}
