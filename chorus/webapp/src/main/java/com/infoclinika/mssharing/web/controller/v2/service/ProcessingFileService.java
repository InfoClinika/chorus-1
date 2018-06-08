package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.s3client.AwsS3ClientConfigurationService;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
public class ProcessingFileService {

    private static final Logger LOGGER = Logger.getLogger(ProcessingFileService.class);

    private static final CloudStorageService CLOUD_STORAGE_SERVICE = CloudStorageFactory.service();
    public static final String UPLOAD_COMPLETE = "UPLOAD COMPLETE";
    public static final String ALREADY_EXISTS = "ALREADY EXISTS";


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
    private RuleValidator ruleValidator;


    public ResponseEntity<Object> uploadFileToStorage(long user, long experimentId, MultipartFile[] multipartFiles) throws IOException {

        if(ruleValidator.canUserReadExperiment(user, experimentId)){
            LOGGER.info("#### Start upload file to storage ####");
            long start  = System.currentTimeMillis();

            Map<String, Collection<String>> resultsProcessingFiles = new HashMap();
            List<String> uploadComplete = new ArrayList();
            List<String> uploadErrors = new ArrayList();

            boolean isUserHasAccessToExperiment = restAuthClientService.isUserHasAccessToExperiment(user, experimentId);
            final DetailsReaderTemplate.ExperimentShortInfo experimentShortInfo = detailsReader.readExperimentShortInfo(user, experimentId);

            if(isUserHasAccessToExperiment && experimentShortInfo.files.size() > 0){

                if(multipartFiles.length > 0){
                    for (MultipartFile multipartFile: multipartFiles) {

                        File file = convertMultipartToFile(multipartFile);
                        startUploadProcessingFilesToStorage(experimentId, file, resultsProcessingFiles, uploadComplete, uploadErrors);
                        file.delete();
                    }

                    long end  = System.currentTimeMillis();
                    long value = end - start;
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(value);

                    LOGGER.info(minutes + " minutes needed to upload file ####");
                    LOGGER.info(resultsProcessingFiles.toString() + " #### Upload results processing files ####");

                    return new ResponseEntity(resultsProcessingFiles.toString(), HttpStatus.OK);
                }
            }
            return new ResponseEntity("User with ID: " + user + " does not have access to lab", HttpStatus.UNAUTHORIZED);
        }else {
            return new ResponseEntity("Experiment by id: " + experimentId + " not found", HttpStatus.BAD_REQUEST);
        }


    }




    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
            File result = new File(tmpDir, multipartFile.getOriginalFilename());
            multipartFile.transferTo(result);
            return result;
    }


    private void startUploadProcessingFilesToStorage(long experiment, File file, Map<String, Collection<String>> map,List<String> uploadDone, List<String> uploadExists){

        boolean processingFileAlreadyExist = processingFileManagement.isProcessingFileAlreadyUploadedToExperiment(experiment, file.getName());
        final NodePath nodePath = awsConfigService.returnProcessingStorageTargetFolder(experiment, file.getName());
        final CloudStorageItemReference itemReference = awsConfigService.storageItemReference(nodePath.getPath());

        if(!processingFileAlreadyExist && !CLOUD_STORAGE_SERVICE.existsAtCloud(itemReference)){

            CLOUD_STORAGE_SERVICE.uploadToCloud(file, itemReference.getBucket(), itemReference.getKey());
            final ProcessingFileManagement.ProcessingFileShortInfo processingFileShortInfo = new ProcessingFileManagement.ProcessingFileShortInfo(file.getName(), itemReference.getKey());
            processingFileManagement.createProcessingFile(experiment, processingFileShortInfo);

            uploadDone.add(file.getName());
            map.put(UPLOAD_COMPLETE, uploadDone);

            LOGGER.info("Processing file  have been upload to storage: " + nodePath.getPath());

        }else {

            uploadExists.add(file.getName());
            map.put(ALREADY_EXISTS, uploadExists);

            LOGGER.info("Processing file with name key: " + nodePath.getPath() + " already exists");
        }
    }




}
