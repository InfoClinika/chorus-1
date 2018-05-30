package com.infoclinika.mssharing.web.controller.v2.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.internal.repository.ExperimentSampleRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunsDTO;
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


    @Override
    public Map<String, Collection<String>> validateAssociateFiles(Map<String, Collection<String>> map, long experimentId, long user) {
        Map<String, Collection<String>> collectionMap = new HashMap();
        Collection<String> collection = new ArrayList();
        ExperimentItem experimentItem = detailsReader.readExperiment(user, experimentId);

        for(Map.Entry<String, Collection<String>> entry : map.entrySet()){

            Collection<String> experimentFiles = entry.getValue();

            for(String fileName : experimentFiles) {
                boolean activeFileMetaData = fileMetaDataRepository.findNameByInstrument(experimentItem.instrument.get(), fileName);

                if(!activeFileMetaData){
                    collection.add(fileName);
                    collectionMap.put(NOT_EXISTS_EXPERIMENT_FILES, collection);
                }
            }
        }

        return collectionMap;
    }

    @Override
    public Map<String, Collection<String>> checkValidProcessingFilesToFileMap(ProcessingRunsDTO dto, long experiment, Map<String, Collection<String>> resultsMap) {

        List<String> errorsData = new ArrayList();

        for(Map.Entry<String, Collection<String>> entry : dto.getFileToFileMap().entrySet()){
            boolean isProcessingFileAlreadyUploadedToExperiment = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, entry.getKey());

            if(!isProcessingFileAlreadyUploadedToExperiment){
                errorsData.add(entry.getKey());
                resultsMap.put(NOT_EXISTS_PROCESSING_FILES, errorsData);
            }
        }
        return resultsMap;
    }

    @Override
    public Map<String, Collection<String>> checkExistProcessingFiles(Map<String, Collection<String>> map, long experiment, ValidationType validationType) {

        Map<String, Collection<String>> resultsMap = new HashMap();
        List<String> collection = new ArrayList();

        switch(validationType){
            case FILE_TO_FILE_MAP:
                for(Map.Entry<String, Collection<String>> entry : map.entrySet()){
                    boolean isProcessingFileAlreadyUploadedToExperiment = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, entry.getKey());

                    if(!isProcessingFileAlreadyUploadedToExperiment){
                        collection.add(entry.getKey());
                        resultsMap.put(NOT_EXISTS_PROCESSING_FILES, collection);
                    }
                }
                break;
            case SAMPLE_FILE_MAP:

                for (Map.Entry<String, Collection<String>> entry : map.entrySet()){
                    Collection<String> processingFileList = entry.getValue();

                    for(String name : processingFileList){
                        boolean isProcessingFileAlreadyUploadedToExperiment = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, name);

                        if(!isProcessingFileAlreadyUploadedToExperiment){
                            collection.add(name);
                            resultsMap.put(NOT_EXISTS_PROCESSING_FILES, collection);
                        }

                    }
                }
                break;
        }

        return resultsMap;
    }

    @Override
    public Map<String, Collection<String>> checkExistsSampleInExperiment(Map<String, Collection<String>> sampleFileMap, long experiment, long user) {

//        Map<String, Long> sampleMap = new HashMap<>();
//
//        Map<String, Collection<String>> results = new HashMap();
//        List<String> list = Lists.newArrayList();
//
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
//
//
//        for(Map.Entry<String, Collection<String>> entry : sampleFileMap.entrySet()){
//
//
//
//        }


        return null;
    }


}
