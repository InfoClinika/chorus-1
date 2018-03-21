package com.infoclinika.mssharing.platform.model;

import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.security.SecurityChecker;

import java.util.List;
import java.util.Set;

/**
 * @author : Alexander Serebriyan
 */
public interface RuleValidator extends SecurityChecker {

    // labs
    boolean canReadLabs(long actor);

    boolean canCreateLabs(long actor);

    boolean canEditLabDetails(long actor, long labId);

    boolean canProcessLabRequests(long actor);

    boolean canEditLabCreationRequests(long actor);

    boolean canModifyLabMembershipRequests(long actor, long targetLabId);

    boolean userCanSeeLabRequests(long actor);

    // users
    boolean canBeCreatedOrUpdated(UserManagementTemplate.PersonInfo user, Set<Long> labIds);

    boolean userHasPermissionsToRemoveUserFromLab(long labHead, long labId, long userId);

    //  projects
    boolean isUserCanCreateExperimentsInProject(long userId, long projectId);

    boolean canUserCreateProjectWithTitle(long actor, String projectName);

    boolean canUserUpdateProjectWithTitle(long actor, long project, String projectName);

    boolean canRemoveProject(long actor, long projectId);

    // files
    boolean userHasReadPermissionsOnFile(long userId, long fileId);

    boolean isUserCanReadInstrument(long actor, long instrument);

    boolean canReadGroupDetails(long actor, long group);

    boolean canRemoveFile(long actor, long file);

    // instruments
    boolean canUserCreateInstrument(long user);

    boolean canUserCreateInstrumentInLab(long user, long lab);

    boolean canInstrumentBeCreated(long labId, String instrumentName, String serialNumber);

    boolean canInstrumentBeUpdated(long instrumentId, String newName, String serialNumber);

    boolean canUserEditInstrument(long actor, long instrument);

    boolean canRemoveInstrument(long actor, long instrument);

    boolean canShareInstrument(long actor, long operator);

    boolean canEditOperatorsList(long user, long instrument);

    boolean canReadInstrumentInboxDetails(long actor, long instrument);

    boolean canUserManageInstrumentModels(long actor);

    boolean canInstrumentModelBeCreatedWithName(String modelName, String vendorName);

    boolean canInstrumentModelBeUpdatedWithName(long modelId, String newModelName, String vendorName);

    boolean canInstrumentModelBeDeleted(long modelId);

    boolean canUserReadInstrumentModels(long actor);

    // experiments
    boolean isUserCanReadExperiment(long actor, long experiment);

    boolean canRemoveExperiment(long actor, long experimentId);

    boolean canModifyAttachment(long actor, long attachmentId);

    boolean canUserCreateExperimentWithTitle(long actor, String experimentName);

    boolean canSaveExperimentWithFiles(List<Long> fileIds);

    boolean canSaveExperimentWithModel(long instrumentModel, Set<Long> files);

    boolean canSaveExperimentWithSpecies(long species, Set<Long> files);

    boolean userHasEditPermissionsOnExperiment(long userId, long experimentId);

    boolean userHasReadPermissionsOnFiles(long user, Iterable<Long> files);

    boolean canUserUpdateExperimentWithTitle(long actor, long experiment, String experimentName);

    // groups
    boolean canRemoveGroup(long actor, long groupId);

    boolean userHasReadPermissionsToEditGroup(long actor, long groupId);


    // file upload
    boolean isUploadComplete(long fileId);

    boolean canReadInstrumentRequestDetails(long actor, long request);

    boolean canFileBeUploadedByInstrument(boolean archive, long instrument);
}
