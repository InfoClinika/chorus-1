package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.helper.ProcessingFileItem;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import com.infoclinika.mssharing.model.internal.entity.ProcessingRun;
import com.infoclinika.mssharing.model.internal.repository.ProcessingRunRepository;
import com.infoclinika.mssharing.model.read.dto.details.ProcessingRunItem;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

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
    public ProcessingRunItem readProcessingRun(long processingRunId, long experiment) {
        final ProcessingRun processingRun = processingRunRepository.findByIdAndExperimentId(experiment, processingRunId);
        if(processingRun != null){
            return processingRunItemTransform(processingRun);
        }else {
            return null;
        }
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


    private ProcessingRunItem processingRunItemTransform(ProcessingRun processingRun){

        List<ProcessingFileItem> processingFileItems = newArrayList();
        List<String> experimentFiles = null;
        List<String> experimentSampleItems = null;

        Set<ProcessingFile> processingFiles = processingRun.getProcessingFiles();

        if(!processingFiles.isEmpty()){

            for(ProcessingFile file : processingFiles){
                if(!file.getFileMetaDataTemplates().isEmpty()){
                    if(experimentFiles == null){
                        experimentFiles = newArrayList();
                    }

                    for(FileMetaDataTemplate fileMetaDataTemplate : file.getFileMetaDataTemplates()){
                        experimentFiles.add(fileMetaDataTemplate.getName());
                    }

                    if(!file.getExperimentSamples().isEmpty()){
                        if(experimentSampleItems == null){
                            experimentSampleItems = newArrayList();
                        }
                        for(ExperimentSample experimentSample : file.getExperimentSamples()){
                            experimentSampleItems.add(experimentSample.getName());
                        }
                    }

                    ProcessingFileItem processingFileItem = new ProcessingFileItem(file.getId(), file.getName(), file.getContentId(), experimentSampleItems, experimentFiles);
                    processingFileItems.add(processingFileItem);
                }
                experimentSampleItems.clear();
                experimentFiles.clear();
            }

        }

//
//
//
//
//
//
//
//
//        for (ProcessingFile file : processingFiles){
//
//
//            for(FileMetaDataTemplate fileMetaDataTemplate : file.getFileMetaDataTemplates()){
//
//            }
//
//
//
//
//        }

        return new ProcessingRunItem(processingRun.getId(), processingRun.getName(), processingFileItems, processingRun.getProcessedDate());

    }



}
