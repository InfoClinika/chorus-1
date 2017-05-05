/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.CopyProjectRequest;
import com.infoclinika.mssharing.model.internal.entity.ExperimentPreparedSample;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.Factor;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.MSFunctionItem;
import com.infoclinika.mssharing.model.internal.entity.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.UserLabFileTranslationData;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractProject;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedProject;
import com.infoclinika.mssharing.model.internal.entity.restorable.StorageData;
import com.infoclinika.mssharing.model.internal.entity.restorable.TranslationStatus;
import com.infoclinika.mssharing.model.internal.repository.CopyProjectRequestRepository;
import com.infoclinika.mssharing.model.internal.repository.DeletedExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.DeletedFileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.DeletedProjectRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentPreparedSampleRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentSampleRepository;
import com.infoclinika.mssharing.model.internal.repository.FactorRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import com.infoclinika.mssharing.model.internal.repository.MSFunctionItemRepository;
import com.infoclinika.mssharing.model.internal.repository.PrepToExperimentSampleRepository;
import com.infoclinika.mssharing.model.internal.repository.ProjectRepository;
import com.infoclinika.mssharing.model.internal.repository.RawFilesRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.AttachmentManagement;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.model.write.SharingManagement;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.entity.Attachment;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.ProjectSharingRequestTemplate;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.InboxNotifierTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectSharingRequestManagement;
import com.infoclinika.mssharing.platform.repository.AttachmentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectSharingRequestRepositoryTemplate;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.internal.entity.Util.PROJECT_FROM_ID;
import static com.infoclinika.mssharing.model.internal.entity.Util.USER_FROM_ID;
import static com.infoclinika.mssharing.model.internal.entity.restorable.TranslationStatus.Status.IN_PROGRESS;
import static com.infoclinika.mssharing.model.internal.entity.restorable.TranslationStatus.Status.SUCCESS;

/**
 * @author Stanislav Kurilin
 */
@Service
@Transactional
public class StudyManagementImpl implements StudyManagement {

    private static final Logger LOG = Logger.getLogger(StudyManagementImpl.class);
    public static final int FILE_RANGE_PARTS_COUNT = 24;

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
    private MSFunctionItemRepository msFunctionItemRepository;
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

    private void removeProjectPermanently(long project) {
        for (ActiveExperiment e : experimentRepository.findByProject(project)) {
            removeCopiedFiles(e);
            experimentRepository.delete(e.getId());
        }
        projectRepository.delete(project);
        removeProjectSharingRequests(project);
    }

    @Override
    public void removeProject(long actor, long project) {

        if (!ruleValidator.canRemoveProject(actor, project)) throw new AccessDenied("Couldn't remove project");

        removeProjectCopyRequests(project);

        for (ActiveExperiment e : experimentRepository.findByProject(project)) {
            removeCopiedFiles(e);
            experimentRepository.delete(e.getId());
        }
        projectRepository.delete(project);
        removeProjectSharingRequests(project);

    }

    private void removeProjectCopyRequests(long project) {
        final List<CopyProjectRequest> requestList = copyProjectRequestRepository.findByProject(new ActiveProject(project));
        copyProjectRequestRepository.delete(requestList);
    }

    private void removeProjectSharingRequests(long projectId) {
        final List<ProjectSharingRequestTemplate> projectSharingRequests = projectSharingRequestRepository.findByProject(projectId);
        projectSharingRequestRepository.delete(projectSharingRequests);
    }

    @Override
    public long moveProjectToTrash(long actor, long projectId) {
        if (!ruleValidator.canRemoveProject(actor, projectId)) throw new AccessDenied("Couldn't remove project");
        final ActiveProject project = projectRepository.findOne(projectId);
        removeProjectCopyRequests(projectId);
        DeletedProject deletedProject = new DeletedProject(copyProjectFor(project.getCreator(), project, project.getName()));
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
        if (!ruleValidator.canRestoreProject(actor, deletedProject)) throw new AccessDenied("Couldn't restore project");
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

        return projectSharingRequestManagement.newProjectSharingRequest(requesterId, experimentId, downloadExperimentLink);
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
        if (!ruleValidator.hasWriteAccessOnProject(actor, project))
            throw new AccessDenied("Couldn't create a project copy");

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
        final List<CopyProjectRequest> allCopyRequests = copyProjectRequestRepository.findByReceiverAndProject(USER_FROM_ID.apply(actor), PROJECT_FROM_ID.apply(project));
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
        if (!ruleValidator.canRemoveExperiment(actor, experimentId))
            throw new AccessDenied("Couldn't remove experiment");
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        return moveExperimentEntityToTrash(experiment);
    }

