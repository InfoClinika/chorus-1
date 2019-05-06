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

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.*;
import com.infoclinika.mssharing.model.internal.entity.workflow.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.InboxNotifierTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectSharingRequestManagement;
import com.infoclinika.mssharing.platform.repository.AttachmentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectSharingRequestRepositoryTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.internal.entity.Util.PROJECT_FROM_ID;
import static com.infoclinika.mssharing.model.internal.entity.Util.USER_FROM_ID;

/**
 * @author Stanislav Kurilin
 */
@Service
@Transactional
public class StudyManagementImpl implements StudyManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyManagementImpl.class);
    @Inject
    public EntityFactories factories;

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    private UserRepository userRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private FactorRepository factorRepository;
    @Inject
    private Provider<Date> current;
    @Inject
    private AttachmentManagement attachmentManagement;
    @Inject
    private AttachmentRepositoryTemplate<Attachment<User>> attachmentRepository;
    @Inject
    private Notifier notifier;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private RawFilesRepository experimentFileRepository;
    @Inject
    private DeletedExperimentRepository deletedExperimentRepository;
    @Inject
    private ExperimentLabelToExperimentManagement experimentLabelToExperimentManagement;
    @Inject
    private DeletedFileMetaDataRepository deletedFileMetaDataRepository;
    @Inject
    private DeletedProjectRepository deletedProjectRepository;
    @Inject
    private ProjectSharingRequestRepositoryTemplate<ProjectSharingRequestTemplate> projectSharingRequestRepository;
    @Inject
    private SharingManagement sharingManagement;
    @Inject
    private InboxNotifierTemplate inboxNotifier;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private TransactionTemplate transactionTemplate;
    @Inject
    private FileMovingManager fileMovingManager;
    @Inject
    private CopyProjectRequestRepository copyProjectRequestRepository;
    @Inject
    private StorageService fileStorageService;
    @Inject
    @Named("projectManagementImpl")
    private ProjectManagementTemplate<ProjectInfo> projectManagement;
    @Inject
    @Named("experimentManagementImpl")
    private ExperimentManagementTemplate<ExperimentInfo> experimentManagement;
    @Inject
    @Named("defaultProjectSharingRequestManagement")
    private ProjectSharingRequestManagement projectSharingRequestManagement;

    @Inject
    private LabRepository labRepository;
    @Inject
    private ExperimentPreparedSampleRepository preparedSampleRepository;
    @Inject
    private ExperimentSampleRepository experimentSampleRepository;
    @Inject
    private PrepToExperimentSampleRepository prepToExperimentSampleRepository;

    @Override
    public long createProject(long creator, ProjectInfo projectInfo) {
        return projectManagement.createProject(creator, projectInfo);
    }

    @Override
    public void removeProject(long actor, long project, boolean permanently) {
        if (!ruleValidator.canRemoveProject(actor, project)) {
            throw new AccessDenied(String.format("User {%d} cannot remove project {%d}", actor, project));
        }
        if (permanently) {
            removeProjectPermanently(project);
        } else {
            moveProjectToTrash(actor, project);
        }
    }

    @Override
    public void removeProject(long projectId) {
        final DeletedProject project = deletedProjectRepository.findOne(projectId);
        for (DeletedExperiment e : deletedExperimentRepository.findByProject(project.getId())) {
            removeCopiedFiles(e);
            deletedExperimentRepository.delete(e.getId());
        }
        deletedProjectRepository.delete(projectId);
        removeProjectSharingRequests(projectId);
    }

    @Override
    public void removeProject(long actor, long project) {

        if (!ruleValidator.canRemoveProject(actor, project)) {
            throw new AccessDenied("Couldn't remove project");
        }

        removeProjectCopyRequests(project);

        for (ActiveExperiment e : experimentRepository.findByProject(project)) {
            removeCopiedFiles(e);
            experimentRepository.delete(e.getId());
        }
        projectRepository.delete(project);
        removeProjectSharingRequests(project);
    }

    @Override
    public long copyProject(long actor, CopyProjectInfoTemplate copyProjectInfo) {
        return projectManagement.copyProject(actor, copyProjectInfo);
    }

    private Function<AbstractFileMetaData, AbstractFileMetaData> moveMetaDataFn() {
        final Map<AbstractFileMetaData, AbstractFileMetaData> originalCopyMap = newHashMap();
        return new Function<AbstractFileMetaData, AbstractFileMetaData>() {
            @Override
            public AbstractFileMetaData apply(AbstractFileMetaData originalMeta) {
                originalCopyMap.put(originalMeta, originalMeta);
                return originalMeta;
            }
        };
    }

    @Override
    public void updateProject(long actor, long projectId, ProjectInfo projectInfo) {
        projectManagement.updateProject(actor, projectId, projectInfo);
    }

    private void removeProjectPermanently(long project) {
        for (ActiveExperiment e : experimentRepository.findByProject(project)) {
            removeCopiedFiles(e);
            experimentRepository.delete(e.getId());
        }
        projectRepository.delete(project);
        removeProjectSharingRequests(project);
    }

    private void removeProjectCopyRequests(long project) {
        final List<CopyProjectRequest> requestList =
            copyProjectRequestRepository.findByProject(new ActiveProject(project));
        copyProjectRequestRepository.delete(requestList);
    }

    private void removeProjectSharingRequests(long projectId) {
        final List<ProjectSharingRequestTemplate> projectSharingRequests =
            projectSharingRequestRepository.findByProject(projectId);
        projectSharingRequestRepository.delete(projectSharingRequests);
    }

    @Override
    public long moveProjectToTrash(long actor, long projectId) {
        if (!ruleValidator.canRemoveProject(actor, projectId)) {
            throw new AccessDenied("Couldn't remove project");
        }
        final ActiveProject project = projectRepository.findOne(projectId);
        removeProjectCopyRequests(projectId);
        DeletedProject deletedProject =
            new DeletedProject(copyProjectFor(project.getCreator(), project, project.getName()));
        deletedProject.setLab(project.getLab());
        deletedProject = deletedProjectRepository.save(deletedProject);
        for (ActiveExperiment e : experimentRepository.findByProject(project.getId())) {
            e.setProject(deletedProject);
            moveExperimentEntityToTrash(e);
        }
        for (DeletedExperiment e : deletedExperimentRepository.findByProject(project.getId())) {
            e.setProject(deletedProject);
            deletedExperimentRepository.save(e);
        }
        projectRepository.delete(projectId);
        return deletedProject.getId();
    }

    @Override
    public long restoreProject(long actor, long projectId) {
        final DeletedProject deletedProject = deletedProjectRepository.findOne(projectId);
        if (!ruleValidator.canRestoreProject(actor, deletedProject)) {
            throw new AccessDenied("Couldn't restore project");
        }
        ActiveProject project = (copyProjectFor(deletedProject.getCreator(), deletedProject, deletedProject.getName()));
        project.setLab(deletedProject.getLab());
        project = projectRepository.save(project);
        for (DeletedExperiment e : deletedExperimentRepository.findByProject(deletedProject.getId())) {
            e.setProject(project);
            restoreExperimentEntity(e);
        }
        deletedProjectRepository.delete(deletedProject);
        return project.getId();
    }

    @Override
    public long newProjectSharingRequest(long requesterId, long experimentId, String downloadExperimentLink) {

        return projectSharingRequestManagement.newProjectSharingRequest(
            requesterId,
            experimentId,
            downloadExperimentLink
        );
    }

    @Override
    public void approveSharingProject(long actor, long project, long requester) {

        projectSharingRequestManagement.approveSharingProject(actor, project, requester);

    }

    @Override
    public void refuseSharingProject(long actor, long project, long requester, String refuseComment) {

        projectSharingRequestManagement.refuseSharingProject(actor, project, requester, refuseComment);

    }

    @Override
    public long newProjectCopyRequest(long actor, long copyTo, long project) {
        if (!ruleValidator.hasWriteAccessOnProject(actor, project)) {
            throw new AccessDenied("Couldn't create a project copy");
        }

        final User sender = checkNotNull(userRepository.findOne(actor));
        final User receiver = checkNotNull(userRepository.findOne(copyTo));
        final ActiveProject activeProject = checkNotNull(projectRepository.findOne(project));

        final CopyProjectRequest request = new CopyProjectRequest(receiver, sender, activeProject, new Date());
        notifier.sendCopyProjectRequestNotification(receiver.getId(), sender.getFullName(), activeProject.getName());
        return copyProjectRequestRepository.save(request).getId();
    }


    @Override
    public void refuseCopyProjectRequest(long actor, long project) {
        final List<CopyProjectRequest> allRequests = checkNotNull(copyProjectRequestRepository
            .findByReceiverAndProject(USER_FROM_ID.apply(actor), PROJECT_FROM_ID.apply(project)));
        copyProjectRequestRepository.delete(allRequests);
    }

    @Override
    public void approveCopyProjectRequest(long actor, long project, long billLaboratory) {
        final List<CopyProjectRequest> allCopyRequests = copyProjectRequestRepository.findByReceiverAndProject(
            USER_FROM_ID.apply(actor),
            PROJECT_FROM_ID.apply(project)
        );
        checkState(allCopyRequests.size() > 0, "No copy project request. Project: " + project + ", actor: " + actor);
        final Long creator = allCopyRequests.iterator().next().getProject().getCreator().getId();
        copyProject(actor, new CopyProjectInfo(project, actor, creator, billLaboratory, true));
        copyProjectRequestRepository.delete(allCopyRequests);
    }

    @Override
    public void removeExperiment(long experimentId) {
        deletedExperimentRepository.delete(experimentId);
    }

    @Override
    public long moveExperimentToTrash(long actor, long experimentId) {
        if (!ruleValidator.canRemoveExperiment(actor, experimentId)) {
            throw new AccessDenied("Couldn't remove experiment");
        }
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        return moveExperimentEntityToTrash(experiment);
    }

    @Override
    public long restoreExperiment(long actor, long experimentId) {
        final DeletedExperiment experiment = deletedExperimentRepository.findOne(experimentId);
        if (experiment == null) {
            LOGGER.warn("Can not find deleted experiment. May be it is already restored, id: {}", experimentId);
            return -1L;
        }
        if (!ruleValidator.canRestoreExperiment(actor, experiment)) {
            throw new AccessDenied("Couldn't restore experiment");
        }
        if (deletedProjectRepository.findOne(experiment.getProject().getId()) == null) {
            return restoreExperimentEntity(experiment);
        } else {
            return -1L;
        }
    }

    private long moveExperimentEntityToTrash(ActiveExperiment experiment) {
        DeletedExperiment deletedExperiment =
            new DeletedExperiment(copyExperimentFor(experiment.getProject(), experiment,
                experiment.getCreator(), experiment.getLab(), experiment.getBillLaboratory(), experiment.getName()
            ));
        final Function<AbstractFileMetaData, AbstractFileMetaData> copyRawFilesFn = moveMetaDataFn();
        copyRawFiles(experiment, deletedExperiment, copyRawFilesFn);

        setDownloadToken(experiment.getProject().getSharing().getType(), deletedExperiment);
        deletedExperiment = deletedExperimentRepository.save(deletedExperiment);
        experimentLabelToExperimentManagement.copyExperimentLabels(experiment.getId(), deletedExperiment.getId());
        experimentLabelToExperimentManagement.deleteExperimentLabels(experiment.getId());
        experimentRepository.delete(experiment);
        return deletedExperiment.getId();
    }

    private long restoreExperimentEntity(DeletedExperiment deletedExperiment) {
        for (ExperimentFileTemplate rawFile : deletedExperiment.getRawFiles().getData()) {
            if (deletedFileMetaDataRepository.findOne(rawFile.getFileMetaData().getId()) != null) {
                restoreFile((AbstractFileMetaData) rawFile.getFileMetaData());
            }
        }
        ActiveExperiment experiment = (copyExperimentFor(
            deletedExperiment.getProject(),
            deletedExperiment,
            deletedExperiment.getCreator(),
            deletedExperiment.getLab(),
            deletedExperiment.getBillLaboratory(),
            deletedExperiment.getName()
        ));
        experiment = experimentRepository.save(experiment);
        final Function<AbstractFileMetaData, AbstractFileMetaData> copyRawFilesFn = moveMetaDataFn();
        copyRawFiles(deletedExperiment, experiment, copyRawFilesFn);
        setDownloadToken(experiment.getProject().getSharing().getType(), experiment);
        experiment = experimentRepository.save(experiment);

        deletedExperimentRepository.delete(deletedExperiment);
        return experiment.getId();
    }

    private void restoreFile(AbstractFileMetaData deletedFile) {
        final DeletedFileMetaData deleted = (DeletedFileMetaData) deletedFile;

        if (ruleValidator.fileHasDuplicateName(deleted)) {
            throw new AccessDenied("Couldn't restore file");
        }
        ActiveFileMetaData fileMetaData = (ActiveFileMetaData) deleted.copy(deleted.getName(), deleted.getOwner());
        fileMetaData = fileMetaDataRepository.save(fileMetaData);
        final List<com.infoclinika.mssharing.model.internal.entity.RawFile> rawFiles =
            experimentFileRepository.findByMetaData(deleted);

        for (com.infoclinika.mssharing.model.internal.entity.RawFile rawFile : rawFiles) {
            rawFile.setFileMetaData(fileMetaData);
            experimentFileRepository.save(rawFile);
        }
        deletedFileMetaDataRepository.delete(deleted);
    }

    private void removeCopiedFiles(AbstractExperiment e) {
        for (ExperimentFileTemplate file : e.rawFiles.getData()) {
            if (file.getFileMetaData().isCopy() &&
                experimentFileRepository.findByMetaData(file.getFileMetaData()).size() == 1) {
                if (file.getFileMetaData() instanceof ActiveFileMetaData) {
                    experimentFileRepository.delete((com.infoclinika.mssharing.model.internal.entity.RawFile) file);
                    fileMetaDataRepository.delete((ActiveFileMetaData) file.getFileMetaData());
                } else {
                    experimentFileRepository.delete((com.infoclinika.mssharing.model.internal.entity.RawFile) file);
                    deletedFileMetaDataRepository.delete((DeletedFileMetaData) file.getFileMetaData());
                }
            }
        }
    }

    @Override
    public long createExperiment(final long creator, final ExperimentInfo info) {
        return experimentManagement.createExperiment(creator, info);
    }

    @Override
    public void deleteExperiment(long actor, long experiment) {
        experimentManagement.deleteExperiment(actor, experiment);
    }

    private void setDownloadToken(Sharing.Type type, AbstractExperiment experiment) {
        switch (type) {
            case PUBLIC:
                experiment.setDownloadToken(String.valueOf(new Random().nextLong()));
                break;
            case SHARED:
            case PRIVATE:
                experiment.setDownloadToken(null);
                break;
            default:
                throw new RuntimeException("Unsupported Sharing Type: " + type);
        }
    }

    @Override
    public void updateExperiment(final long actor, final long experimentId, final ExperimentInfo info) {

        experimentManagement.updateExperiment(actor, experimentId, info);

    }

    @Override
    public void runPreCacheViewers(long actor, long experimentId) {
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        precacheFiles(experiment);
    }

    @Override
    public void setBlogEnabled(long actor, long project, boolean blogEnabled) {
        ActiveProject entity = projectRepository.findOne(project);
        if (!ruleValidator.isProjectOwner(actor, project)
            && !(entity.getLab() != null && entity.getLab().getHead().getId().equals(actor))) {
            throw new AccessDenied("Couldn't update");
        }
        entity.setBlogEnabled(blogEnabled);
        projectRepository.save(entity);
    }

    private void precacheFiles(ActiveExperiment experiment) {
        LOGGER.warn("Skipping precaching files for experiment due to per-file translation scheme migration");
    }

    //TODO Refactor to avoid code duplicates with ProjectManagementImpl
    public ActiveProject copyProjectFor(User newOwner, AbstractProject project, String copyName) {
        List<Attachment<User>> copiedAttach = new ArrayList<>();
        for (Attachment<User> attachment : project.getAttachments()) {
            final Attachment copy = attachmentRepository.findOne(attachmentManagement.copyAttachment(attachment.getId(),
                newOwner.getId(), true
            ));
            copiedAttach.add(copy);
        }
        ActiveProject copy = new ActiveProject(newOwner, null, copyName, project.getAreaOfResearch(),
            project.getDescription()
        );

        copy.getAttachments().addAll(copiedAttach);
        return copy;
    }

    //TODO Refactor to avoid code duplicates with ProjectManagementImpl
    private void copyRawFiles(AbstractExperiment from,
                              final AbstractExperiment to,
                              final Function<AbstractFileMetaData, AbstractFileMetaData> copyMetaFn) {

        final Map<String, ExperimentPreparedSample> nameToNewPrepSample = copySamplesFromExperiment(from);
        final Collection<RawFile> copiedFiles = transform(from.rawFiles.getData(), new Function<RawFile, RawFile>() {
            @Override
            public RawFile apply(RawFile from) {
                final RawFile copyFile = new RawFile(
                    copyMetaFn.apply(from.getFileMetaData()),
                    from.getFractionNumber(),
                    nameToNewPrepSample.get(from.getPreparedSample().getName())
                );
                copyFile.setCopy(true);
                copyFile.setExperiment(to);
                return copyFile;
            }
        });
        final List<Factor> copiedFactors = new ArrayList<>();
        for (Factor factor : from.rawFiles.getFactors()) {
            copiedFactors.add(factorRepository.save(Factor.createCopy(factor, to)));
        }

        to.rawFiles.getData().clear();
        to.rawFiles.getData().addAll(copiedFiles);
        to.rawFiles.getFactors().clear();
        to.rawFiles.getFactors().addAll(copiedFactors);

        //TODO:2015-12-18:andrii.loboda: cover with tests
        addConditionsToLevelsAndRawFiles(to);
    }

    private Map<String, ExperimentPreparedSample> copySamplesFromExperiment(AbstractExperiment from) {
        final Map<String, ExperimentPreparedSample> nameToNewPrepSample = newHashMap();
        for (RawFile rawFile : from.rawFiles.getData()) {
            final ExperimentPreparedSample fromPreparedSample = rawFile.getPreparedSample();
            if (!nameToNewPrepSample.containsKey(fromPreparedSample.getName())) {
                final Set<PrepToExperimentSample> toPrepToExSample = newHashSet();
                final ExperimentPreparedSample preparedSampleToPersist =
                    new ExperimentPreparedSample(fromPreparedSample.getName(), toPrepToExSample);
                nameToNewPrepSample.put(
                    fromPreparedSample.getName(),
                    preparedSampleRepository.save(preparedSampleToPersist)
                );
                for (PrepToExperimentSample fromPrepToExSample : fromPreparedSample.getSamples()) {
                    final ExperimentSample fromSample = fromPrepToExSample.getExperimentSample();
                    final ExperimentSample toSample = experimentSampleRepository.save(new ExperimentSample(
                        fromSample.getName(),
                        newArrayList(fromSample.getFactorValues())
                    ));
                    final PrepToExperimentSample toPreparedSample =
                        prepToExperimentSampleRepository.save(new PrepToExperimentSample(
                            preparedSampleToPersist,
                            toSample,
                            fromPrepToExSample.getType()
                        ));
                }

            }

        }
        return nameToNewPrepSample;
    }

    //TODO Refactor to avoid code duplicates with ProjectManagementImpl
    private ActiveExperiment copyExperimentFor(
        AbstractProject project,
        AbstractExperiment experiment,
        User owner,
        Lab experimentLab,
        Lab newBillLab,
        String copyName
    ) {

        ActiveExperiment copy = new ActiveExperiment(
            owner,
            project,
            experimentLab,
            copyName,
            experiment.getExperiment(),
            new Date(),
            experiment.getInstrumentRestriction(),
            experiment.getExperimentType(),
            experiment.getSpecie(),
            experiment.getBounds(),
            new ArrayList<>(experiment.getLockMasses()),
            experiment.getSampleTypesCount(),
            experiment.getChannelsCount(),
            experiment.getLabelType(),
            experiment.getGroupSpecificParametersType(),
            experiment.getReporterMassTol(),
            experiment.isFilterByPIFEnabled(),
            experiment.getMinReporterPIF(),
            experiment.getMinBasePeakRatio(),
            experiment.getMinReporterFraction(),
            experiment.getExperimentCategory(),
            experiment.getNgsRelatedData(),
            experiment.isFailed()
        );

        copy.setBillLaboratory(newBillLab);
        List<Attachment<User>> copiedAttach = new ArrayList<>();
        for (Attachment<User> attachment : experiment.attachments) {
            final Attachment<User> att =
                attachmentRepository.findOne(attachmentManagement.copyAttachment(attachment.getId(),
                    owner.getId(), false
                ));
            copiedAttach.add(att);
        }
        copy.attachments = copiedAttach;
        return copy;
    }

    //TODO Refactor to avoid code duplicates with ProjectManagementImpl
    private void addConditionsToLevelsAndRawFiles(AbstractExperiment ex) {
        final Set<ExperimentSample> samples = newHashSet();
        for (RawFile rawFile : ex.getRawFiles().getData()) {
            for (PrepToExperimentSample prepToExSample : rawFile.getPreparedSample().getSamples()) {
                samples.add(prepToExSample.getExperimentSample());
            }
        }
        new SampleConditionsFactory(ex, ex.rawFiles.getFactors(), samples).create();
    }


}
