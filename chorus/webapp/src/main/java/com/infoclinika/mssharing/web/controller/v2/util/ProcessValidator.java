package com.infoclinika.mssharing.web.controller.v2.util;


import java.util.Collection;
import java.util.Map;

public interface ProcessValidator {


    Map<String, Collection<String>> validateSampleFileMap(Map<String, Collection<String>> sampleFileMap, long experiment, long user,ValidationType validationType);

    Map<String, Collection<String>> validateAssociationFiles(Map<String, Collection<String>> fileToFileMap, long experimentId, long user, ValidationType type);

    boolean isProcessingRunExist(long processingRunId, long experiment);


}