    @Override
    public long restoreExperiment(long actor, long experimentId) {
        final DeletedExperiment experiment = deletedExperimentRepository.findOne(experimentId);
        if (experiment == null) {
            LOG.warn("Can not find deleted experiment. May be it is already restored, id: " + experimentId);
            return -1L;
        }
        if (!ruleValidator.canRestoreExperiment(actor, experiment))
            throw new AccessDenied("Couldn't restore experiment");
        if (deletedProjectRepository.findOne(experiment.getProject().getId()) == null) {
            return restoreExperimentEntity(experiment);
        } else {
            return -1L;
        }
    }

    private long moveExperimentEntityToTrash(ActiveExperiment experiment) {
        DeletedExperiment deletedExperiment = new DeletedExperiment(copyExperimentFor(experiment.getProject(), experiment,
                experiment.getCreator(), experiment.getLab(), experiment.getBillLaboratory(), experiment.getName()));
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
        ActiveExperiment experiment = (copyExperimentFor(deletedExperiment.getProject(), deletedExperiment,
                deletedExperiment.getCreator(), deletedExperiment.getLab(), deletedExperiment.getBillLaboratory(), deletedExperiment.getName()));
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
//        if (!validator.userIsOperatorOfInstrumentForFile(actor).apply(deleted)) {
//            throw new AccessDenied("Cannot restore file");
//        }
        if (ruleValidator.fileHasDuplicateName(deleted)) {
            throw new AccessDenied("Couldn't restore file");
        }
        ActiveFileMetaData fileMetaData = (ActiveFileMetaData) deleted.copy(deleted.getName(), deleted.getOwner());
        copyMSData(deleted, fileMetaData, fileMetaData.getInstrument().getLab());
        fileMetaData = fileMetaDataRepository.save(fileMetaData);
        final List<com.infoclinika.mssharing.model.internal.entity.RawFile> rawFiles = experimentFileRepository.findByMetaData(deleted);

        for (com.infoclinika.mssharing.model.internal.entity.RawFile rawFile : rawFiles) {
            rawFile.setFileMetaData(fileMetaData);
            experimentFileRepository.save(rawFile);
        }
        deletedFileMetaDataRepository.delete(deleted);
    }

