package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import com.infoclinika.mssharing.model.internal.entity.ProcessingRun;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;


import java.util.List;

public interface ProcessingFileReader {

    ProcessingFileInfo readProcessingFileInfo(long processingFile);

    List<ProcessingFile> readProcessingFilesByExperiment(long experiment);


    class ProcessingFileInfo{
        public String name;
        public String content;
        public List<FileMetaDataTemplate> fileMetaDataTemplateList;
        public List<ProcessingRun> processingRuns;
        public AbstractExperiment abstractExperiment;

        public ProcessingFileInfo(String name, String content, List<FileMetaDataTemplate> fileMetaDataTemplateList, List<ProcessingRun> processingRuns, AbstractExperiment abstractExperiment) {
            this.name = name;
            this.content = content;
            this.fileMetaDataTemplateList = fileMetaDataTemplateList;
            this.processingRuns = processingRuns;
            this.abstractExperiment = abstractExperiment;
        }
    }
}
