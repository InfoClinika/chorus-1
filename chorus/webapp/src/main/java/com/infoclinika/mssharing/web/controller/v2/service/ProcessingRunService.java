package com.infoclinika.mssharing.web.controller.v2.service;


import com.infoclinika.mssharing.model.helper.ProcessingFileItem;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.read.ProcessingRunReader;
import com.infoclinika.mssharing.model.internal.s3client.AwsS3ClientConfigurationService;
import com.infoclinika.mssharing.model.read.dto.details.ProcessingRunItem;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.model.write.ProcessingRunManagement;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingFileDTO;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunDetails;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunsDTO;
import com.infoclinika.mssharing.web.controller.v2.util.ProcessValidator;
import com.infoclinika.mssharing.web.controller.v2.util.ValidationType;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service
@Transactional
public class ProcessingRunService {

    private static final Logger LOGGER = Logger.getLogger(ProcessingRunService.class);


    @Inject
    private ProcessingFileManagement processingFileManagement;
    @Inject
    private RestAuthClientService restAuthClientService;
    @Inject
    private ProcessingRunReader processingRunReader;
    @Inject
    private ProcessingRunManagement processingRunManagement;
    @Inject
    private ProcessValidator processValidator;
    @Inject
    private RuleValidator ruleValidator;

    @Inject
    private AwsS3ClientConfigurationService awsConfigService;


    public ResponseEntity<Object> createProcessingRun(ProcessingRunsDTO dto, long user, long experiment) {

        boolean isUserHasAccessToExperiment = restAuthClientService.isUserHasAccessToExperiment(user, experiment);
        boolean isProcessingRunAlreadyExist = processingRunReader.findProcessingRunByExperiment(dto.getName(), experiment);

        if ((dto.getFileToFileMap() == null || dto.getFileToFileMap().size() == 0) && (dto.getSampleFileMap() == null || dto.getSampleFileMap().size() == 0)) {
            return createProcessingRunWithoutAssociateFiles(dto.getName(), user, experiment, isUserHasAccessToExperiment, isProcessingRunAlreadyExist);
        }

        if ((dto.getFileToFileMap() == null || dto.getFileToFileMap().size() == 0) && (dto.getSampleFileMap() != null || dto.getSampleFileMap().size() != 0)) {
            return new ResponseEntity("You can`t create sample file map without association file map !", HttpStatus.BAD_REQUEST);
        }

        Collection<Map> notValidData = new ArrayList();

        returnValidationFileToFileMapResults(dto, experiment, user, notValidData);

        returnValidationSampleFileResults(dto, notValidData, experiment, user);


        if (!notValidData.isEmpty()) {
            return new ResponseEntity("Association data does not exists !" + notValidData.toString(), HttpStatus.BAD_REQUEST);
        }

        return createProcessing(dto, user, experiment, isProcessingRunAlreadyExist, isUserHasAccessToExperiment);
    }


    public ResponseEntity<Object> updateProcessingRun(ProcessingRunsDTO dto, long experiment, long user) {

        if (processingRunReader.findProcessingRunByExperiment(dto.getName(), experiment)) {
            return associateProcessingFile(dto, experiment, user);
        } else {
            LOGGER.warn("#### Processing Run with name: " + dto.getName() + " does not exists by experiment id: " + experiment);
            return new ResponseEntity("Processing Run with name: " + dto.getName() + " does not exists by experiment id: " + experiment, HttpStatus.BAD_REQUEST);
        }
    }


    public ResponseEntity<List<ProcessingRunsDTO.ProcessingRunsShortDetails>> getAllProcessingRuns(long experiment, long user){

        if(ruleValidator.canUserReadExperiment(user, experiment)){
            if(restAuthClientService.isUserHasAccessToExperiment(user, experiment)){
                return processingRunDetailsToDto(experiment);
            }

            LOGGER.warn("#### User with ID: " + user + "does not have access to lab ####");
            return new ResponseEntity("User with ID: " + user + "does not have access to lab", HttpStatus.UNAUTHORIZED);

        }

        return new ResponseEntity("Experiment by id: " + experiment + " not found", HttpStatus.BAD_REQUEST);


    }

    public ResponseEntity<ProcessingRunDetails> showProcessingRunDetails(long processingRunId, long user, long experiment){

        if(processValidator.isProcessingRunExist(processingRunId, experiment)){
            if(ruleValidator.canUserReadExperiment(user, experiment)){
                if(restAuthClientService.isUserHasAccessToExperiment(user, experiment)){
                    return processingRunItemToDTO(processingRunId,experiment);
                }

                LOGGER.warn("#### User with ID: " + user + "does not have access to lab ####");
                return new ResponseEntity("User with ID: " + user + "does not have access to lab", HttpStatus.UNAUTHORIZED);
            }else {
                return new ResponseEntity("Experiment by id: " + experiment + " not found", HttpStatus.BAD_REQUEST);
            }
        }else {
            return new ResponseEntity("Processing Run by id: " + processingRunId + " not found", HttpStatus.BAD_REQUEST);
        }
    }



    private ResponseEntity<ProcessingRunDetails> processingRunItemToDTO(long processingRunId,long experiment){

        ProcessingRunItem processingRunItem = processingRunReader.readProcessingRun(processingRunId, experiment);
        ProcessingRunDetails processingRunsDTO = new ProcessingRunDetails();
        processingRunsDTO.setName(processingRunItem.getName());
        processingRunsDTO.setId(processingRunItem.getId());
        processingRunsDTO.setProcessedDate(processingRunItem.getDate().toString());

        List<ProcessingFileDTO> processingFiles = new ArrayList<>();

        for(ProcessingFileItem processingFileItem : processingRunItem.getProcessingFileItems()){

            ProcessingFileDTO processedFile = new ProcessingFileDTO(processingFileItem.getId(), processingFileItem.getName(),
                    awsConfigService.generateTemporaryLinkToS3(processingFileItem.getFilePath()), processingFileItem.getExperimentFiles(),
                    processingFileItem.getExperimentSampleItems());
            processingFiles.add(processedFile);
        }
        processingRunsDTO.setProcessedFiles(processingFiles);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(processingRunsDTO);
    }