    private void removeCopiedFiles(AbstractExperiment e) {
        for (ExperimentFileTemplate file : e.rawFiles.getData()) {
            if (file.getFileMetaData().isCopy() && experimentFileRepository.findByMetaData(file.getFileMetaData()).size() == 1) {
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
        }
    }

    @Override
    public void updateExperiment(final long actor, final long experimentId, final ExperimentInfo info) {

        experimentManagement.updateExperiment(actor, experimentId, info);

    }

    /* Translation methods*/

    @Override
    @Async
    public void removeTranslationData(long actor, Iterable<Long> files, long lab) {

        for (Long file : files) {
            removeTranslationData(actor, file, lab);
        }

    }

    @Override
    public void removeTranslationData(long actor, long file, final long lab) {

        final ActiveFileMetaData metaData = checkNotNull(fileMetaDataRepository.findOne(file), "File {" + file + "} not found");

        if (!ruleValidator.canRemoveTranslationData(actor, file, lab)) {
            throw new AccessDenied("Cannot remove translation data for file {" + file + "}");
        }

        doRemoveTranslationData(actor, lab, metaData);

    }

    @Override
    @Async
    public void removeTranslationDataOfExperimentFiles(long actor, long experiment) {

        if (!ruleValidator.canRemoveAnyExperimentTranslationData(actor, experiment)) {
            throw new AccessDenied(String.format("Cannot remove experiment {%d} translation data", experiment));
        }

        final ActiveExperiment activeExperiment = experimentRepository.findOne(experiment);
        final Optional<Lab> labToRemoveTranslationData = fromNullable(activeExperiment.getLab() == null ? activeExperiment.getBillLaboratory() : activeExperiment.getLab());
        checkState(labToRemoveTranslationData.isPresent(), String.format("Lab for the experiment {%d} is not specified!", experiment));

        final Lab lab = labToRemoveTranslationData.get();

        for (ExperimentFileTemplate rawFile : activeExperiment.getRawFiles().getData()) {
            doRemoveTranslationData(actor, lab.getId(), (ActiveFileMetaData) rawFile.getFileMetaData());
        }

    }

    private void doRemoveTranslationData(long actor, Long labId, ActiveFileMetaData fileMetaData) {

        final ImmutableList<UserLabFileTranslationData> filteredTranslationData = copyOf(functionsForFileForUserForLab(actor, labId, fileMetaData));

        for (UserLabFileTranslationData translationData : filteredTranslationData) {
            removeTranslationData(fileMetaData, translationData);
        }

        fileMetaDataRepository.save(fileMetaData);
    }

    private FluentIterable<UserLabFileTranslationData> functionsForFileForUserForLab(final long actor, final long lab, AbstractFileMetaData metaData) {
        return from(metaData.getUsersFunctions())
                .filter(and(new Predicate<UserLabFileTranslationData>() {
                    @Override
                    public boolean apply(UserLabFileTranslationData input) {
                        return input.getUser().getId().equals(actor) || input.getLab().getHead().getId().equals(actor);
                    }
                }, new Predicate<UserLabFileTranslationData>() {
                    @Override
                    public boolean apply(UserLabFileTranslationData input) {
                        return input.getLab().getId().equals(lab);
                    }
                }));
    }

    private void removeTranslationData(AbstractFileMetaData metaData, UserLabFileTranslationData userTranslationDataToDelete) {

        final ImmutableSet<MSFunctionItem> oldMSFunctionItems = ImmutableSet.copyOf(userTranslationDataToDelete.getFunctions());

        metaData.getUsersFunctions().remove(userTranslationDataToDelete);

        for (MSFunctionItem oldMSFunctionItem : oldMSFunctionItems) {
            removeTranslationDataFromStorage(metaData, oldMSFunctionItem);
        }
    }

    private void removeTranslationDataFromStorage(AbstractFileMetaData metaData, final MSFunctionItem msFunctionItem) {
        if (msFunctionItemRepository.countByTranslatedPath(msFunctionItem.getTranslatedPath()) == 0) {
            fileStorageService.delete(new NodePath(msFunctionItem.getTranslatedPath()));
        } else {
            LOG.debug(String.format("*** Translation data of the file {%d} is used in other files and is not removed from Storage. Path: {%s}", metaData.getId(), msFunctionItem.getTranslatedPath()));
        }
    }

    private boolean isFunctionUsedByOtherLabs(AbstractFileMetaData metaData, final MSFunctionItem msFunctionItem) {
        return size(from(metaData.getUsersFunctions())
                .transformAndConcat(new Function<UserLabFileTranslationData, Iterable<MSFunctionItem>>() {
                    @Override
                    public Iterable<MSFunctionItem> apply(UserLabFileTranslationData input) {
                        return input.getFunctions();
                    }
                })
                .filter(new Predicate<MSFunctionItem>() {
                    @Override
                    public boolean apply(MSFunctionItem input) {
                        return input.equals(msFunctionItem);
                    }
                })) > 0;
    }

    @Override
    public void markExperimentFilesForTranslation(long actor, long experimentId, long chargedLabId) {
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        if (!ruleValidator.canTranslateExperimentFiles(actor, chargedLabId, experiment)) {
            throw new AccessDenied("User isn't permitted to translate this experiment");
        }
        prepareForTranslation(experiment);
        final List<RawFile> data = experiment.getRawFiles().getData();
        final Lab chargedLab = labRepository.findOne(chargedLabId);
        checkNotNull(chargedLab);
        for (ExperimentFileTemplate rawFile : data) {
            markExperimentFileForTranslation(actor, (AbstractFileMetaData) rawFile.getFileMetaData(), chargedLab);
            fileMetaDataRepository.save((ActiveFileMetaData) rawFile.getFileMetaData());
        }
        experimentRepository.save(experiment);
    }

    @Override
    public void markNotTranslatedFilesToTranslate(long actor, long experimentId, long chargedLab) {

        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        if (!ruleValidator.userHasEditPermissionsOnExperiment(actor, experimentId)
                || !ruleValidator.canTranslateExperimentFiles(actor, chargedLab, experiment)) {
            throw new AccessDenied("User isn't permitted to translate this experiment");
        }

        prepareForTranslation(experiment);

        final Lab lab = experiment.getLab() == null ? experiment.getBillLaboratory() : experiment.getLab();

        final List<? extends ExperimentFileTemplate<?, ?, ?>> data = experiment.getRawFiles().getData();

        for (ExperimentFileTemplate rawFile : data) {
            handleExperimentFileTranslation(actor, lab, rawFile);
        }
    }

    private void handleExperimentFileTranslation(long actor, Lab lab, ExperimentFileTemplate rawFile) {

        final AbstractFileMetaData fileMetaData = (AbstractFileMetaData) rawFile.getFileMetaData();
        final UserLabFileTranslationData dataForLab = findOrAppendUserLabTranslationData(actor, fileMetaData, lab);

        final ImmutableSet<TranslationStatus.Status> excludedStatuses = ImmutableSet.of(IN_PROGRESS, SUCCESS);

        if (!excludedStatuses.contains(dataForLab.getTranslationStatus().getStatus())) {

            handleFileForTranslation(fileMetaData, dataForLab);

            fileMetaDataRepository.save((ActiveFileMetaData) fileMetaData);
        }
    }

    private void handleFileForTranslation(final AbstractFileMetaData fileMetaData, UserLabFileTranslationData dataForLab) {
        final StorageData storageData = fileMetaData.getStorageData();

        switch (storageData.getStorageStatus()) {
            case ARCHIVED:
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        fileMovingManager.requestFileUnarchiving(fileMetaData.getId());
                    }
                });
            case ARCHIVING_REQUESTED:
            case UNARCHIVING_REQUESTED:
                setTranslationValues(dataForLab);
                dataForLab.getTranslationStatus().setToTranslationQueue(false);
                setToTranslate(dataForLab, false);
                dataForLab.setToTempFolder(true);
                break;
            case UNARCHIVED:
                setTranslationValues(dataForLab);
                setToTranslate(dataForLab, true);
                break;
        }
    }

    private void markExperimentFileForTranslation(long actor, AbstractFileMetaData fileMetaData, final Lab chargedLab) {

        final UserLabFileTranslationData userTranslationData = findOrAppendUserLabTranslationData(actor,
                fileMetaData, chargedLab);

        setTranslationValues(userTranslationData);
        setToTranslate(userTranslationData, true);

        for (MSFunctionItem functionToClear : userTranslationData.getFunctions()) {
            msFunctionItemRepository.delete(functionToClear);
        }
        userTranslationData.getFunctions().clear();
    }

    private UserLabFileTranslationData findOrAppendUserLabTranslationData(final long actor, final AbstractFileMetaData fileMetaData, final Lab lab) {

        return getFileTranslationDataForLab(fileMetaData, lab).or(createAndAppendUserLabTranslationData(actor, fileMetaData, lab));
    }

    private Supplier<UserLabFileTranslationData> createAndAppendUserLabTranslationData(final long actor, final AbstractFileMetaData fileMetaData, final Lab lab) {
        return new Supplier<UserLabFileTranslationData>() {
            @Override
            public UserLabFileTranslationData get() {

                final UserLabFileTranslationData translationData = new UserLabFileTranslationData(USER_FROM_ID.apply(actor), Collections.<MSFunctionItem>emptySet(), lab);
                fileMetaData.getUsersFunctions().add(translationData);
                return translationData;
            }
        };
    }

    private Optional<UserLabFileTranslationData> getFileTranslationDataForLab(AbstractFileMetaData fileMetaData, final Lab lab) {

        return tryFind(fileMetaData.getUsersFunctions(), new Predicate<UserLabFileTranslationData>() {
            @Override
            public boolean apply(UserLabFileTranslationData input) {

                return input.getLab().equals(lab);

            }
        });
    }

    private void setTranslationValues(UserLabFileTranslationData userTranslationData) {
        userTranslationData.getTranslationStatus().setLastTranslationAttempt(new Date());
        userTranslationData.getTranslationStatus().setToTranslationQueue(true);
        userTranslationData.getTranslationStatus().setTranslationError(null);
        userTranslationData.getTranslationStatus().setLastTranslationDuration(null);
        userTranslationData.getTranslationStatus().setLastTranslationEndDate(null);
        userTranslationData.getTranslationStatus().setTranslationSubmitted(true);
        userTranslationData.getTranslationStatus().setStatus(IN_PROGRESS);
    }

    private UserLabFileTranslationData findOrAppendUserTranslationData(final long actor, final AbstractFileMetaData file) {

        return getFileLabTranslationData(file)
                .or(createAndAppendUserLabTranslationData(actor, file, file.getInstrument().getLab()));
    }

    private Optional<UserLabFileTranslationData> getFileLabTranslationData(final AbstractFileMetaData file) {

        return getFileTranslationDataForLab(file, file.getInstrument().getLab());
    }

    @Override
    public void markFileForTranslation(long actor, long lab, long fileId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableSet<Long> markFilesForTranslation(final long actor, long lab, @NotNull Set<Long> files) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void runPreCacheViewers(long actor, long experimentId) {
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        precacheFiles(experiment);
    }

    private void prepareForTranslation(final ActiveExperiment experiment) {
        experiment.setLastTranslationAttempt(current.get());
        experiment.setTranslated(true);
        experiment.setTranslationError(null);
    }

    @Override
    public void retranslateAllExperiments(long actor) {
        if (!ruleValidator.userHasPermissionToRetranslateAllExperiments(actor)) {
            throw new AccessDenied("User isn't permitted to retranslate all experiments");
        }
        LOG.warn("Skipping retranslating all experiments due to per-file translation scheme migration");
    }

    @Override
    public void retranslateExperiments(long actor, List<Long> experiments) {
        if (!ruleValidator.userHasPermissionToRetranslateAllExperiments(actor)) {
            throw new AccessDenied("User isn't permitted to retranslate selected experiments");
        }
        if (experiments.isEmpty()) {
            throw new IllegalArgumentException("No experiments to retranslate");
        }
        LOG.warn("Skipping retranslating specified experiments due to per-file translation scheme migration");
    }

    @Override
    public void retranslateExperimentsFiles(long actor, List<Long> experiments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void retranslateFiles(long actor, List<Long> files, boolean metadataOnly) {
        throw new UnsupportedOperationException();
    }


    private Function<AbstractFileMetaData, AbstractFileMetaData> prepareForDirectTranslationFn(final long actor) {

        return new Function<AbstractFileMetaData, AbstractFileMetaData>() {
            @Override
            public AbstractFileMetaData apply(AbstractFileMetaData file) {

                final UserLabFileTranslationData userTranslationData = findOrAppendUserTranslationData(actor, file);

                setTranslationValues(userTranslationData);
                setToTranslate(userTranslationData, false);

                for (MSFunctionItem functionToClear : userTranslationData.getFunctions()) {
                    msFunctionItemRepository.delete(functionToClear);
                }
                userTranslationData.getFunctions().clear();
                return file;
            }
        };
    }

    private void setToTranslate(UserLabFileTranslationData userTranslationData, boolean value) {
        userTranslationData.getTranslationStatus().setToTranslate(value);
    }

    @Override
    public void reTranslateAllNotTranslatedFilesOfExperiments(long actor, boolean metadataOnly) {
        throw new UnsupportedOperationException();
    }



    @Override
    public void translateMarkedFiles() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setBlogEnabled(long actor, long project, boolean blogEnabled) {
        ActiveProject entity = projectRepository.findOne(project);
        if (!ruleValidator.isProjectOwner(actor, project)
                && !(entity.getLab() != null && entity.getLab().getHead().getId().equals(actor)))
            throw new AccessDenied("Couldn't update");
        entity.setBlogEnabled(blogEnabled);
        projectRepository.save(entity);
    }

    private void precacheFiles(ActiveExperiment experiment) {
        LOG.warn("Skipping precaching files for experiment due to per-file translation scheme migration");
    }

    //TODO Refactor to avoid code duplicates with ProjectManagementImpl
    public ActiveProject copyProjectFor(User newOwner, AbstractProject project, String copyName) {
        List<Attachment<User>> copiedAttach = new ArrayList<>();
        for (Attachment<User> attachment : project.getAttachments()) {
            final Attachment copy = attachmentRepository.findOne(attachmentManagement.copyAttachment(attachment.getId(),
                    newOwner.getId(), true));
            copiedAttach.add(copy);
        }
        ActiveProject copy = new ActiveProject(newOwner, null, copyName, project.getAreaOfResearch(),
                project.getDescription());
//        copy.setSharing(project.getSharing());
        copy.getAttachments().addAll(copiedAttach);
        return copy;
    }

    //TODO Refactor to avoid code duplicates with ProjectManagementImpl
    private void copyRawFiles(AbstractExperiment from, final AbstractExperiment to, final Function<AbstractFileMetaData, AbstractFileMetaData> copyMetaFn) {

        final Map<String, ExperimentPreparedSample> nameToNewPrepSample = copySamplesFromExperiment(from);
        final Collection<RawFile> copiedFiles = transform(from.rawFiles.getData(), new Function<RawFile, RawFile>() {
            @Override
            public RawFile apply(RawFile from) {
                final RawFile copyFile = new RawFile(copyMetaFn.apply(from.getFileMetaData()), from.getFractionNumber(), nameToNewPrepSample.get(from.getPreparedSample().getName()));
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
//        saveExperiment(to);
    }

    private Map<String, ExperimentPreparedSample> copySamplesFromExperiment(AbstractExperiment from) {
        final Map<String, ExperimentPreparedSample> nameToNewPrepSample = newHashMap();
        for (RawFile rawFile : from.rawFiles.getData()) {
            final ExperimentPreparedSample fromPreparedSample = rawFile.getPreparedSample();
            if (!nameToNewPrepSample.containsKey(fromPreparedSample.getName())) {
                final Set<PrepToExperimentSample> toPrepToExSample = newHashSet();
                final ExperimentPreparedSample preparedSampleToPersist = new ExperimentPreparedSample(fromPreparedSample.getName(), toPrepToExSample);
                nameToNewPrepSample.put(fromPreparedSample.getName(), preparedSampleRepository.save(preparedSampleToPersist));
                for (PrepToExperimentSample fromPrepToExSample : fromPreparedSample.getSamples()) {
                    final ExperimentSample fromSample = fromPrepToExSample.getExperimentSample();
                    final ExperimentSample toSample = experimentSampleRepository.save(new ExperimentSample(fromSample.getName(), newArrayList(fromSample.getFactorValues())));
                    final PrepToExperimentSample toPreparedSample = prepToExperimentSampleRepository.save(new PrepToExperimentSample(preparedSampleToPersist, toSample, fromPrepToExSample.getType()));
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
                experiment.getNgsRelatedData()
        );

        copy.setBillLaboratory(newBillLab);
        copy.setTranslationError(experiment.getTranslationError());
        copy.setLastTranslationAttempt(experiment.getLastTranslationAttempt());
        copy.setTranslated(experiment.isTranslated());
        List<Attachment<User>> copiedAttach = new ArrayList<>();
        for (Attachment<User> attachment : experiment.attachments) {
            final Attachment<User> att = attachmentRepository.findOne(attachmentManagement.copyAttachment(attachment.getId(),
                    owner.getId(), false));
            copiedAttach.add(att);
        }
        copy.attachments = copiedAttach;
        return copy;
    }

    //TODO Refactor to avoid code duplicates with ProjectManagementImpl
    public void copyMSData(final AbstractFileMetaData from, final AbstractFileMetaData target, Lab newBillLab) {

        final Set<UserLabFileTranslationData> usersFunctions = from.getUsersFunctions();

        for (UserLabFileTranslationData usersFunction : usersFunctions) {
            final Set<MSFunctionItem> functions = usersFunction.getFunctions();
            final ImmutableSet.Builder<MSFunctionItem> builder = ImmutableSet.builder();
            for (MSFunctionItem function : functions) {
                final MSFunctionItem originalFunction = msFunctionItemRepository.findOne(function.getId());
                MSFunctionItem copyFunction = originalFunction.copy();
                copyFunction = msFunctionItemRepository.save(copyFunction);
                builder.add(copyFunction);
            }
            target.getUsersFunctions().add(new UserLabFileTranslationData(usersFunction.getUser(), builder.build(), newBillLab));
        }

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
