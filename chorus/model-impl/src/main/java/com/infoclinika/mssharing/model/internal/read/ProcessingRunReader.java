package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Transactional
public interface ProcessingRunReader {

    boolean findProcessingRunByExperiment(String name, long experiment);

    ProcessingRunInfo readProcessingRun(long processingRunId);

    ProcessingRunInfo readProcessingRunByNameAndExperiment(long experiment, String name);

    List<ProcessingRunInfo> readAllProcessingRunsByExperiment(long experiment);






    class ProcessingRunInfo{
        public Long id;
        public String name;
        public Date date;
        public AbstractExperiment abstractExperiment;
        public Set<ProcessingFile> processingFiles;

        public ProcessingRunInfo() {
        }

        public ProcessingRunInfo(Long id, String name, Date date) {
            this.id = id;
            this.name = name;
            this.date = date;
        }

        public ProcessingRunInfo(Long id, String name, Date date, AbstractExperiment abstractExperiment, Set<ProcessingFile> processingFiles) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.abstractExperiment = abstractExperiment;
            this.processingFiles = processingFiles;
        }
    }

}
