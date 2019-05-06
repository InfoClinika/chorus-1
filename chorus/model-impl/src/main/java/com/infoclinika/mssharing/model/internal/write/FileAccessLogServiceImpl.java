package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.model.internal.entity.FileAccessLog;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.DeletedFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.write.FileAccessLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author vladislav.kovchug
 */
@Service
@Async("fileAccessLogExecutor")
public class FileAccessLogServiceImpl implements FileAccessLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileAccessLogServiceImpl.class);

    @Inject
    private FileAccessLogRepository fileAccessLogRepository;

    @Inject
    private FileMetaDataRepository fileRepository;

    @Inject
    private DeletedFileMetaDataRepository deletedFileMetaDataRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private LabRepository labRepository;

    @Inject
    private InstrumentRepository instrumentRepository;


    private void saveFileAccessLog(
        AbstractFileMetaData metaData,
        User user,
        Lab lab,
        FileAccessLog.OperationType operationType
    ) {
        final Long userId = user == null ? null : user.getId();
        final String email = user == null ? null : user.getEmail();
        final Long userLabId = lab == null ? null : lab.getId();
        final String name = lab == null ? null : lab.getName();
        final Long fmdId = metaData.getId();
        final long fmdSize = metaData.getSizeInBytes();
        final String fmdContentId = metaData.getContentId();
        final String fmdArchiveId = metaData.getArchiveId();
        final String fmdName = metaData.getName();

        FileAccessLog log = new FileAccessLog(userId, email, userLabId, name,
            fmdId, fmdSize, fmdContentId, fmdArchiveId,
            fmdName, operationType, new Date()
        );
        fileAccessLogRepository.save(log);
    }

    @Override
    public void logFileUploadStart(long actor, long instrument, long fileId) {
        final ActiveFileMetaData metaData = fileRepository.findOne(fileId);
        if (metaData == null) {
            LOGGER.warn("Try to log file upload start of not existing file with Id: {}", Long.toString(fileId));
        } else {
            final User user = userRepository.findOne(actor);
            final Lab lab = instrumentRepository.findOne(instrument).getLab();
            saveFileAccessLog(metaData, user, lab, FileAccessLog.OperationType.FILE_UPLOAD_STARTED);
        }
    }

    @Override
    public void logFileUploadConfirm(long actor, long labId, long fileId) {
        final ActiveFileMetaData metaData = fileRepository.findOne(fileId);
        if (metaData == null) {
            LOGGER.warn("Try to log file upload confirm of not existing file with Id: {}", Long.toString(fileId));
        } else {
            final User user = userRepository.findOne(actor);
            final Lab lab = labRepository.findOne(labId);
            saveFileAccessLog(metaData, user, lab, FileAccessLog.OperationType.FILE_UPLOAD_CONFIRMED);
        }
    }

    @Override
    public void logFileDelete(long fileId) {
        final DeletedFileMetaData metaData = deletedFileMetaDataRepository.findOne(fileId);
        if (metaData == null) {
            LOGGER.warn("Try to log file delete of not existing file with Id: {}", Long.toString(fileId));
        } else {
            final Lab lab = metaData.getInstrument() == null ? null : metaData.getInstrument().getLab();
            final User user = metaData.getOwner();
            saveFileAccessLog(metaData, user, lab, FileAccessLog.OperationType.FILE_DELETED);
        }
    }

    @Override
    public void logFileDeletePermanently(long actor, long fileId) {
        final ActiveFileMetaData metaData = fileRepository.findOne(fileId);
        if (metaData == null) {
            LOGGER.warn("Try to log file delete permanently of not existing file with Id: {}", Long.toString(fileId));
        } else {
            final Lab lab = metaData.getInstrument() == null ? null : metaData.getInstrument().getLab();
            final User user = userRepository.findOne(actor);
            saveFileAccessLog(metaData, user, lab, FileAccessLog.OperationType.FILE_DELETED_PERMANENTLY);
        }
    }

    @Override
    public void logFileArchiveStart(long actor, long fileId) {
        final ActiveFileMetaData metaData = fileRepository.findOne(fileId);
        if (metaData == null) {
            LOGGER.warn("Try to log file archive start of not existing file with Id: {}", Long.toString(fileId));
        } else {
            final User user = userRepository.findOne(actor);
            final Lab lab = metaData.getInstrument() == null ? null : metaData.getInstrument().getLab();
            saveFileAccessLog(metaData, user, lab, FileAccessLog.OperationType.FILE_ARCHIVE_STARTED);
        }
    }

    @Override
    public void logFileArchiveConfirm(long fileId) {
        final ActiveFileMetaData metaData = fileRepository.findOne(fileId);
        if (metaData == null) {
            LOGGER.warn("Try to log file archive confirm of not existing file with Id: {}", Long.toString(fileId));
        } else {
            final User user = metaData.getOwner();
            final Lab lab = metaData.getInstrument() == null ? null : metaData.getInstrument().getLab();
            saveFileAccessLog(metaData, user, lab, FileAccessLog.OperationType.FILE_ARCHIVE_CONFIRMED);
        }
    }

    @Override
    public void logFileDownload(long actor, ChorusFileData fileData) {
        final ActiveFileMetaData metaData = fileRepository.findOne(fileData.id);
        if (metaData == null) {
            LOGGER.warn("Try to log file download of not existing file with Id: {}", Long.toString(fileData.id));
        } else {
            final Lab lab = labRepository.findOne(fileData.lab);
            final User user = metaData.getOwner();
            saveFileAccessLog(metaData, user, lab, FileAccessLog.OperationType.FILE_DOWNLOAD_STARTED);
        }
    }
}
