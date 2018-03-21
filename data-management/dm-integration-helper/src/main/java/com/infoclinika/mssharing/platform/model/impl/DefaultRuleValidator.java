package com.infoclinika.mssharing.platform.model.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.all;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;

/**
 * @author : Alexander Serebriyan, Herman Zamula, Andrii Loboda
 */
@Component
public class DefaultRuleValidator<EXPERIMENT extends ExperimentTemplate, FILE extends FileMetaDataTemplate,
        PROJECT extends ProjectTemplate, INSTRUMENT extends InstrumentTemplate, LAB extends LabTemplate>
        implements RuleValidator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRuleValidator.class);
    @Inject
    protected ValidatorPredicates validatorPredicates;
    @Inject
    protected EntityFactories factories;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private LabRepositoryTemplate<LAB> labRepository;
    @Inject
    private InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;
    @Inject
    private ProjectRepositoryTemplate<PROJECT> projectRepository;
    @Inject
    private FileRepositoryTemplate<FILE> fileMetaDataRepository;
    @Inject
    private GroupRepositoryTemplate<GroupTemplate> groupRepository;
    @Inject
    private AttachmentRepositoryTemplate<Attachment> attachmentRepository;
    @Inject
    private ExperimentRepositoryTemplate<EXPERIMENT> experimentRepository;
    @Inject
    private InstrumentModelRepositoryTemplate<InstrumentModel> instrumentModelRepository;
    @Inject
    private SpeciesRepositoryTemplate<Species> speciesRepository;
    @Inject
    private InstrumentCreationRequestRepositoryTemplate<InstrumentCreationRequestTemplate> instrumentCreationRequestRepository;
    @Inject
    private VendorRepositoryTemplate vendorRepository;


    //========== labs

    @Override
    public boolean canReadLabs(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canCreateLabs(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canEditLabDetails(long actor, long labId) {
        return isAdmin(actor);
    }

    @Override
    public boolean canProcessLabRequests(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canEditLabCreationRequests(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canModifyLabMembershipRequests(long actor, long targetLabId) {
        LabTemplate targetLab = labRepository.findOne(targetLabId);
        return targetLab.getHead().getId() == actor;
    }


    //=========== users

    @Override
    public boolean canBeCreatedOrUpdated(UserManagementTemplate.PersonInfo user, Set<Long> labIds) {
        if (labIds.isEmpty()) {
            return true;
        }
        final List<LabTemplate> labs = instrumentRepository.labsToWithUserPending(user.email);
        switch (labs.size()) {
            case 0:
                return true;
            case 1:
                final Long id = labs.iterator().next().getId();
                for (Long labId : labIds) {
                    if (id.equals(labId)) {
                        return true;
                    }
                }
                return false;
            default: {
                LOG.warn("Actually we can not create user with this email ( {} )at all", user.email);
                return false;
            }
        }
    }

    @Override
    public boolean userHasPermissionsToRemoveUserFromLab(long labHead, long labId, long userId) {
        if (labHead == userId) return false;
        final LabTemplate lab = checkPresence(labRepository.findOne(labId));
        if (!lab.getHead().getId().equals(labHead)) return false;
        final UserTemplate user = checkPresence(userRepository.findOne(userId));
        return user.getLabs().contains(lab);
    }


    // ============= projects

    /**
     * User can create experiments on his project or in projects shared with him
     */
    @Override
    public boolean isUserCanCreateExperimentsInProject(long userId, long projectId) {
        return projectRepository.isProjectAllowedForWriting(userId, projectId);
    }

    @Override
    public boolean canUserCreateProjectWithTitle(long actor, String projectName) {
        if (!StringUtils.isNotEmpty(projectName.trim())) {
            return false;
        }
        final UserTemplate user = checkNotNull(userRepository.findOne(actor));
        return projectRepository.findOneByName(user.getId(), projectName) == null;
    }

    @Override
    public boolean canUserUpdateProjectWithTitle(long actor, long project, String projectName) {
        final UserTemplate user = checkPresence(userRepository.findOne(actor));
        if (!StringUtils.isNotEmpty(projectName.trim())) {
            return false;
        }
        if (checkPresence(projectRepository.findOne(project)).getName().equals(projectName)) {
            return true;
        }
        return projectRepository.findOneByName(user.getId(), projectName) == null;
    }

    @Override
    public boolean canRemoveProject(long actor, long projectId) {
        final ProjectTemplate project = checkPresence(projectRepository.findOne(projectId));
        return (project.getCreator().getId().equals(actor) || ValidatorPredicates.isProjectLabHead(factories.userFromId.apply(actor)).apply(project))
                && project.getSharing().getType() == Sharing.Type.PRIVATE;
    }


    protected boolean isAdmin(long actor) {
        return findUser(actor).isAdmin();
    }

    protected <USER extends UserTemplate> USER findUser(long actor) {
        //noinspection unchecked
        return (USER) checkPresence(userRepository.findOne(actor));
    }

    private ProjectTemplate findProject(long projectId) {
        return checkPresence(projectRepository.findOne(projectId));
    }


    // =========== files

    @Override
    public boolean userHasReadPermissionsOnFile(long userId, long fileId) {
        final FileMetaDataTemplate file = fileMetaDataRepository.findOne(fileId);
        return validatorPredicates.userHasReadPermissionsOnFile(userId).apply(file);
    }


    @Override
    public boolean isUserCanReadInstrument(long actor, long instrument) {

        return validatorPredicates
                .isUserCanReadInstrument(actor)
                .apply(instrumentRepository.findOne(instrument));
    }

    @Override
    public boolean canReadGroupDetails(long actor, long group) {
        return groupRepository.findOne(group).getOwner().getId().equals(actor);
    }

    @Override
    public boolean userCanSeeLabRequests(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canModifyAttachment(long actor, long attachmentId) {
        final Attachment attachment = findAttachment(attachmentId);
        return attachment.getOwner().getId().equals(actor);
    }

    private Attachment findAttachment(long attachmentId) {
        return checkPresence(attachmentRepository.findOne(attachmentId));
    }

    @Override
    public boolean canUserCreateInstrument(long user) {
        return !userRepository.findOne(user).getLabs().isEmpty();
    }

    @Override
    public boolean canUserCreateInstrumentInLab(long user, long lab) {
        return labRepository.findOne(lab).getHead().getId().equals(user);
    }

    @Override
    public boolean canInstrumentBeCreated(long labId, final String instrumentName, String serialNumber) {
        final LabTemplate lab = labRepository.findOne(labId);
        return lab != null && instrumentRepository.findBySerialNumber(serialNumber) == null &&
                instrumentRepository.findOneByName(instrumentName, labId) == null;
    }

    @Override
    public boolean canInstrumentBeUpdated(long instrumentId, String newName, String serialNumber) {
        final InstrumentTemplate instrument = instrumentRepository.findOne(instrumentId);

        return instrument.getSerialNumber().equalsIgnoreCase(serialNumber) &&
                instrument.getName().equalsIgnoreCase(newName)
                ||
                instrumentRepository.findBySerialNumber(serialNumber) == null &&
                        instrument.getName().equalsIgnoreCase(newName)
                ||
                instrumentRepository.findOneByName(newName, instrument.getLab().getId()) == null;

    }

    @Override
    public boolean canUserEditInstrument(long actor, long instrument) {
        final InstrumentTemplate instrument1 = checkPresence(instrumentRepository.findOne(instrument));
        //noinspection unchecked
        return instrument1.isOperator(factories.userFromId.apply(actor));
    }

    @Override
    public boolean canRemoveInstrument(long actor, long instrument) {
        final InstrumentTemplate entity = checkPresence(instrumentRepository.findOne(instrument));
        final UserTemplate head = entity.getLab().getHead();
        if (!entity.getCreator().getId().equals(actor) && !head.getId().equals(actor)) {
            return false;
        }
        return fileMetaDataRepository.countByInstrument(entity.getId()) == 0;
    }

    @Override
    public boolean canShareInstrument(long actor, long operator) {
        return inSameLab(actor, operator);

    }

    @Override
    public boolean canUserManageInstrumentModels(long actor) {
        return isAdmin(actor);
    }

    @Override
    public boolean canInstrumentModelBeCreatedWithName(String modelName, String vendorName) {
        return instrumentModelRepository.findByNameAndVendorName(modelName, vendorName) == null;
    }

    @Override
    public boolean canInstrumentModelBeUpdatedWithName(long modelId, String newModelName, String vendorName) {
        final InstrumentModel instrumentModel = instrumentModelRepository.findByNameAndVendorName(newModelName, vendorName);
        return instrumentModel == null || instrumentModel.getId().equals(modelId);
    }

    @Override
    public boolean canInstrumentModelBeDeleted(long modelId) {
        return instrumentRepository.countByInstrumentModel(modelId) == 0;
    }

    @Override
    public boolean canUserReadInstrumentModels(long actor) {
        return userRepository.exists(actor);
    }

    private boolean inSameLab(long actor, long operator) {
        final Set<LabTemplate> actorLabs = findLabs(actor);
        final Set<LabTemplate> operatorLabs = findLabs(operator);
        for (LabTemplate lab : actorLabs) {
            if (operatorLabs.contains(lab)) {
                return true;
            }
        }
        return false;
    }

    protected <LAB extends LabTemplate> Set<LAB> findLabs(long user) {
        //noinspection unchecked
        return checkPresence(userRepository.findOne(user), "unknown user").getLabs();
    }

    @Override
    public boolean canEditOperatorsList(long user, long instrument) {
        //noinspection unchecked
        return checkPresence(instrumentRepository.findOne(instrument), "Instrument doesn't exist")
                .isOperator(factories.userFromId.apply(user));
    }

    @Override
    public boolean isUserCanReadExperiment(long actor, long experiment) {
        return validatorPredicates.isUserCanReadExperiment(actor).apply(experimentRepository.findOne(experiment));
    }

    protected boolean isExperimentLabHead(ExperimentTemplate input, long actor) {
        return input.getLab() == null ? input.getCreator().getId().equals(actor) : input.getLab().getHead().getId().equals(actor);
    }

    @Override
    public boolean canUserCreateExperimentWithTitle(long actor, String experimentName) {
        final UserTemplate user = checkPresence(userRepository.findOne(actor));
        return experimentName.trim().length() != 0 && experimentRepository.findOneByName(user.getId(), experimentName) == null;
    }

    @Override
    public boolean canSaveExperimentWithFiles(List<Long> fileIds) {
        final List<FILE> files = fileMetaDataRepository.findAllByIds(fileIds);

        return all(files, not(validatorPredicates.isFileInvalid()));
    }

    @Override
    public boolean canSaveExperimentWithModel(long instrumentModel, Set<Long> files) {
        final List<FILE> dataList = fileMetaDataRepository.findAllByIds(files);
        final InstrumentModel model = checkNotNull(instrumentModelRepository.findOne(instrumentModel), "Instrument model not found.");
        return all(dataList, validatorPredicates.hasSameInstrumentModel(model));
    }

    @Override
    public boolean canSaveExperimentWithSpecies(long species, Set<Long> files) {
        final List<FILE> dataList = fileMetaDataRepository.findAllByIds(files);
        final Species specie = checkNotNull(speciesRepository.findOne(species), "Species not found.");
        return specie.isUnspecified() || all(dataList, validatorPredicates.hasSameSpecies(specie));
    }


    /**
     * User can edit experiment only if this user is it's creator
     */
    @Override
    public boolean userHasEditPermissionsOnExperiment(long userId, long experimentId) {
        final ExperimentTemplate experiment = checkPresence(experimentRepository.findOne(experimentId));
        final UserTemplate user = factories.userFromId.apply(userId);
        return experiment.getCreator().equals(user) || isExperimentLabHead(experiment, userId);
    }

    /**
     * User can use files if he can use each of this files
     * User can use file if he is owner or this file is used in any project with user can access
     */
    @Override
    public boolean userHasReadPermissionsOnFiles(final long user, Iterable<Long> files) {
        FluentIterable<FileMetaDataTemplate> fileMetaDatas = from(files).transform(factories.fileFromId);
        return all(fileMetaDatas, validatorPredicates.userHasReadPermissionsOnFile(user));
    }

    @Override
    public boolean canUserUpdateExperimentWithTitle(long actor, long experiment, String experimentName) {
        final UserTemplate user = checkNotNull(userRepository.findOne(actor));
        if (checkPresence(experimentRepository.findOne(experiment)).getName().equals(experimentName)) {
            return true;
        }
        return experimentRepository.findOneByName(user.getId(), experimentName) == null;
    }

    @Override
    public boolean canReadInstrumentInboxDetails(long actor, long instrument) {
        final InstrumentTemplate template = instrumentRepository.findOne(instrument);
        //noinspection unchecked
        return template.isOperator(userRepository.findOne(actor));
    }

    @Override
    public boolean canRemoveGroup(long actor, long groupId) {
        final GroupTemplate group = groupRepository.findOne(groupId);
        final List<PROJECT> projectsSharedForGroup = projectRepository.findBySharedGroup(groupId);
        return checkNotNull(group).getOwner().getId().equals(actor) && projectsSharedForGroup.isEmpty();
    }

    @Override
    public boolean userHasReadPermissionsToEditGroup(long actor, long group) {
        return groupRepository.findOne(group).getOwner().equals(factories.userFromId.apply(actor));
    }


    @Override
    public boolean canRemoveExperiment(long actor, long experimentId) {
        final ExperimentTemplate experiment = checkPresence(experimentRepository.findOne(experimentId));
        return experiment.getCreator().getId().equals(actor) || isExperimentLabHead(experiment, actor);
    }

    /**
     * Check if user can retrieve information connected to project
     */
    @Override
    @Transactional
    public boolean hasReadAccessOnProject(long user, long projectId) {
        return validatorPredicates.isUserCanReadProject(factories.userFromId.apply(user)).apply(projectRepository.findOne(projectId));
    }

    @Override
    public boolean hasWriteAccessOnProject(long actor, long projectId) {

        final ProjectTemplate project = findProject(projectId);
        final UserTemplate userTemplate = factories.userFromId.apply(actor);

        return or(
                ValidatorPredicates.isOwnerInProject(userTemplate),
                ValidatorPredicates.isProjectLabHead(userTemplate)
        ).apply(project);
    }

    @Override
    public Set<Long> getProjectsWithReadAccess(long actor) {
        throw new UnsupportedOperationException("Method is not supported for default security implementation.");
    }

    public boolean canFileBeUploadedByInstrument(boolean archive, long instrument) {
        if (!archive) {
            return true;
        }
        final InstrumentTemplate instrumentOne = checkPresence(instrumentRepository.findOne(instrument));
        final InstrumentModel model = instrumentOne.getModel();
        return instrumentModelRepository.findWithFolderArchiveUploadSupport().contains(model) || model.isAdditionalFiles();
    }

    @Override
    public boolean canRemoveFile(final long actor, long file) {
        final FileMetaDataTemplate entity = checkPresence(fileMetaDataRepository.findOne(file));
        //noinspection unchecked
        boolean isOperator = FluentIterable.from(entity.getInstrument().getOperators()).anyMatch(new Predicate<UserTemplate>() {
            @Override
            public boolean apply(UserTemplate user) {
                return user.getId().equals(actor);
            }
        });
        final boolean usedInExperiments = experimentRepository.countByFile(file) > 0;
        return !usedInExperiments && !(!entity.getOwner().getId().equals(actor) && !isOperator);
    }

    @Override
    public boolean isUploadComplete(long fileId) {
        final FileMetaDataTemplate file = fileMetaDataRepository.findOne(fileId);
        return file != null && file.getContentId() != null;
    }

    @Override
    public boolean canReadInstrumentRequestDetails(long actor, long request) {

        final InstrumentCreationRequestTemplate creationRequest = checkNotNull(instrumentCreationRequestRepository.findOne(request),
                "Cannot find instrument creation request by id: " + request);

        return creationRequest.getLab().getHead().getId().equals(actor);
    }
}
