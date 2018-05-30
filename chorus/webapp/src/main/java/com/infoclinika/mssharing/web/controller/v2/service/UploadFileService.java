package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.internal.s3client.AwsS3ClientConfigurationService;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ProcessingRunReader;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunsDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UploadFileService {

    private static final Logger LOGGER = Logger.getLogger(UploadFileService.class);

    private static final CloudStorageService CLOUD_STORAGE_SERVICE = CloudStorageFactory.service();

    @Value("${multipart.location}")
    private String tmpDir;

    @Inject
    private ProcessingFileManagement processingFileManagement;
    @Inject
    private AwsS3ClientConfigurationService awsConfigService;
    @Inject
    private RestAuthClientService restAuthClientService;
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private ProcessingRunReader processingRunReader;


    public ResponseEntity<Object> uploadFileToStorage(long user, long experimentId, MultipartFile[] multipartFiles) throws IOException {

        LOGGER.info("#### Start upload file to storage ####");
        long start  = System.currentTimeMillis();

        Map<String, Collection<String>> resultsProcessingFiles = new HashMap();
        List<String> uploadComplete = new ArrayList();
        List<String> uploadErrors = new ArrayList();

        boolean isUserLabMembership = restAuthClientService.isUserHasAcessToExperiment(user, experimentId);
        final DetailsReaderTemplate.ExperimentShortInfo experimentShortInfo = detailsReader.readExperimentShortInfo(user, experimentId);

        if(isUserLabMembership && experimentShortInfo.files.size() > 0){

            if(multipartFiles.length > 0){
                for (MultipartFile multipartFile: multipartFiles) {

                    File file = convertMultipartToFile(multipartFile);
                    startUploadFilesToStorage(experimentId, file, resultsProcessingFiles, uploadComplete, uploadErrors);
                    file.delete();
                }

                long end  = System.currentTimeMillis();
                long value = end - start;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(value);

                LOGGER.info(minutes + " minutes need to upload file ####");
                LOGGER.info(resultsProcessingFiles.toString() + " #### Upload results processing files ####");

                return new ResponseEntity(resultsProcessingFiles.toString(), HttpStatus.OK);
            }
        }
        LOGGER.warn(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity("User with ID: " + user + " does not have access to lab", HttpStatus.UNAUTHORIZED);
    }




    public ResponseEntity<Object> createProcessingRun(ProcessingRunsDTO dto, long user, long experiment){

        boolean isUserHasAcessToExperiment = restAuthClientService.isUserHasAcessToExperiment(user, experiment);
        boolean isProcessingRunAlreadyExist  = processingRunReader.findByProcessingRunName(dto.getName(), experiment);

        Map<String, Collection<String>> map = processingFileManagement.validateAssociateFiles(dto.getFileToFileMap(), experiment, user);

        if(!map.isEmpty()){
            return new ResponseEntity("Files in experiment does not exists !" + map.toString(), HttpStatus.BAD_REQUEST);
        }

        Map<String, Collection<String>> resultsMap = new HashMap();
        List<String> errorsData = new ArrayList();

        return  checkNoValidFilesToFileMap(dto, experiment, resultsMap, errorsData) ?
                new ResponseEntity("You can't create processing runs with not valid data, please check processing file name and experiment file name !  " + resultsMap.toString(), HttpStatus.BAD_REQUEST):
                createProcessingRunAndAssociateProcessingFiles(dto, user, experiment, isProcessingRunAlreadyExist, isUserHasAcessToExperiment);
    }


    private void startUploadFilesToStorage(long experiment, File file, Map<String, Collection<String>> map,List<String> uploadDone, List<String> uploadExists){

        boolean processingFileAlreadyExist = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, file.getName());
        final NodePath nodePath = awsConfigService.returnProcessingStorageTargetFolder(experiment, file.getName());
        final CloudStorageItemReference itemReference = awsConfigService.storageItemReference(nodePath.getPath());

        if(!processingFileAlreadyExist && !CLOUD_STORAGE_SERVICE.existsAtCloud(itemReference)){

            CLOUD_STORAGE_SERVICE.uploadToCloud(file, itemReference.getBucket(), itemReference.getKey());
            final ProcessingFileManagement.ProcessingFileShortInfo processingFileShortInfo = new ProcessingFileManagement.ProcessingFileShortInfo(file.getName(), itemReference.getKey());
            processingFileManagement.createProcessingFile(experiment, processingFileShortInfo);

            uploadDone.add(file.getName());
            map.put("uploadComplete", uploadDone);
            LOGGER.info("Processing file  have been upload to storage: " + nodePath.getPath());

        }else {

            uploadExists.add(file.getName());
            map.put("alreadyExists", uploadExists);
            LOGGER.info("Processing file with name key: " + nodePath.getPath() + " already exists");
        }
    }






    private boolean checkNoValidFilesToFileMap(ProcessingRunsDTO dto, long experiment, Map<String, Collection<String>> resultsMap, List<String> errorsData){

        for(Map.Entry<String, Collection<String>> entry : dto.getFileToFileMap().entrySet()){
            boolean exist = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, entry.getKey());

            if(!exist){
                errorsData.add(entry.getKey());
                resultsMap.put("not valid data", errorsData);
            }
        }
        return !resultsMap.isEmpty();
    }



    private ResponseEntity<Object> createProcessingRunAndAssociateProcessingFiles(ProcessingRunsDTO dto, long user, long experiment, boolean processingRunExist, boolean isUserLabMembership){
        if(!processingRunExist){
            if(isUserLabMembership){
                return processingFileManagement.associateProcessingFileWithRawFile(dto.getFileToFileMap(), experiment, user, dto.getName()) ?
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

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File result = new File(tmpDir, multipartFile.getOriginalFilename());
        multipartFile.transferTo(result);
        return result;
    }

}
