package com.infoclinika.mssharing.web.controller.v2.service;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.s3client.AwsS3ClientConfigurationService;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate;
import com.infoclinika.mssharing.web.controller.v2.dto.ExperimentDetails;
import com.infoclinika.mssharing.web.controller.v2.dto.ExperimentInfoDTO;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.inject.Inject;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ExperimentService {

    private static final Logger LOGGER = Logger.getLogger(ExperimentService.class);

    @Inject
    private AwsS3ClientConfigurationService awsConfigService;
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private DashboardReader dashboardReader;
    @Inject
    private RestAuthClientService restAuthClientService;
    @Inject
    private ExperimentCreationHelper experimentCreationHelper;
    @Inject
    private RuleValidator ruleValidator;




    public ResponseEntity<ExperimentInfoDTO> returnExperimentInfo(long userId, long experimentId){


        if(!ruleValidator.isExperimentExist(experimentId)){
            LOGGER.warn("#### Experiment with id:"  + experimentId + " does not exist !");
            return new ResponseEntity("Experiment with id: " + experimentId + " does not exist !", HttpStatus.BAD_REQUEST);
        }

        boolean isUserHasAcessToExperiment = restAuthClientService.isUserHasAcessToExperiment(userId, experimentId);

        if(isUserHasAcessToExperiment){
            return toExperimentInfoDTO(new ExperimentDetails(detailsReader.readExperiment(userId, experimentId), detailsReader.readExperimentShortInfo(userId, experimentId)));
        }else {

            LOGGER.warn("User does not have access to lab !");

            return new ResponseEntity("User does not have access to lab!", HttpStatus.UNAUTHORIZED);
        }
    }

    private ResponseEntity<ExperimentInfoDTO> experimentDetailsToInfoDTO(ExperimentDetails experimentDetails, ExperimentInfoDTO destination){
        Map<String, Collection<ExperimentInfoDTO.FileToSamplesDTO>> map = new HashMap<>();

        ExperimentItem experimentItemSource = experimentDetails.getExperimentItem();
        DictionaryItem dictionaryItem = experimentCreationHelper.specie(experimentItemSource.specie);
        DetailsReaderTemplate.ExperimentShortInfo shortInfo = experimentDetails.getExperimentShortInfo();
        InstrumentModelReaderTemplate.InstrumentModelLineTemplate instrumentModel = dashboardReader.readById(experimentItemSource.labHead, experimentItemSource.instrumentModel);

        destination.setName(experimentItemSource.name);
        destination.setLabName(shortInfo.labName);
        destination.setLaboratory(experimentItemSource.lab);
        destination.setDescription(experimentItemSource.description);
        destination.setInstrumentModel(instrumentModel.name);
        destination.setVendor(experimentItemSource.instrumentVendor);
        destination.setLabName(experimentItemSource.labName);
        destination.setProjectId(experimentItemSource.project);
        destination.setProjectName(shortInfo.projectName);
        destination.setSpecies(dictionaryItem.name);
        destination.setTechnologyType(instrumentModel.technologyType.name);
        destination.setExperimentType(experimentItemSource.experimentType);

        if(!experimentItemSource.files.isEmpty()){
            for(DetailsReaderTemplate.FileItemTemplate fileItem: experimentItemSource.files){
                destination.setFilesToSamples(computeExperimentFileSamples(fileItem, experimentItemSource.labHead, fileItem.instrumentId, shortInfo, map));

            }
        }


        LOGGER.info(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(destination));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(destination);
    }

    private Map<String, Collection<ExperimentInfoDTO.FileToSamplesDTO>> computeExperimentFileSamples(DetailsReaderTemplate.FileItemTemplate fileItem, long user, long instrument, DetailsReaderTemplate.ExperimentShortInfo shortInfo, Map<String, Collection<ExperimentInfoDTO.FileToSamplesDTO>> map){


        List<ExperimentInfoDTO.FileToSamplesDTO> list = new ArrayList<>();


        for (DetailsReaderTemplate.ShortExperimentFileItem file : shortInfo.files){

            if(fileItem.name.equals(file.name)){

                ExtendedShortExperimentFileItem fileItems = (ExtendedShortExperimentFileItem) file;

                if(!map.containsKey(fileItem.name)){

                    ImmutableList<ExtendedShortExperimentFileItem.ExperimentShortSampleItem> immutableList = fileItems.samples;
                    ExperimentInfoDTO.FileToSamplesDTO fileToSamplesDTO = new ExperimentInfoDTO.FileToSamplesDTO();

                    for (ExtendedShortExperimentFileItem.ExperimentShortSampleItem sampleItem : immutableList){
                        fileToSamplesDTO.setSampleName(sampleItem.condition.name);
                        fileToSamplesDTO.setInstrumentName(fileItem.instrumentName);
                    }

                    final NodePath nodePath = awsConfigService.returnExperimentStorageTargetFolder(user, instrument,fileItem.name);
                    fileToSamplesDTO.setFilePath(awsConfigService.generateTemporaryLinkToS3(nodePath.getPath()));

                    list.add(fileToSamplesDTO);
                    map.put(fileItem.name, list);

                }

            }

        }

        return map;
    }

    private ResponseEntity<ExperimentInfoDTO> toExperimentInfoDTO(ExperimentDetails experimentItemSource){
        return experimentDetailsToInfoDTO(experimentItemSource, new ExperimentInfoDTO());
    }

}
