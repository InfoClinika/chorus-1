package com.infoclinika.mssharing.web.controller.v2.util;

import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunsDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProcessFileValidator {

//    Map<String, Collection<String>> validateAssociateExperimentFiles(Map<String, Collection<String>> map, long experimentId, long user);
//
//    Map<String, Collection<String>> checkValidProcessingFiles(ProcessingRunsDTO dto, long experiment, Map<String, Collection<String>> resultsMap);

    Map<String, Collection<String>> validateSampleFileMap(Map<String, Collection<String>> sampleFileMap, long experiment, long user,ValidationType validationType);

//    Map<String, Collection<String>> checkExistsSampleInExperiment(Map<String, Collection<String>> sampleFileMap, long experiment, long user);

    Map<String, Collection<String>> validateAssociationFiles(Map<String, Collection<String>> fileToFileMap, long experimentId, long user, ValidationType type);


}
