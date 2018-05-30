package com.infoclinika.mssharing.web.controller.v2.dto;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Optional;
import com.infoclinika.mssharing.web.controller.v2.service.ExperimentService;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

@Data
@JsonPropertyOrder(alphabetic = true)
public class ExperimentInfoDTO {

    private String name;
    private String labName;
    private Long projectId;
    private String projectName;
    private Long laboratory;
    private String vendor;
    private String technologyType;
    private String description;
    private String species;
    private String instrumentModel;
    private Long experimentType;
    private Map<String, Collection<FileToSamplesDTO>> filesToSamples;


    @Data
    public static class FileToSamplesDTO{
        private String filePath;
        private String sampleName;
        private String instrumentName;
    }
}



