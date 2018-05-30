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
import com.infoclinika.mssharing.model.read.ProcessingRunReader;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
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
                return create(processingFile, experimentId, processingRunName);
            }
        }

        return false;

    }

    @Override
    public boolean isUserLabMembership(long user, long lab) {
        UserLabMembership userLabMembership = userLabMembershipRepository.findByLabAndUser(lab, user);
        return userLabMembership == null ? false: true;
    }

    private boolean create(ProcessingFile processingFile, long experiment, String processingRunName){

        ProcessingRun processingRun = processingFile.getProcessingRun();
        final ActiveExperiment activeExperiment = experimentRepository.findOne(experiment);

        boolean isProcessingRunNameExist = processingRunReader.findByProcessingRunName(processingRunName, experiment);

        if(processingRun == null){
            if(!isProcessingRunNameExist){
                if(activeExperiment != null){
                    processingRun = new ProcessingRun();
                    processingRun.setName(processingRunName);
                    processingRun.setExperimentTemplate(activeExperiment);
                    processingRun.getProcessingFiles().add(processingFile);
                    processingRunRepository.save(processingRun);
                    processingFile.setProcessingRun(processingRun);
                    processingFileRepository.save(processingFile);

                }else {
                    LOGGER.info("Experiment with Id: " + experiment + "does not exist !");
                }
            }else{
                processingRun = processingRunRepository.findByNameAndExperiment(processingRunName, experiment);
                processingFile.setProcessingRun(processingRun);
                processingFileRepository.save(processingFile);
            }

            LOGGER.info("#### Associating processes file successfully complete ####");
            LOGGER.info("#### Processing run successfully created ####");

            return true;
        }

        return false;
    }

    @Override
    public Map<String, Collection<String>> validateAssociateFiles(Map<String, Collection<String>> map,long experimentId, long user){

        Map<String, Collection<String>> collectionMap = new HashMap();
        Collection<String> collection = new ArrayList();
        ExperimentItem experimentItem = detailsReader.readExperiment(user, experimentId);

        for(Map.Entry<String, Collection<String>> entry : map.entrySet()){

            Collection<String> experimentFiles = entry.getValue();

                for(String fileName : experimentFiles) {
                    boolean activeFileMetaData = fileMetaDataRepository.findNameByInstrument(experimentItem.instrument.get(), fileName);

                    if(!activeFileMetaData){
                        collection.add(fileName);
                        collectionMap.put("error_files", collection);
                    }
                }
        }

        return collectionMap;
    }

}
