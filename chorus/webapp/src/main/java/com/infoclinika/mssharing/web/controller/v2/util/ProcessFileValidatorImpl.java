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

        Map<String, Collection<String>> map = null;
        Collection<String> collection = new ArrayList();

        switch(type){
            case EXPERIMENT_FILES:

                ExperimentItem experimentItem = detailsReader.readExperiment(user, experimentId);

                for(Map.Entry<String, Collection<String>> entry : fileToFileMap.entrySet()){

                    Collection<String> experimentFiles = entry.getValue();

                    for(String fileName : experimentFiles) {
                        boolean activeFileMetaData = fileMetaDataRepository.findNameByInstrument(experimentItem.instrument.get(), fileName);

                        if(!activeFileMetaData){
                            if(map == null){
                                map = new HashMap();
                            }

                            collection.add(fileName);
                            map.put(NOT_EXISTS_EXPERIMENT_FILES, collection);
                        }
                    }
                }
                break;
            case PROCESSING_FILES: checkNotValidProcessingFiles(map, collection, experimentId, fileToFileMap);
                break;
        }


        return map;
    }


    @Override
    public Map<String, Collection<String>> validateSampleFileMap(Map<String, Collection<String>> sampleFileMap, long experiment, long user,ValidationType validationType) {

        Map<String, Collection<String>> resultsMap = null;
        List<String> collection = new ArrayList();

        switch(validationType){
            case PROCESSING_FILE_SAMPLE:
                checkNotValidProcessingFileInSampleMap(resultsMap, collection, experiment, sampleFileMap);
                break;
            case EXPERIMENT_SAMPLE:
                checkNotValidExperimentSample(user,experiment, sampleFileMap, resultsMap, collection);
                break;
        }

        return resultsMap;
    }


    private void checkNotValidProcessingFiles(Map<String, Collection<String>> map, Collection<String> collection, long experiment, Map<String, Collection<String>> valueMap){

        Set<String> set = valueMap.keySet();

        for(String name: set){
            boolean uploaded = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, name);
            if(!uploaded){
                if(map == null){
                    map = new HashMap();
                }
                collection.add(name);
                map.put(NOT_EXISTS_PROCESSING_FILES, collection);
            }
        }
    }

    private void checkNotValidProcessingFileInSampleMap(Map<String, Collection<String>> map, Collection<String> collection, long experiment, Map<String, Collection<String>> sampleFileMap){

        for(Map.Entry<String, Collection<String>> entry: sampleFileMap.entrySet()){
            Collection<String> processingFiles = entry.getValue();

            for(String name : processingFiles){
                if(!processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, name)){
                    if(map == null){
                        map = new HashMap<>();
                    }
                    collection.add(name);
                    map.put(NOT_EXISTS_PROCESSING_FILES, collection);
                }
            }
        }
    }

    private void checkNotValidExperimentSample(long user, long experiment, Map<String, Collection<String>> sampleFileMap, Map<String, Collection<String>> map, Collection<String> collection){

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

    }



}
