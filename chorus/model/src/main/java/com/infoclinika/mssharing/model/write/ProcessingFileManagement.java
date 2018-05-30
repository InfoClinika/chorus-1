package com.infoclinika.mssharing.model.write;


import java.util.Collection;
import java.util.Map;

public interface ProcessingFileManagement {

    long createProcessingFile(long experimentId, ProcessingFileShortInfo processingFileShortInfo);

    boolean isProcessingFileAlreadyUploadedToExperiment(long experiment, String fileName);

    boolean associateProcessingFileWithRawFile(Map<String, Collection<String>> map, long experimentId, long userId, String processingRunName);

    boolean isUserLabMembership(long user, long lab);

    Map<String, Collection<String>> validateAssociateFiles(Map<String, Collection<String>> map, long experimentId, long user);





    class ProcessingFileShortInfo {

        public final String name;
        public final String content;

        public ProcessingFileShortInfo(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }
}
