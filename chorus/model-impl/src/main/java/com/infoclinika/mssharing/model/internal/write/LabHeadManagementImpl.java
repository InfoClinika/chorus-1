/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedProject;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.FileReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultLabHeadManagement;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate.FileLineTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * @author andrii.loboda
 */
@Service
public class LabHeadManagementImpl extends DefaultLabHeadManagement implements LabHeadManagement {

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private FileReaderHelper<ActiveFileMetaData, FileLineTemplate> fileReaderHelper;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private DeletedProjectRepository deletedProjectRepository;
    @Inject
    private DeletedExperimentRepository deletedExperimentRepository;
    @Inject
    private DeletedFileMetaDataRepository deletedFileRepository;
    @Inject
    private DashboardReader dashboardReader;


    @Override
    protected void afterRemoveUserFromLab(long labHead, long labId, long userId, UserTemplate user, LabTemplate lab) {
        Lab actualLab = (Lab) lab;
        super.afterRemoveUserFromLab(labHead, labId, userId, user, lab);

        changeDeletedExperimentsOwnerToLabHead(userId, actualLab);
        changeDeletedProjectsOwnerToLabHead(userId, actualLab);
        changeDeletedFilesOwnerToLabHead(userId, actualLab);
    }

    private void changeDeletedProjectsOwnerToLabHead(long userId, Lab lab) {
        List<DeletedProject> deletedProjects = deletedProjectRepository.findByOwnerAndLab(userId, lab.getId());
        for (DeletedProject deletedProject : deletedProjects) {
            deletedProject.setCreator(lab.getHead());
            deletedProjectRepository.save(deletedProject);
        }
    }

    private void changeDeletedExperimentsOwnerToLabHead(long userId, Lab lab) {
        List<DeletedExperiment> deletedExperiments = deletedExperimentRepository.findByOwnerAndLab(userId, lab.getId());
        for (DeletedExperiment deletedExperiment : deletedExperiments) {
            deletedExperiment.setCreator(lab.getHead());
            deletedExperimentRepository.save(deletedExperiment);
        }
    }

    private void changeDeletedFilesOwnerToLabHead(long userId, Lab lab) {
        List<DeletedFileMetaData> deletedFiles = deletedFileRepository.findByOwnerAndLab(userId, lab.getId());
        for (DeletedFileMetaData deletedFile : deletedFiles) {
            deletedFile.setOwner(lab.getHead());
            deletedFileRepository.save(deletedFile);
        }
    }

}
