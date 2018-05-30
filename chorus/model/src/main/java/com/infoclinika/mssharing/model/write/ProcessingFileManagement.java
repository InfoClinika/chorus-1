package com.infoclinika.mssharing.model.write;


import java.util.Collection;
import java.util.Map;

public interface ProcessingFileManagement {

    long createProcessingFile(long experimentId, ProcessingFileShortInfo processingFileShortInfo);

    boolean isProcessingFileAlreadyUploadedToExperiment(long experiment, String fileName);

    boolean associateProcessingFileWithRawFile(Map<String, Collection<String>> fileToFileMap,Map<String, Collection<String>> sampleFileMap, long experimentId, long userId, String processingRunName);



    class ProcessingFileShortInfo {

        public final String name;
        public final String content;

        public ProcessingFileShortInfo(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }
}
