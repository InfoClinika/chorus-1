package com.infoclinika.mssharing.web.controller.v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingRunsDTO{

    private long id;
    private String name;
    private Date processedDate;
    private Map<String, Collection<String>> fileToFileMap;
    private Map<String, Collection<String>>  sampleFileMap;


    @Data
    public static class ProcessingRunsShortDetails{
        private long id;
        private String name;
        private String processedDate;
    }


}
