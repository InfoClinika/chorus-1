package com.infoclinika.mssharing.web.controller.v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingRunsDTO{

    private String name;
    private Map<String, Collection<String>> fileToFileMap;
}
