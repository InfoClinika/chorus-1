package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.ProcessingRun;
import com.infoclinika.mssharing.model.internal.repository.ProcessingRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
@Transactional(readOnly = true)
public class ProcessingRunReaderImpl implements ProcessingRunReader {

    @Inject
    private ProcessingRunRepository processingRunRepository;



    @Override
    public boolean findProcessingRunByExperiment(String name, long experiment) {
        return processingRunRepository.findByNameAndExperiment(name, experiment) != null ? true : false;
    }

    @Override
    public ProcessingRunReader.ProcessingRunInfo readProcessingRun(long processingRunId) {
        final ProcessingRun processingRun = (ProcessingRun) find(processingRunId);
        checkNotNull(processingRun);

        return createProcessingRunInfo(processingRun);
    }

    @Override
    public ProcessingRunInfo readProcessingRunByNameAndExperiment(long experiment, String name) {
        final ProcessingRun processingRun = processingRunRepository.findByNameAndExperiment(name, experiment);
        checkNotNull(processingRun);
        return createProcessingRunInfo(processingRun);
    }

    @Override
    public List<ProcessingRunInfo> readAllProcessingRunsByExperiment(long experiment) {
        List<ProcessingRun> processingRuns = processingRunRepository.findAll(experiment);
        List<ProcessingRunInfo> processingRunInfos = new ArrayList<>();

        for(ProcessingRun processingRun : processingRuns){
            ProcessingRunInfo processingRunInfo = new ProcessingRunInfo();
            processingRunInfo.id = processingRun.getId();
            processingRunInfo.name = processingRun.getName();
            processingRunInfo.date = processingRun.getProcessedDate();
            processingRunInfos.add(processingRunInfo);
        }

        return processingRunInfos;
    }


    private Object find(long id){
        return checkNotNull(processingRunRepository.findOne(id), "Couldn't find processing file with id %s", id);
    }


    private ProcessingRunInfo createProcessingRunInfo(ProcessingRun processingRun){
        return new ProcessingRunInfo(processingRun.getId(), processingRun.getName(),
                                        processingRun.getProcessedDate(), processingRun.getExperimentTemplate(), processingRun.getProcessingFiles());

    }



}
