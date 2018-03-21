package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.infoclinika.mssharing.model.AdminNotifier;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.StorageData;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.AdministrationToolsManagement;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Herman Zamula
 */
@Service
public class AdministrationToolsManagementImpl implements AdministrationToolsManagement {

    private static final Logger LOGGER = Logger.getLogger(AdministrationToolsManagementImpl.class);

    @Inject
    private AdminNotifier adminNotifier;
    @Inject
    private UserRepository userRepository;
    @Inject
    private RuleValidator validator;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private FileOperationsManager fileOperationsManager;

    @Override
    public void broadcastNotification(long actor, String title, String body) {
        if (!validator.hasAdminRights(actor)) {
            throw new AccessDenied("Only admin can broadcast notification to all users");
        }

        Preconditions.checkNotNull(title, "Title of the message cannot be null");
        Preconditions.checkNotNull(title, "Body of the message cannot be null");

        final List<UserRepositoryTemplate.UserShortRecord> shortRecordsAll = userRepository.findShortRecordsAll();

        for (UserRepositoryTemplate.UserShortRecord user : shortRecordsAll) {
            adminNotifier.sendCommonEmail(user.id, title, body);
        }

    }

    @Async
    @Override
    public void unarchiveInconsistentFiles(long actor) {
        LOGGER.info("Unarchive inconsistent files");
        final List<Long> fileIds = fileMetaDataRepository.getInconsistentFilesIds();
        LOGGER.info("Inconsistent files count: " + fileIds.size());
        for (Long fileId : fileIds) {
            try{
                final ActiveFileMetaData file = fileMetaDataRepository.findOne(fileId);
                final StorageData.Status storageStatus = file.getStorageData().getStorageStatus();
                if(storageStatus == StorageData.Status.ARCHIVED || storageStatus == StorageData.Status.ARCHIVING_REQUESTED) {
                    fileOperationsManager.markFilesToUnarchive(file.getOwner().getId(), Sets.newHashSet(fileId));
                }
            } catch (Exception e) {
                LOGGER.warn("Couldn't unarchive file. ID: " + fileId, e);
            }
        }
        fileOperationsManager.unarchiveMarkedFiles();
    }
}
