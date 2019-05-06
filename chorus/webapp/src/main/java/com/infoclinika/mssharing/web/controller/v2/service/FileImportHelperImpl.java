package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.InstrumentRepository;
import com.infoclinika.mssharing.model.internal.repository.UploadDetailsRepository;
import com.infoclinika.mssharing.model.internal.repository.UploadFileDetailsRepository;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.platform.model.FileTransferNotifier;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;
import com.infoclinika.mssharing.web.controller.v2.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus.REGISTERED;
import static com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus.UPLOADED;

/**
 * @author Vitalii Petkanych
 */
@Service
class FileImportHelperImpl implements FileImportHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImportHelperImpl.class);

    @Inject
    private ActiveBucketService activeBucketService;
    @Inject
    private FileMetaDataRepository fileRepository;
    @Inject
    private FileManagementTemplate<FileMetaDataInfo> fileManagement;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    @Named("InboxFileTransferNotifier")
    private FileTransferNotifier inboxNotifier;
    @Inject
    private UploadDetailsRepository uploadDetailsRepository;
    @Inject
    private UploadFileDetailsRepository fileDetailsRepository;

    @Override
    public void importFinish(UploadDetails uploadDetails) {
        uploadDetails.setPassword(null);
        uploadDetails.setUploadFinished(LocalDateTime.now());
        uploadDetailsRepository.save(uploadDetails);

        if (uploadDetails.getType() != UploadType.S3_LINK) {
            final Duration duration =
                Duration.between(uploadDetails.getUploadStarted(), uploadDetails.getUploadFinished());
            final long totalSize = uploadDetails.getFiles()
                .stream()
                .filter(f -> f.getStatus() == UPLOADED || f.getStatus() == REGISTERED)
                .mapToLong(FileDetails::getSizeUploaded)
                .sum();
            final long filesCount = uploadDetails.getFiles()
                .stream()
                .filter(f -> f.getStatus() == UPLOADED || f.getStatus() == REGISTERED)
                .count();
            LOGGER.debug(
                "S3 Import (uploadId={}) summary:\n" +
                    "\tfiles   : {}\n" +
                    "\tduration: {}\n" +
                    "\tsize    : {}\n" +
                    "\tspeed   : {}/s",
                new Object[] {uploadDetails.getId(),
                    filesCount, FileUtil.formatDuration(duration), FileUtil.formatSize(totalSize),
                    duration.getSeconds() == 0 ? '-' : FileUtil.formatSize(totalSize / duration.getSeconds())}
            );
        }

        List<DictionaryItem> files = uploadDetails.getFiles()
            .stream()
            .filter(f -> f.getStatus() == UPLOADED)
            .map(f -> FileUtil.extractName(f.getFileName()))
            .map(f -> new DictionaryItem(0, f))
            .collect(Collectors.toList());

        final Lab lab = instrumentRepository.labOfInstrument(uploadDetails.getInstrumentId());
        inboxNotifier.notifyFileTransferCompleted(uploadDetails.getUserId(), lab.getId(), files);
    }

    @Override
    public void createFileMetadata(long userId, long instrumentId, FileDetails file) {
        final String name = FileUtil.extractName(file.getFileName());
        final String path = activeBucketService.importKeyGenerator(userId, instrumentId).apply(name);
        final String labels = file.getLabels() == null ? null : String.join(",", file.getLabels());
        final FileMetaDataInfo fmd =
            new FileMetaDataInfo(name, file.getSize(), labels, path, file.getSpecieId(), false);
        final long fileId = fileManagement.createFile(userId, instrumentId, fmd);
        file.setFileId(fileId);
        fileDetailsRepository.save(file);
    }

    @Override
    public void finishFileMetadata(FileDetails file) {
        final ActiveFileMetaData fmd = fileRepository.findOne(file.getFileId());
        fmd.setContentId(fmd.getDestinationPath());
        fmd.setDestinationPath(null);
        fmd.setUploadId(null);
        fileRepository.save(fmd);
        file.setStatus(REGISTERED);
        fileDetailsRepository.save(file);
    }

    @Override
    public void removeFileMetadata(long fileId) {
        fileRepository.delete(fileId);
    }

    @Override
    public boolean isFileAlreadyUploaded(long userId, long instrumentId, String fullName) {
        final String fileName = FileUtil.extractName(fullName);
        return fileRepository.isFileAlreadyUploadedForInstrument(userId, instrumentId, fileName);
    }

    @Override
    public void saveFileDetails(FileDetails file) {
        fileDetailsRepository.save(file);
    }

    @Override
    public void saveUploadDetails(UploadDetails upload) {
        uploadDetailsRepository.save(upload);
    }

    @Override
    public long createFileReference(
        String bucket,
        String key,
        boolean readOnly,
        long size,
        String labels,
        long userId,
        long instrumentId,
        long specieId
    ) {
        final String fileName = FileUtil.extractName(key);
        final FileMetaDataInfo fmd =
            new FileMetaDataInfo(fileName, size, labels, key, specieId, false, bucket, key, readOnly);
        return fileManagement.createFile(userId, instrumentId, fmd);
    }
}
