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
package com.infoclinika.mssharing.model.internal;

import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.api.MSFunctionType;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.*;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.impl.DefaultRuleValidator;
import com.infoclinika.mssharing.platform.model.impl.ValidatorPredicates;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.repository.FileProjectUsage;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRepositoryTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.*;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.infoclinika.mssharing.model.features.ApplicationFeature.*;
import static com.infoclinika.mssharing.model.internal.entity.restorable.StorageData.Status.*;
import static com.infoclinika.mssharing.model.internal.read.Transformers.RAW_META_DATA_TRANSFORMER;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;

/**
 * @author Stanislav Kurilin
 */
@Component("validator")
@Transactional(readOnly = true)
public class RuleValidatorImpl extends DefaultRuleValidator<ActiveExperiment, ActiveFileMetaData, ActiveProject,
    Instrument, Lab> implements RuleValidator {


    private static final Logger LOGGER = LoggerFactory.getLogger(RuleValidatorImpl.class);

    public static final java.util.function.Predicate<ActiveFileMetaData> IS_UPLOAD_COMPLETE =
        input -> input.getContentId() != null;
    private static final java.util.function.Predicate<ActiveFileMetaData> IS_ON_GLACIER =
        input -> input.getArchiveId() != null;
    private static final Function<ExperimentFileTemplate, AbstractFileMetaData> META_DATA_FROM_RAW_FILE =
        input -> (AbstractFileMetaData) getMetaData(input);
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private DeletedExperimentRepository deletedExperimentRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private DeletedFileMetaDataRepository deletedFileMetaDataRepository;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private LabRepository labRepository;
    @Inject
    private BillingFeaturesHelper billingFeaturesHelper;
    @Inject
    private AnnotationAttachmentRepository annotationAttachmentRepository;
    @Inject
    private UserLabMembershipRepositoryTemplate userLabMembershipRepository;
    @Inject
    private FeaturesHelper featuresHelper;


    /**
     * User can create experiment run in experiment only if this user is it's creator or labhead
     */
    @Override
    public boolean userHasPermissionToCreateSearch(long creator, long experiment) {
        final ActiveExperiment experimentEntity = checkPresence(experimentRepository.findOne(experiment));

        return experimentEntity.getCreator().getId().equals(creator) ||
            isMemberOfExperimentLab(experimentEntity, creator) ||
            isExperimentLabHead(experimentEntity, creator);
    }

    @Override
    public Predicate<ActiveFileMetaData> userHasReadPermissionsOnFilePredicate(long userId) {
        return validatorPredicates.userHasReadPermissionsOnFile(userId);
    }

    private static AbstractFileMetaData getMetaData(ExperimentFileTemplate file) {
        return (AbstractFileMetaData) file.getFileMetaData();
    }

    @Override
    public AccessLevel getAccessLevel(AbstractFileMetaData input) {

        final List<FileProjectUsage> projects = fileMetaDataRepository.findFileProjectUsage(input.getId());

        if (includedToProjectsOfType(projects, Sharing.Type.PUBLIC)) {
            return AccessLevel.PUBLIC;
        }
        if (includedToProjectsOfType(projects, Sharing.Type.SHARED)) {
            return AccessLevel.SHARED;
        }
        return AccessLevel.PRIVATE;
    }

    private boolean includedToProjectsOfType(List<FileProjectUsage> projects, final Sharing.Type type) {
        return Iterables.tryFind(projects, input -> input.sharingType.equals(type)).isPresent();
    }

    @Override
    public Predicate<ActiveFileMetaData> filesFromMatchedProjectsPredicate(
        Predicate<AbstractProject> projectPredicate
    ) {
        return validatorPredicates.filesFromMatchedProjects(projectPredicate);
    }


    @Override
    public boolean canAccessExistingInstrument(long actor, String serialNumber) {
        final Lab ownerLab = instrumentRepository.labOfInstrument(serialNumber);
        final Set<Lab> actorLabs = super.findLabs(actor);
        return actorLabs.contains(ownerLab);
    }

    @Override
    public boolean canReadLabBilling(long actor, long lab) {
        final List<Lab> actorLabs = labRepository.findForUser(actor);
        final boolean userBelongsToLab = actorLabs.stream().filter(l -> l.getId().equals(lab)).findFirst().isPresent();
        return isAdmin(actor) || userBelongsToLab; /*|| isLabHead(actor, lab)*/
    }

    @Override
    public boolean isLabHead(long actor, long lab) {
        final Lab labEntity = labRepository.findOne(lab);
        return labEntity.getHead().getId().equals(actor);
    }

    @Override
    public boolean usersInSameLab(long user1, long user2) {
        if (user1 == user2) {
            return true;
        }

        Set<UserLabMembership<User, Lab>> ul1 = userLabMembershipRepository.findByUser_id(user1);
        Set<UserLabMembership<User, Lab>> ul2 = userLabMembershipRepository.findByUser_id(user2);

        return !Collections.disjoint(
            ul1.stream()
                .map(v -> v.getLab().getId())
                .collect(Collectors.toCollection(() -> new ArrayList<>(ul1.size()))),
            ul2.stream()
                .map(v -> v.getLab().getId())
                .collect(Collectors.toCollection(() -> new ArrayList<>(ul2.size())))
        );
    }

    @Override
    public boolean canUserManageLabAccount(long actor, long lab) {
        return isAdmin(actor) || isLabHead(actor, lab);
    }

    @Override
    public boolean isProjectOwner(long actor, long projectId) {
        return ValidatorPredicates.isOwnerInProject(factories.userFromId.apply(actor))
            .apply(projectRepository.findOne(projectId));
    }

    @Override
    public boolean canReadUsersInLab(long labHead, long labId) {
        final Lab lab = checkPresence(labRepository.findOne(labId));
        return lab.getHead().getId().equals(labHead);
    }


    @Override
    public boolean canRestoreProject(long actor, DeletedProject project) {
        final UserTemplate user = factories.userFromId.apply(actor);
        return (project.getCreator().getId().equals(actor) || ValidatorPredicates.isProjectLabHead(user).apply(project))
            && !projectHasDuplicateNames(actor, project);
    }

    @Override
    public boolean projectHasDuplicateNames(long owner, DeletedProject project) {
        if (projectRepository.findByName(owner, project.getName()).size() > 0) {
            return true;
        } else {
            for (DeletedExperiment e : deletedExperimentRepository.findByProject(project.getId())) {
                if (experimentHasDuplicateNames(e.getCreator().getId(), e)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canRestoreExperiment(long actor, DeletedExperiment experiment) {
        return (experiment.getCreator().getId().equals(actor) || isExperimentLabHead(experiment, actor))
            && !experimentHasDuplicateNames(actor, experiment);
    }

    @Override
    public boolean experimentHasDuplicateNames(long owner, DeletedExperiment experiment) {
        if (experimentRepository.findByName(owner, experiment.getName()).size() > 0) {
            return true;
        } else {
            for (ExperimentFileTemplate rawFile : experiment.getRawFiles().getData()) {
                DeletedFileMetaData deletedFile = deletedFileMetaDataRepository.findOne(getMetaData(rawFile).getId());
                if (deletedFile != null && fileHasDuplicateName(deletedFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canRestoreFile(long actor, DeletedFileMetaData file) {
        return (file.getOwner().getId().equals(actor) || file.getInstrument().getLab().getHead().getId().equals(actor))
            && !fileHasDuplicateName(file);
    }

    @Override
    public boolean fileHasDuplicateName(DeletedFileMetaData file) {
        return fileMetaDataRepository.findByInstrumentWithName(file.getInstrument(), file.getName()).size() > 0;
    }

    @Override
    public Predicate<ActiveExperiment> isUserCanReadExperimentPredicate(long actor) {
        return validatorPredicates.isUserCanReadExperiment(actor);
    }

    @Override
    public boolean isUserCanReadExperiment(long actor, long experiment) {
        return super.isUserCanReadExperiment(actor, experiment);
    }

    @Override
    public Predicate<Instrument> isUserCanReadInstrumentPredicate(long actor) {
        return validatorPredicates.isUserCanReadInstrument(actor);
    }

    @Override
    public boolean hasAdminRights(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canManageNews(long actor) {
        return isAdmin(actor);
    }


    @Override
    public boolean isUploadAppConfigurationOwner(long actor, UploadAppConfiguration configuration) {
        final User user = checkNotNull(configuration).getUser();
        return user != null && user.getId() == actor;
    }

    @Override
    public boolean isExperimentReadyToDownload(ActiveExperiment activeExperiment) {
        return activeExperiment.getRawFiles().getData()
            .stream()
            .map(RAW_META_DATA_TRANSFORMER::apply)
            .allMatch(rawFileHasStorageStatus(UNARCHIVED)::apply);
    }

    private Optional<Lab> getExperimentLab(ActiveExperiment experiment) {
        return Optional.ofNullable(experiment.getLab() != null ? experiment.getLab() : experiment.getBillLaboratory());
    }

    private Predicate<AbstractFileMetaData> fileHasStorageStatus(final StorageData.Status status) {
        return input -> input.getStorageData().getStorageStatus().equals(status);
    }

    private Predicate<AbstractFileMetaData> isLabHeadOfFileInstrument(final long actor) {
        return userIsLabHeadOfFile(actor);
    }

    @Override
    public boolean canArchiveFile(long actor, long file) {

        final ActiveFileMetaData fileMetaData = checkNotNull(fileMetaDataRepository.findOne(file));

        final Long lab = fileMetaData.getInstrument().getLab().getId();
        return isFeatureEnabledForLab(GLACIER, lab)
            && isBillingFeatureEnabledForLab(BillingFeature.ANALYSE_STORAGE, lab)
            && isBillingFeatureEnabledForLab(BillingFeature.ARCHIVE_STORAGE, lab)
            && not(fileHasStorageStatus(ARCHIVED)).apply(fileMetaData)
            && (validatorPredicates.userIsOwnerOfFile(actor).apply(fileMetaData) ||
            isLabHeadOfFileInstrument(actor).apply(fileMetaData));
    }

    @Override
    public boolean canUnarchiveFile(long actor, long file) {
        final ActiveFileMetaData fileMetaData = checkNotNull(fileMetaDataRepository.findOne(file));
        final Long lab = fileMetaData.getInstrument().getLab().getId();
        return isFeatureEnabledForLab(GLACIER, lab)
            && isBillingFeatureEnabledForLab(BillingFeature.ANALYSE_STORAGE, lab)
            && isBillingFeatureEnabledForLab(BillingFeature.ARCHIVE_STORAGE, lab)
            && not(fileHasStorageStatus(UNARCHIVED)).apply(fileMetaData)
            && (validatorPredicates.userIsOwnerOfFile(actor).apply(fileMetaData) ||
            isLabHeadOfFileInstrument(actor).apply(fileMetaData));
    }

    @Override
    public boolean canArchiveExperiment(final long actor, ActiveExperiment experiment) {

        final Optional<Lab> lab = getExperimentLab(experiment);

        if (!lab.isPresent()) {
            return false;
        }

        final Long labId = lab.get().getId();
        final Iterable<AbstractFileMetaData> rawFiles =
            transform(experiment.getRawFiles().getData(), RAW_META_DATA_TRANSFORMER);

        return (isExperimentCreator(experiment, actor) || isExperimentLabHead(experiment, actor))
            && isFeatureEnabledForLab(GLACIER, labId)
            && isBillingFeatureEnabledForLab(BillingFeature.ANALYSE_STORAGE, labId)
            && isBillingFeatureEnabledForLab(BillingFeature.ARCHIVE_STORAGE, labId)
            && anyFilesHaveStatus(rawFiles, UNARCHIVED)
            && any(rawFiles, or(validatorPredicates.userIsOwnerOfFile(actor), isLabHeadOfFileInstrument(actor)));
    }


    @Override
    public boolean canUnarchiveExperiment(long actor, ActiveExperiment experiment) {

        final Optional<Lab> lab = getExperimentLab(experiment);

        if (!lab.isPresent()) {
            return false;
        }

        final Long labId = lab.get().getId();
        final Iterable<AbstractFileMetaData> rawFiles =
            transform(experiment.getRawFiles().getData(), RAW_META_DATA_TRANSFORMER);

        return (isExperimentCreator(experiment, actor) || isExperimentLabHead(experiment, actor))
            && isFeatureEnabledForLab(GLACIER, labId)
            && isBillingFeatureEnabledForLab(BillingFeature.ANALYSE_STORAGE, labId)
            && isBillingFeatureEnabledForLab(BillingFeature.ARCHIVE_STORAGE, labId)
            && anyFilesHaveStatus(rawFiles, ARCHIVED, UNARCHIVING_REQUESTED, ARCHIVING_REQUESTED)
            && any(rawFiles, or(validatorPredicates.userIsOwnerOfFile(actor), isLabHeadOfFileInstrument(actor)));
    }

    @Override
    public boolean isBillingEnabledForLab(long lab) {
        return isFeatureEnabledForLab(BILLING, lab);
    }

    @Override
    public boolean canRemoveFile(long actor, long file) {

        final boolean removeFile = super.canRemoveFile(actor, file);

        if (!removeFile) {
            return false;
        }

        final ActiveFileMetaData entity = fileMetaDataRepository.findOne(file);

        return RuleValidatorImpl.IS_UPLOAD_COMPLETE.test(entity) || RuleValidatorImpl.IS_ON_GLACIER.test(entity);
    }

    private boolean anyFilesHaveStatus(Iterable<AbstractFileMetaData> files, final StorageData.Status... status) {
        return any(files, or(statusPredicateTransformer(status)));
    }

    private Iterable<Predicate<AbstractFileMetaData>> statusPredicateTransformer(StorageData.Status... status) {
        return newArrayList(status).stream().map(this::rawFileHasStorageStatus).collect(Collectors.toList());
    }

    private Predicate<AbstractFileMetaData> rawFileHasStorageStatus(final StorageData.Status status) {
        return input -> input.getStorageData().getStorageStatus().equals(status);
    }

    private boolean isExperimentCreator(ActiveExperiment experiment, long actor) {
        return experiment.getCreator().getId().equals(actor);
    }

    @Override
    public boolean canModifyAnnotationAttachment(long actor, long annotationAttachment) {
        final AnnotationAttachment attachment = annotationAttachmentRepository.findOne(annotationAttachment);
        return attachment.getOwner().getId().equals(actor);
    }

    private Predicate<AbstractFileMetaData> userIsLabHeadOfFile(final long actor) {
        return input -> input.getInstrument().getLab().getHead().getId().equals(actor);
    }

    @Override
    public Set<Long> getProjectsWithReadAccess(final long actor) {
        return userRepository.findOne(actor)
            .getProjectsWithReadAccess()
            .stream()
            .map(EntityUtil.ENTITY_TO_ID::apply)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean canCreatePostProcessingPipeline(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canImportMicroArrays(long actor, long lab) {
        return isFeatureEnabledForLab(MICROARRAYS, lab);
    }

    @Override
    public boolean isLabMember(long actor, long lab) {
        final Set<UserLabMembership<? extends UserTemplate<?>, Lab>> labMemberships =
            userRepository.findOne(actor).getLabMemberships();
        final Optional<UserLabMembership<? extends UserTemplate<?>, Lab>> labMembership =
            labMemberships.stream().filter(membership -> membership.getLab().getId().equals(lab)).findFirst();
        return labMembership.isPresent();
    }

    @Override
    public boolean userCanUploadFileOfInstrument(long userId, long instrumentId) {
        if (!canFileBeUploadedByInstrument(false, instrumentId)) {
            return false;
        }

        final Instrument instrument = instrumentRepository.findOne(instrumentId);
        final UserLabMembership labMembership =
            userLabMembershipRepository.findByLabAndUser(instrument.getLab().getId(), userId);
        return labMembership != null;
    }

    private boolean isFeatureEnabledForLab(ApplicationFeature feature, long labId) {
        return featuresHelper.isEnabledForLab(feature, labId);
    }

    private boolean isBillingFeatureEnabledForLab(BillingFeature feature, long labId) {
        return billingFeaturesHelper.isFeatureEnabled(labId, feature);
    }

    private boolean isMemberOfExperimentLab(AbstractExperiment experiment, long user) {
        if (experiment.getLab() == null) {
            return false;
        }

        return experiment.getLab()
            .getLabMemberships()
            .stream()
            .anyMatch(membership -> membership.getUser().getId().equals(user));
    }

}
