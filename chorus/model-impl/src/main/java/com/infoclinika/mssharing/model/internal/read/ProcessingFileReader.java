package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.ProcessingRun;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ProcessingFileReader {

    ProcessingFileInfo readProcessingFileInfo(long processingFile);


    class ProcessingFileInfo{
        public String name;
        public String content;
        public List<FileMetaDataTemplate> fileMetaDataTemplateList;
        public ProcessingRun processingRun;
        public AbstractExperiment abstractExperiment;

        public ProcessingFileInfo(String name, String content, List<FileMetaDataTemplate> fileMetaDataTemplateList, ProcessingRun processingRun, AbstractExperiment abstractExperiment) {
            this.name = name;
            this.content = content;
            this.fileMetaDataTemplateList = fileMetaDataTemplateList;
            this.processingRun = processingRun;
            this.abstractExperiment = abstractExperiment;
        }
    }
}