    private ResponseEntity<List<ProcessingRunsDTO.ProcessingRunsShortDetails>> processingRunDetailsToDto(long experiment){
        List<ProcessingRunReader.ProcessingRunInfo> processingRuns =  processingRunReader.readAllProcessingRunsByExperiment(experiment);

        List<ProcessingRunsDTO.ProcessingRunsShortDetails> dtoList = new ArrayList<>();

        for(ProcessingRunReader.ProcessingRunInfo processingRunInfo : processingRuns){

            ProcessingRunsDTO.ProcessingRunsShortDetails shortDetails = new ProcessingRunsDTO.ProcessingRunsShortDetails();
            shortDetails.setId(processingRunInfo.id);
            shortDetails.setName(processingRunInfo.name);
            shortDetails.setProcessedDate(processingRunInfo.date.toString());

            dtoList.add(shortDetails);
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoList);
    }




    private ResponseEntity<Object> createProcessing(ProcessingRunsDTO dto, long user, long experiment, boolean processingRunExist, boolean isUserHasAccessToExperiment) {
        if (!processingRunExist) {
            if (isUserHasAccessToExperiment) {

                return processingFileManagement.associateProcessingFileWithRawFile(dto.getFileToFileMap(), dto.getSampleFileMap(), experiment, user, dto.getName()) ?
                        new ResponseEntity("Processing Run with name: " + dto.getName() + " successfully created", HttpStatus.OK) :
                        new ResponseEntity("Processing files already has processing run", HttpStatus.BAD_REQUEST);

            } else {

                LOGGER.warn("#### User with ID: " + user + "does not have access to lab ####");
                return new ResponseEntity("User with ID: " + user + "does not have access to lab", HttpStatus.UNAUTHORIZED);
            }
        } else {

            LOGGER.warn("#### Processing Run with name: " + dto.getName() + " already exists ####");
            return new ResponseEntity("Processing Run with name: " + dto.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<Object> createProcessingRunWithoutAssociateFiles(String name, long user, long experiment, boolean isUserHasAccessToExperiment, boolean isProcessingRunAlreadyExist) {

        if (!isProcessingRunAlreadyExist) {
            if (isUserHasAccessToExperiment) {
                processingRunManagement.create(experiment, name);
                return new ResponseEntity("Processing Run: " + name + " successfully created", HttpStatus.OK);
            } else {
                LOGGER.warn("#### User with ID: " + user + "does not have access to lab ####");
                return new ResponseEntity("User with ID: " + user + "does not have access to lab", HttpStatus.UNAUTHORIZED);
            }
        }
        return new ResponseEntity("Processing Run: " + name + " already exists " + " by experiment id: " + experiment, HttpStatus.BAD_REQUEST);
    }


    private ResponseEntity<Object> associateProcessingFile(ProcessingRunsDTO dto, long experiment, long user) {

        if ((dto.getFileToFileMap() == null || dto.getFileToFileMap().size() == 0) && (dto.getSampleFileMap() != null || dto.getSampleFileMap().size() != 0)) {
            return new ResponseEntity("You can`t create sample file map without association file map !", HttpStatus.BAD_REQUEST);
        }

        Collection<Map> notValidData = new ArrayList<>();

        returnValidationFileToFileMapResults(dto, experiment, user, notValidData);

        returnValidationSampleFileResults(dto, notValidData, experiment, user);

        if (notValidData.isEmpty()) {
            return processingFileManagement.associateProcessingFileWithRawFile(dto.getFileToFileMap(), dto.getSampleFileMap(), experiment, user, dto.getName()) ?
                    new ResponseEntity("Processing Run with name: " + dto.getName() + " successfully updated", HttpStatus.OK) :
                    new ResponseEntity("Processing files already has processing run", HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity("Please check your input data: " + notValidData.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    private void returnValidationFileToFileMapResults(ProcessingRunsDTO dto, long experiment, long user, Collection<Map> maps) {

        Map experimentFilesMap = processValidator.validateAssociationFiles(dto.getFileToFileMap(), experiment, user, ValidationType.EXPERIMENT_FILES);
        Map processingFilesMap = processValidator.validateAssociationFiles(dto.getFileToFileMap(), experiment, user, ValidationType.PROCESSING_FILES);

        if (!experimentFilesMap.isEmpty()) {
            maps.add(experimentFilesMap);
        }
        if (!processingFilesMap.isEmpty()) {
            maps.add(processingFilesMap);
        }
    }

    private void returnValidationSampleFileResults(ProcessingRunsDTO dto, Collection<Map> maps, long experiment, long user) {
        if (dto.getSampleFileMap() != null) {
            Map experimentFileSample = processValidator.validateSampleFileMap(dto.getSampleFileMap(), experiment, user, ValidationType.EXPERIMENT_SAMPLE);
            Map processingFileSample = processValidator.validateSampleFileMap(dto.getSampleFileMap(), experiment, user, ValidationType.PROCESSING_FILE_SAMPLE);

            if (!experimentFileSample.isEmpty()) {
                maps.add(experimentFileSample);
            }
            if (!processingFileSample.isEmpty()) {
                maps.add(processingFileSample);
            }
        }

    }
}
