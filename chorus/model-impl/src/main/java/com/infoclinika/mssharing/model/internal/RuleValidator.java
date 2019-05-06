package com.infoclinika.mssharing.model.internal;

import com.google.common.base.Predicate;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.UploadAppConfiguration;
import com.infoclinika.mssharing.model.internal.entity.restorable.*;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Pavel Kaplin
 */
public interface RuleValidator extends com.infoclinika.mssharing.platform.model.RuleValidator {

    boolean userHasPermissionToCreateSearch(long creator, long experiment);

    Predicate<ActiveFileMetaData> userHasReadPermissionsOnFilePredicate(long userId);

    AccessLevel getAccessLevel(AbstractFileMetaData input);

    Predicate<ActiveFileMetaData> filesFromMatchedProjectsPredicate(Predicate<AbstractProject> projectPredicate);

    boolean canAccessExistingInstrument(long actor, String serialNumber);


    boolean canReadLabBilling(long actor, long lab);

    boolean isProjectOwner(long actor, long projectId);


    boolean canReadUsersInLab(long labHead, long labId);


    boolean canRestoreProject(long actor, DeletedProject projectId);

    boolean projectHasDuplicateNames(long owner, DeletedProject project);

    boolean canRestoreExperiment(long actor, DeletedExperiment experiment);

    boolean experimentHasDuplicateNames(long owner, DeletedExperiment experiment);

    boolean canRestoreFile(long actor, DeletedFileMetaData file);

    boolean fileHasDuplicateName(DeletedFileMetaData file);


    Predicate<ActiveExperiment> isUserCanReadExperimentPredicate(long actor);

    Predicate<Instrument> isUserCanReadInstrumentPredicate(long actor);

    boolean hasAdminRights(long actor);

    boolean canManageNews(long actor);


    boolean isUploadAppConfigurationOwner(long actor, UploadAppConfiguration configuration);

    boolean isExperimentReadyToDownload(ActiveExperiment experiment);

    boolean canArchiveFile(long actor, long file);

    boolean canUnarchiveFile(long actor, long file);

    boolean canArchiveExperiment(long actor, ActiveExperiment experiment);

    boolean canUnarchiveExperiment(long actor, ActiveExperiment experiment);

    boolean isBillingEnabledForLab(long lab);

    boolean canModifyAnnotationAttachment(long actor, long annotationAttachment);

    boolean isLabHead(long actor, long lab);

    boolean usersInSameLab(long user1, long user2);

    boolean canCreatePostProcessingPipeline(long actor);
    
    boolean canUserManageLabAccount(long actor, long lab);

    boolean canImportMicroArrays(long actor, long lab);

    boolean isLabMember(long actor, long lab);

    boolean userCanUploadFileOfInstrument(long userId, long instrumentId);
}
