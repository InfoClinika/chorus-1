package com.infoclinika.mssharing.web.controller.v2.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingFileDTO {

    private Long id;
    private String name;
    private String filePath;
    private List<String> experimentFiles;
    private List<String> experimentSamples;


}
