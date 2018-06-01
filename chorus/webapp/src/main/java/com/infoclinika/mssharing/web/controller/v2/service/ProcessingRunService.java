package com.infoclinika.mssharing.web.controller.v2.service;


import com.infoclinika.mssharing.model.internal.read.ProcessingRunReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.model.write.ProcessingRunManagement;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunsDTO;
import com.infoclinika.mssharing.web.controller.v2.util.ProcessFileValidator;
import com.infoclinika.mssharing.web.controller.v2.util.ValidationType;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Service
@Transactional
public class ProcessingRunService {

    private static final Logger LOGGER = Logger.getLogger(ProcessingRunService.class);

    public static final String PROCESSING_FILES = "Processing files";
    public static final String EXPERIMENT_FILES = "Experiment files";


    @Inject
    private ProcessingFileManagement processingFileManagement;
    @Inject
    private RestAuthClientService restAuthClientService;
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private ProcessingRunReader processingRunReader;
    @Inject
    private ProcessingRunManagement processingRunManagement;
    @Inject
    private ProcessFileValidator processFileValidator;






    public ResponseEntity<Object> createProcessingRun(ProcessingRunsDTO dto, long user, long experiment){

        boolean isUserHasAccessToExperiment = restAuthClientService.isUserHasAccessToExperiment(user, experiment);
        boolean isProcessingRunAlreadyExist  = processingRunReader.findProcessingRunByExperiment(dto.getName(), experiment);

//        Collection<Map> maps = new ArrayList();

        // if user does not input fileToFileMap and sampleToFile

        if(dto.getFileToFileMap() == null || dto.getFileToFileMap().isEmpty() && dto.getSampleFileMap() == null || dto.getSampleFileMap().isEmpty()){
            return createProcessingRunWithoutAssociate(dto.getName(), user, experiment, isUserHasAccessToExperiment, isProcessingRunAlreadyExist);
        }

        // else validate experiment files

//        maps.add(processFileValidator.validateAssociationFiles(dto.getFileToFileMap(), experiment, user, ValidationType.EXPERIMENT_FILES));
//        maps.add(processFileValidator.validateAssociationFiles(dto.getFileToFileMap(), experiment, user, ValidationType.PROCESSING_FILES));

        Collection<Map> maps = returnValidationResults(dto, experiment, user);


        if(!maps.isEmpty()){
            return new ResponseEntity("Association data does not exists !" + maps.toString(), HttpStatus.BAD_REQUEST);
        }

        return createProcessing(dto, user, experiment, isProcessingRunAlreadyExist, isUserHasAccessToExperiment);

    }


    public ResponseEntity<Object> updateProcessingRun(ProcessingRunsDTO dto, long experiment, long user){

        if(processingRunReader.findProcessingRunByExperiment(dto.getName(), experiment)){
            return associateProcessingFile(dto, experiment, user);
        }else{
            LOGGER.warn("#### Processing Run with name: "+ dto.getName() +" does not exists by experiment id: " + experiment);
            return new ResponseEntity("Processing Run with name: "+ dto.getName() +" does not exists by experiment id: " + experiment, HttpStatus.BAD_REQUEST);
        }
    }



    private ResponseEntity<Object> createProcessing(ProcessingRunsDTO dto, long user, long experiment, boolean processingRunExist, boolean isUserLabMembership){
        if(!processingRunExist){
            if(isUserLabMembership){

                return processingFileManagement.associateProcessingFileWithRawFile(dto.getFileToFileMap(), dto.getSampleFileMap(), experiment, user, dto.getName()) ?
                        new ResponseEntity("Processing Run with name: " + dto.getName() + " successfully created", HttpStatus.OK) :
                        new ResponseEntity("Processing files already has processing run", HttpStatus.BAD_REQUEST);

            }else {

                LOGGER.warn("#### User with ID: " + user + "does not have access to lab ####");
                return new ResponseEntity("User with ID: " + user + "does not have access to lab", HttpStatus.UNAUTHORIZED);
            }
        }else {

            LOGGER.warn("#### Processing Run with name: "+ dto.getName() +" already exists ####");
            return new ResponseEntity("Processing Run with name: "+ dto.getName() +" already exists", HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<Object> createProcessingRunWithoutAssociate(String name,long user, long experiment, boolean isUserHasAccessToExperiment, boolean isProcessingRunAlreadyExist){

        if(!isProcessingRunAlreadyExist){
            if(isUserHasAccessToExperiment){
                processingRunManagement.create(experiment, name);
                return new ResponseEntity("Processing Run: " + name + " successfully created", HttpStatus.OK);
            }else {
                LOGGER.warn("#### User with ID: " + user + "does not have access to lab ####");
                return new ResponseEntity("User with ID: " + user + "does not have access to lab", HttpStatus.UNAUTHORIZED);
            }
        }
        return new ResponseEntity("User with ID: " + user + "does not have access to lab", HttpStatus.UNAUTHORIZED);
    }


//    private Map<String, Map<String, Collection<String>>> returnValidateResults(Map<String, Collection<String>> processingFilesMap, Map<String, Collection<String>> experimentFilesMap){
//        Map<String, Map<String, Collection<String>>> map = new HashMap();
//
//        if(!processingFilesMap.isEmpty()){
//            map.put(PROCESSING_FILES, processingFilesMap);
//        }else if(!experimentFilesMap.isEmpty()){
//            map.put(EXPERIMENT_FILES, experimentFilesMap);
//        }
//
//        return map;
//    }

    private ResponseEntity<Object> associateProcessingFile(ProcessingRunsDTO dto, long experiment, long user){
//        Map<String, Map<String, Collection<String>>> validateResults = returnValidateResults(
//                processFileValidator.validateAssociationFiles(dto.getFileToFileMap(), experiment,user, ValidationType.PROCESSING_FILES),
//                processFileValidator.validateAssociationFiles(dto.getFileToFileMap(), experiment, user,ValidationType.EXPERIMENT_FILES));

        Collection<Map> maps = returnValidationResults(dto, experiment, user);

        if(maps.isEmpty()){

            return processingFileManagement.associateProcessingFileWithRawFile(dto.getFileToFileMap(), dto.getSampleFileMap(),experiment, user, dto.getName()) ?
                    new ResponseEntity("Processing Run with name: " + dto.getName() + " successfully updated", HttpStatus.OK) :
                    new ResponseEntity("Processing files already has processing run", HttpStatus.BAD_REQUEST);

        }else {
            return new ResponseEntity("Please check your associating data: " + maps.toString(),HttpStatus.BAD_REQUEST);
        }
    }

    private Collection<Map> returnValidationResults(ProcessingRunsDTO dto, long experiment, long user){

        Collection<Map> maps = new ArrayList();

        maps.add(processFileValidator.validateAssociationFiles(dto.getFileToFileMap(), experiment, user, ValidationType.EXPERIMENT_FILES));
        maps.add(processFileValidator.validateAssociationFiles(dto.getFileToFileMap(), experiment, user, ValidationType.PROCESSING_FILES));

        return maps;
    }
}
