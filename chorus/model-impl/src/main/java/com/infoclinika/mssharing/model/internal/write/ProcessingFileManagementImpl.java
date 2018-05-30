package com.infoclinika.mssharing.model.internal.write;


import static com.google.common.base.Preconditions.*;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import com.infoclinika.mssharing.model.internal.entity.ProcessingRun;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.read.ProcessingRunReader;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.inject.Inject;
import java.util.*;

@Service
@Transactional
public class ProcessingFileManagementImpl implements ProcessingFileManagement{

    private static final Logger LOGGER = Logger.getLogger(ProcessingFileManagementImpl.class);

    @Inject
    private ProcessingFileRepository processingFileRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private ProcessingRunRepository processingRunRepository;
    @Inject
    private ProcessingRunReader processingRunReader;
    @Inject
    private ExperimentSampleRepository sampleRepository;

    @Override
    public long createProcessingFile(long experimentId, ProcessingFileShortInfo processingFileShortInfo) {

        checkNotNull(processingFileShortInfo);
        checkNotNull(processingFileShortInfo.name);

        final ActiveExperiment activeExperiment = experimentRepository.findOne(experimentId);
        final ProcessingFile processingFile = new ProcessingFile();
        processingFile.setContentId(processingFileShortInfo.content);
        processingFile.setName(processingFileShortInfo.name);
        processingFile.setExperiment(activeExperiment);

        processingFileRepository.save(processingFile);
        return processingFile.getId();
    }

    @Override
    public boolean isProcessingFileAlreadyUploadedToExperiment(long experiment, String fileName) {
        return processingFileRepository.isProcessingFileAlreadyUploadedToExperiment(experiment, fileName);
    }

    @Override
    public boolean associateProcessingFileWithRawFile(Map<String, Collection<String>> fileToFileMap, Map<String, Collection<String>> sampleFileMap, long experimentId, long userId, String processingRunName) {

        boolean results = false;

        LOGGER.info("#### Associating processes file start ####");

        final ExperimentShortInfo experimentShortInfo = detailsReader.readExperimentShortInfo(userId, experimentId);

        for(Map.Entry<String, Collection<String>> entry : fileToFileMap.entrySet()){

            ProcessingFile processingFile = processingFileRepository.findByName(entry.getKey(), experimentId);

            if(processingFile != null){

                boolean isAlreadyUpload = isProcessingFileAlreadyUploadedToExperiment(experimentId, entry.getKey());

                if(isAlreadyUpload){
                    if(experimentShortInfo.files.size() > 0){

                        experimentShortInfo.files.stream().forEach(file ->{
                            entry.getValue().stream().forEach(value -> {
                                if(file.name.equals(value)){

                                    ActiveFileMetaData fileMetaDataTemplate = fileMetaDataRepository.findOne(file.id);

                                    if(fileMetaDataTemplate != null){
                                            processingFile.getFileMetaDataTemplates().add(fileMetaDataTemplate);
                                    }
                                }
                            });
                        });
                    }else{
                        LOGGER.warn("#### Experiment does not have files");
                    }
                }
            }else{
                LOGGER.warn("Processing file name: " + entry.getKey() + " does not exists!");
            }

            results = createOrUpdate(processingFile, experimentId, processingRunName);
        }

        if(!sampleFileMap.isEmpty()){
            sampleToFileMap(sampleFileMap, experimentId, userId);
        }

        return results;
    }

    public void sampleToFileMap(Map<String, Collection<String>> sampleFileMap, long experiment, long user) {

        final ExperimentShortInfo  shortInfo = detailsReader.readExperimentShortInfo(user, experiment);

        Map<String, Long> samples = getSampleByFiles(shortInfo);

        for(Map.Entry<String, Collection<String>> entry : sampleFileMap.entrySet()){

            String key = entry.getKey();

            entry.getValue().stream().forEach(file ->{
               ProcessingFile processingFile = processingFileRepository.findByName(file, experiment);

               if(processingFile != null && samples.containsKey(key)){
                   ExperimentSample experimentSample = sampleRepository.findOne(samples.get(key));
                   processingFile.getExperimentSamples().add(experimentSample);
                   processingFileRepository.save(processingFile);
               }
            });
        }
    }





    private Map<String, Long> getSampleByFiles(ExperimentShortInfo shortInfo){
        Map<String, Long> sampleMap = new HashMap<>();

        for (DetailsReaderTemplate.ShortExperimentFileItem file : shortInfo.files) {
            ExtendedShortExperimentFileItem fileItems = (ExtendedShortExperimentFileItem) file;

            ImmutableList<ExtendedShortExperimentFileItem.ExperimentShortSampleItem> immutableList = fileItems.samples;

            for(ExtendedShortExperimentFileItem.ExperimentShortSampleItem experimentShortSampleItem: immutableList){
                if(!sampleMap.containsKey(experimentShortSampleItem.id)){
                    sampleMap.put(experimentShortSampleItem.name, experimentShortSampleItem.id);
                }
            }
        }

        return sampleMap;
    }


    private boolean createOrUpdate(ProcessingFile processingFile, long experiment, String processingRunName){

        final ActiveExperiment activeExperiment = experimentRepository.findOne(experiment);

        boolean isProcessingRunNameExist = processingRunReader.findProcessingRunByExperiment(processingRunName, experiment);

        if(!isProcessingRunNameExist && activeExperiment != null){
            ProcessingRun processingRun = new ProcessingRun();
            processingRun.setName(processingRunName);
            processingRun.setExperimentTemplate(activeExperiment);
            processingRun.addProcessingFile(processingFile);
            processingRunRepository.save(processingRun);
            processingFile.addProcessingRun(processingRun);
            processingFileRepository.save(processingFile);

            LOGGER.info("#### Associating processes file successfully complete ####");
            LOGGER.info("#### Processing run successfully created ####");

            return true;

        }else{

            return updateProcessingRun(processingFile, experiment, processingRunName);
        }
    }


    private boolean updateProcessingRun(ProcessingFile processingFile, long experiment, String processingRunName){
        ProcessingRun processingRun = processingRunRepository.findByNameAndExperiment(processingRunName, experiment);
        processingFile.addProcessingRun(processingRun);
        processingFileRepository.save(processingFile);
        processingRun.addProcessingFile(processingFile);
        processingRunRepository.save(processingRun);

        LOGGER.info("#### Associating processes file successfully complete ####");
        LOGGER.info("#### Processing run successfully updated ####");

        return true;
    }
}
