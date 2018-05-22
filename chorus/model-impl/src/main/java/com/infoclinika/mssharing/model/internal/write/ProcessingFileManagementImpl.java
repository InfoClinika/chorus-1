package com.infoclinika.mssharing.model.internal.write;


import static com.google.common.base.Preconditions.*;

import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import com.infoclinika.mssharing.model.internal.entity.ProcessingRun;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.ProcessingFileRepository;
import com.infoclinika.mssharing.model.internal.repository.ProcessingRunRepository;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.internal.read.ProcessingRunReader;
import com.infoclinika.mssharing.model.write.ProcessingFileManagement;
import com.infoclinika.mssharing.platform.entity.UserLabMembership;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRepositoryTemplate;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.inject.Inject;
import javax.inject.Named;
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

    @Named("userLabMembershipRepository")
    @Inject
    private UserLabMembershipRepositoryTemplate userLabMembershipRepository;

    @Override
    public long createProcessingFile(long experimentId, ProcessingFileShortInfo processingFileShortInfo) {

        checkNotNull(processingFileShortInfo);
        checkNotNull(processingFileShortInfo.name);

        final ActiveExperiment activeExperiment = experimentRepository.findOne(experimentId);
        final ProcessingFile processingFile = new ProcessingFile();
        processingFile.setContentId(processingFileShortInfo.content);
        processingFile.setName(processingFileShortInfo.name);
        processingFile.setExperimentTemplate(activeExperiment);

        processingFileRepository.save(processingFile);
        return processingFile.getId();
    }

    @Override
    public boolean isProcessingFileAlreadyUploadedToExperiment(long experiment, String fileName) {
        return processingFileRepository.isProcessingFileAlreadyUploadedToExperiment(experiment, fileName);
    }

    @Override
    public boolean associateProcessingFileWithRawFile(Map<String, Collection<String>> map, long experimentId, long userId, String processingRunName) {

        boolean results = false;

        LOGGER.info("#### Associating processes file start ####");

        final DetailsReaderTemplate.ExperimentShortInfo experimentShortInfo = detailsReader.readExperimentShortInfo(userId, experimentId);

        for(Map.Entry<String, Collection<String>> entry : map.entrySet()){

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

            if(processingFile != null){
                results = apply(processingFile, experimentId, processingRunName);
            }
        }

        return results;

    }

    @Override
    public boolean isUserLabMembership(long user, long lab) {
        UserLabMembership userLabMembership = userLabMembershipRepository.findByLabAndUser(lab, user);
        return userLabMembership == null ? false: true;
    }

    private boolean apply(ProcessingFile processingFile, long experiment, String processingRunName){

        final ActiveExperiment activeExperiment = experimentRepository.findOne(experiment);

        boolean isProcessingRunNameExist = processingRunReader.findProcessingRunByExperiment(processingRunName, experiment);

        if(!isProcessingRunNameExist){
            if(activeExperiment != null){
                ProcessingRun processingRun = new ProcessingRun();
                processingRun.setName(processingRunName);
                processingRun.setExperimentTemplate(activeExperiment);
                processingRun.getProcessingFiles().add(processingFile);
                processingRunRepository.save(processingRun);
//                processingFile.setProcessingRun(processingRun);
                processingFileRepository.save(processingFile);

                LOGGER.info("#### Associating processes file successfully complete ####");
                LOGGER.info("#### Processing run successfully created ####");

                return true;
            }else {
                LOGGER.info("Experiment with Id: " + experiment + "does not exist !");
            }
        }else{
            return updateProcessingFiles(processingFile, experiment, processingRunName);
        }

        return false;
    }


    private boolean updateProcessingFiles(ProcessingFile processingFile, long experiment, String processingRunName){
        ProcessingRun processingRun = processingRunRepository.findByNameAndExperiment(processingRunName, experiment);

        if(processingFile.getProcessingRuns() == null){
//            processingFile.setProcessingRun(processingRun);
            processingFileRepository.save(processingFile);

            processingRun.getProcessingFiles().add(processingFile);
            processingRunRepository.save(processingRun);

            LOGGER.info("#### Associating processes file successfully complete ####");
            LOGGER.info("#### Processing run successfully updated ####");

            return true;
        }

        return false;
    }
}
