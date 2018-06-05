package com.infoclinika.mssharing.web.controller.v2.util;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.model.internal.repository.ExperimentSampleRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.text.CollationElementIterator;
import java.util.*;

@Component
@Transactional
public class ProcessFileValidatorImpl implements ProcessFileValidator {

    @Inject
    private DetailsReader detailsReader;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private ProcessingFileManagement processingFileManagement;

    public static final String NOT_EXISTS_PROCESSING_FILES = "NOT_EXISTS_PROCESSING_FILES";

    public static final String NOT_EXISTS_EXPERIMENT_FILES = "NOT_EXISTS_EXPERIMENT_FILES";

    public static final String NOT_EXISTS_EXPERIMENT_SAMPLE = "NOT_EXISTS_EXPERIMENT_SAMPLE";



    @Override
    public Map<String, Collection<String>> validateAssociationFiles(Map<String, Collection<String>> fileToFileMap, long experimentId, long user, ValidationType type) {
        switch(type){
            case EXPERIMENT_FILES:
                return checkNotValidExperimentFiles(fileToFileMap, experimentId, user);
            case PROCESSING_FILES:
                return checkNotValidProcessingFiles(experimentId, fileToFileMap);
        }

        return null;
    }


    @Override
    public Map<String, Collection<String>> validateSampleFileMap(Map<String, Collection<String>> sampleFileMap, long experiment, long user,ValidationType validationType) {

        switch(validationType){
            case PROCESSING_FILE_SAMPLE:
                return checkNotValidProcessingFileInSampleMap(experiment, sampleFileMap);
            case EXPERIMENT_SAMPLE:
                return checkNotValidExperimentSample(user,experiment, sampleFileMap);
        }

        return null;
    }


    private Map<String, Collection<String>> checkNotValidProcessingFiles(long experiment, Map<String, Collection<String>> fileToFileMap){
        Map<String, Collection<String>> map = new HashMap<>();
        List<String> collection = new ArrayList();
        Set<String> set = fileToFileMap.keySet();

        for(String name: set){
            boolean uploaded = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, name);
            if(!uploaded){
                collection.add(name);
                map.put(NOT_EXISTS_PROCESSING_FILES, collection);
            }
        }
        return map;
    }

    private Map<String, Collection<String>> checkNotValidProcessingFileInSampleMap(long experiment, Map<String, Collection<String>> sampleFileMap){

        Map<String, Collection<String>> map = new HashMap<>();
        List<String> collection = new ArrayList();

        for(Map.Entry<String, Collection<String>> entry: sampleFileMap.entrySet()){
            Collection<String> processingFiles = entry.getValue();

            for(String name : processingFiles){
                if(!processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, name)){
                    collection.add(name);
                    map.put(NOT_EXISTS_PROCESSING_FILES, collection);
                }
            }
        }
        return map;
    }

    private Map<String, Collection<String>> checkNotValidExperimentSample(long user, long experiment, Map<String, Collection<String>> sampleFileMap){

        Map<String, Collection<String>> map = new HashMap<>();
        List<String> collection = new ArrayList();

        final DetailsReaderTemplate.ExperimentShortInfo shortInfo = detailsReader.readExperimentShortInfo(user, experiment);

        List<String> experimentSamples = new ArrayList();

        for(DetailsReaderTemplate.ShortExperimentFileItem file : shortInfo.files){
            ExtendedShortExperimentFileItem fileItems = (ExtendedShortExperimentFileItem) file;
            ImmutableList<ExtendedShortExperimentFileItem.ExperimentShortSampleItem> samples = fileItems.samples;

            for(ExtendedShortExperimentFileItem.ExperimentShortSampleItem sampleItem: samples){

                if(!experimentSamples.contains(sampleItem.name)){
                    experimentSamples.add(sampleItem.name);
                }
            }
        }

        Set<String> set = sampleFileMap.keySet();

        for(String s : set){
            if(!experimentSamples.contains(s)){
                if(map == null){
                    map = new HashMap<>();
                }
                collection.add(s);
                map.put(NOT_EXISTS_EXPERIMENT_SAMPLE, collection);
            }
        }

        return map;

    }

    private Map<String, Collection<String>> checkNotValidExperimentFiles(Map<String, Collection<String>> fileToFileMap, long experimentId, long user){

        Map<String, Collection<String>> map = new HashMap<>();
        List<String> collection = new ArrayList();

        ExperimentItem experimentItem = detailsReader.readExperiment(user, experimentId);

        for(Map.Entry<String, Collection<String>> entry : fileToFileMap.entrySet()){

            Collection<String> experimentFiles = entry.getValue();

            for(String fileName : experimentFiles) {
                boolean activeFileMetaData = fileMetaDataRepository.findNameByInstrument(experimentItem.instrument.get(), fileName);

                if(!activeFileMetaData){
                    collection.add(fileName);
                    map.put(NOT_EXISTS_EXPERIMENT_FILES, collection);
                }
            }
        }
        return map;
    }



}
