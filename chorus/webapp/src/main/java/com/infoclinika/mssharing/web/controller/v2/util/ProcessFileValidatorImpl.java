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
    @Inject
    private ExperimentSampleRepository sampleRepository;

    public static final String NOT_EXISTS_PROCESSING_FILES = "NOT_EXISTS_PROCESSING_FILES";

    public static final String NOT_EXISTS_EXPERIMENT_FILES = "NOT_EXISTS_EXPERIMENT_FILES";

    public static final String NOT_EXISTS_EXPERIMENT_SAMPLE = "NOT_EXISTS_EXPERIMENT_SAMPLE";



    @Override
    public Map<String, Collection<String>> validateAssociationFiles(Map<String, Collection<String>> fileToFileMap, long experimentId, long user, ValidationType type) {



        Map<String, Collection<String>> map = new HashMap();
        Collection<String> collection = new ArrayList();

        switch(type){
            case EXPERIMENT_FILES:

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
                break;
            case PROCESSING_FILES: checkNotValidProcessingFiles(map, collection, experimentId);
                break;
        }

        return map;
    }

//
//    @Override
//    public Map<String, Collection<String>> validateAssociateExperimentFiles(Map<String, Collection<String>> map, long experimentId, long user) {
//        Map<String, Collection<String>> collectionMap = new HashMap();
//        Collection<String> collection = new ArrayList();
//
//
////        ExperimentItem experimentItem = detailsReader.readExperiment(user, experimentId);
////
////        for(Map.Entry<String, Collection<String>> entry : map.entrySet()){
////
////            Collection<String> experimentFiles = entry.getValue();
////
////            for(String fileName : experimentFiles) {
////                boolean activeFileMetaData = fileMetaDataRepository.findNameByInstrument(experimentItem.instrument.get(), fileName);
////
////                if(!activeFileMetaData){
////                    collection.add(fileName);
////                    collectionMap.put(NOT_EXISTS_EXPERIMENT_FILES, collection);
////                }
////            }
////        }
//
//        return collectionMap;
//    }

//    @Override
//    public Map<String, Collection<String>> checkValidProcessingFiles(ProcessingRunsDTO dto, long experiment, Map<String, Collection<String>> resultsMap) {
//
//        List<String> errorsData = new ArrayList();
////
////        for(Map.Entry<String, Collection<String>> entry : dto.getFileToFileMap().entrySet()){
////            boolean isProcessingFileAlreadyUploadedToExperiment = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, entry.getKey());
////
////            if(!isProcessingFileAlreadyUploadedToExperiment){
////                errorsData.add(entry.getKey());
////                resultsMap.put(NOT_EXISTS_PROCESSING_FILES, errorsData);
////            }
////        }
//        return resultsMap;
//    }

    @Override
    public Map<String, Collection<String>> validateSampleFileMap(Map<String, Collection<String>> sampleFileMap, long experiment, long user,ValidationType validationType) {

        Map<String, Collection<String>> resultsMap = new HashMap();
        List<String> collection = new ArrayList();

        switch(validationType){
            case PROCESSING_FILES:
                checkNotValidProcessingFiles(resultsMap, collection, experiment);
                break;
            case EXPERIMENT_SAMPLE:
                checkNotValidExperimentSample(user,experiment, sampleFileMap, resultsMap, collection);
                break;
        }

        return resultsMap;
    }

//    @Override
//    public Map<String, Collection<String>> checkExistsSampleInExperiment(Map<String, Collection<String>> sampleFileMap, long experiment, long user) {
//
////        Map<String, Long> sampleMap = new HashMap<>();
////
////        Map<String, Collection<String>> results = new HashMap();
////        List<String> list = Lists.newArrayList();
////
//        final DetailsReaderTemplate.ExperimentShortInfo shortInfo = detailsReader.readExperimentShortInfo(user, experiment);
//
//        for (DetailsReaderTemplate.ShortExperimentFileItem file : shortInfo.files) {
//
//            ExtendedShortExperimentFileItem fileItems = (ExtendedShortExperimentFileItem) file;
//
//            ImmutableList<ExtendedShortExperimentFileItem.ExperimentShortSampleItem> immutableList = fileItems.samples;
//
//            for(ExtendedShortExperimentFileItem.ExperimentShortSampleItem experimentShortSampleItem: immutableList){
//                if(!sampleMap.containsKey(experimentShortSampleItem.id)){
//                    sampleMap.put(experimentShortSampleItem.name, experimentShortSampleItem.id);
//                }
//            }
//        }
////
////
////        for(Map.Entry<String, Collection<String>> entry : sampleFileMap.entrySet()){
////
////
////
////        }
//
//
//        return null;
//    }

    private void checkNotValidProcessingFiles(Map<String, Collection<String>> map, Collection<String> collection, long experiment){
        for(Map.Entry<String, Collection<String>> entry : map.entrySet()){
            boolean uploaded = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, entry.getKey());
            if(!uploaded){
                collection.add(entry.getKey());
                map.put(NOT_EXISTS_PROCESSING_FILES, collection);
            }
        }
    }

    private void checkNotValidExperimentSample(long user, long experiment, Map<String, Collection<String>> sampleFileMap, Map<String, Collection<String>> map, Collection<String> collection){

        final DetailsReaderTemplate.ExperimentShortInfo shortInfo = detailsReader.readExperimentShortInfo(user, experiment);

        List<String> sampleList = new ArrayList();


        for(DetailsReaderTemplate.ShortExperimentFileItem file : shortInfo.files){
            ExtendedShortExperimentFileItem fileItems = (ExtendedShortExperimentFileItem) file;
            ImmutableList<ExtendedShortExperimentFileItem.ExperimentShortSampleItem> samples = fileItems.samples;

            for(ExtendedShortExperimentFileItem.ExperimentShortSampleItem sampleItem: samples){

                if(!sampleList.contains(sampleItem.name)){
                    sampleList.add(sampleItem.name);
                }
            }
        }


        for(String sample : sampleList){
            if(!sampleFileMap.containsKey(sample)){
                collection.add(sample);
                map.put(NOT_EXISTS_EXPERIMENT_SAMPLE, collection);
            }
        }

    }



}
