package com.infoclinika.mssharing.web.controller.v2.service;

import com.amazonaws.services.s3.AmazonS3URI;
import com.infoclinika.mssharing.model.internal.entity.upload.*;
import com.infoclinika.mssharing.web.controller.v2.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus.*;

/**
 * @author Vitalii Petkanych
 */
@Service
public class TrackingUploadServiceImpl implements TrackingUploadService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingUploadServiceImpl.class);

    private static final long CLEAN_DELAY = 1;

    @Inject
    private FileImportHelper fileImportHelper;

    private HashMap<String, FileDetails> fileIndex = new HashMap<>();
    private HashMap<Long, UploadDetails> uploadIndex = new HashMap<>();
    private HashMap<String, Long> uploadByFileIndex = new HashMap<>();

    @Override
    public UploadDetails getTrackingDetails(long id) {
        return uploadIndex.get(id);
    }

    @Override
    public void startTracking(UploadDetails uploadDetails) {
        final String host = getHost(uploadDetails.getUrl(), uploadDetails.getType());
        uploadDetails.setUploadStarted(LocalDateTime.now());
        uploadIndex.put(uploadDetails.getId(), uploadDetails);
        uploadDetails.getFiles().forEach(file -> {
            fileIndex.put(trackingId(host, file.getFileName()), file);
            uploadByFileIndex.put(file.getFileName(), uploadDetails.getId());
        });
    }

    @Override
    public void updateFileProgress(String trackingId, long bytes) {
        final FileDetails file = fileIndex.get(trackingId);
        if (file == null) {
            throw new IllegalArgumentException();
        }
        file.setSizeUploaded(bytes);
        fileImportHelper.saveFileDetails(file);
    }

    @Override
    public void updateFileStatus(String trackingId, UploadFileStatus status) {
        LOGGER.info("Copy {} {}", trackingId, status);
        final FileDetails file = fileIndex.get(trackingId);
        updateFileStatus(file, status);
    }

    private void updateFileStatus(FileDetails file, UploadFileStatus status) {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        if (file.getStatus() == status) {
            return;
        }

        file.setStatus(status);

        final long uploadId = uploadByFileIndex.get(file.getFileName());
        final UploadDetails upload = uploadIndex.get(uploadId);

        if (status == STARTED) {
            file.setStatus(status);
            file.setUploadStarted(LocalDateTime.now());
            if (file.getFileId() == 0) {
                fileImportHelper.createFileMetadata(upload.getUserId(), upload.getInstrumentId(), file);
            }
        } else if (status == FAILED || status == UPLOADED || status == CANCELED) {
            file.setUploadFinished(LocalDateTime.now());
            if (status == UPLOADED) {
                file.setSizeUploaded(file.getSize());
                if (upload.getType() != UploadType.S3_LINK) {
                    final Duration duration = Duration.between(file.getUploadStarted(), file.getUploadFinished());
                    LOGGER.debug(
                        "Copy {} (fileId={}) summary:\n" +
                            "\tduration: {}\n" +
                            "\tsize    : {}\n" +
                            "\tspeed   : {}/s",
                        new Object[] {file.getFileName(), file.getFileId(),
                            FileUtil.formatDuration(duration), FileUtil.formatSize(file.getSize()),
                            duration.getSeconds() == 0 ? '-'
                                : FileUtil.formatSize(file.getSize() / duration.getSeconds())}
                    );
                }
                fileImportHelper.finishFileMetadata(file);
            } else if (file.getFileId() > 0) {
                fileImportHelper.removeFileMetadata(file.getFileId());
            }

            if (upload.getStatus() != UploadStatus.CANCELED) {
                final boolean finished = upload.getFiles()
                    .stream()
                    .map(FileDetails::getStatus)
                    .allMatch(s -> s == FAILED || s == UPLOADED || s == REGISTERED || s == CANCELED);
                if (finished) {
                    upload.setStatus(UploadStatus.FINISHED);
                    upload.setUploadFinished(LocalDateTime.now());
                    fileImportHelper.importFinish(upload);
                    asyncCleanTackingData(upload);
                }
            }
        }
    }

    @Override
    public void cancelFile(long uploadId, long fileId) {
        uploadIndex.computeIfPresent(uploadId, (id, upload) -> {
            upload.getFiles()
                .stream()
                .filter(f -> f.getFileId() == fileId)
                .findAny()
                .ifPresent(f -> {
                    if (f.getStatus() == QUEUE || f.getStatus() == STARTED) {
                        LOGGER.debug(
                            "Copy {} (fileId={}) canceled",
                            FileUtil.extractName(f.getFileName()),
                            f.getFileId()
                        );
                        updateFileStatus(f, CANCELED);
                        f.setStatus(CANCELED);
                    }
                });
            return upload;
        });
    }

    @Override
    public void cancelUpload(long uploadId) {
        uploadIndex.computeIfPresent(uploadId, (id, upload) -> {
            upload.setStatus(UploadStatus.CANCELED);
            upload.getFiles()
                .stream()
                .filter(f -> f.getStatus() == QUEUE || f.getStatus() == STARTED)
                .forEach(f -> updateFileStatus(f, CANCELED));
            asyncCleanTackingData(upload);
            fileImportHelper.saveUploadDetails(upload);
            return upload;
        });
    }

    @Override
    public UploadFileTracker startFileTracking(String trackingId) {
        final FileDetails file = fileIndex.get(trackingId);
        if (file == null) {
            throw new IllegalArgumentException();
        }
        return new UploadFileTrackerImpl(file);
    }

    private String getHost(String url, UploadType type) {

        if (type == null) {
            throw new IllegalArgumentException("Upload type is not specified");
        }

        switch (type) {
            case S3_COPY:
            case S3_LINK:
                return new AmazonS3URI(url).getBucket();
            default:
                throw new IllegalArgumentException("Type " + type + " is unsupported");
        }
    }

    @Async
    public void asyncCleanTackingData(UploadDetails upload) {
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> cleanTackingData(upload), CLEAN_DELAY, TimeUnit.MINUTES);
        executor.shutdown();
    }

    private void cleanTackingData(UploadDetails upload) {
        LOGGER.info("Cleanup tracking (uploadId={})", upload.getId());
        final List<String> keys = uploadByFileIndex.entrySet()
            .stream()
            .filter(e -> Objects.equals(e.getValue(), upload.getId()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        fileIndex.entrySet().removeIf(e -> keys.contains(e.getKey()));
        uploadByFileIndex.entrySet().removeIf(e -> keys.contains(e.getKey()));
        uploadIndex.remove(upload.getId());
    }

    private class UploadFileTrackerImpl implements UploadFileTracker {

        private static final int PROGRESS_SAVING_INTERVAL = 1000;

        private FileDetails file;

        private long nextProgressUpdate = 0;

        UploadFileTrackerImpl(FileDetails file) {
            this.file = file;
        }

        @Override
        public void updateFileProgress(long bytes) {
            file.setSizeUploaded(bytes);
            if (nextProgressUpdate < System.currentTimeMillis()) {
                fileImportHelper.saveFileDetails(file);
                nextProgressUpdate = System.currentTimeMillis() + PROGRESS_SAVING_INTERVAL;
            }
        }

        @Override
        public void updateFileStatus(UploadFileStatus status) {
            TrackingUploadServiceImpl.this.updateFileStatus(file, status);
            fileImportHelper.saveFileDetails(file);
        }

        @Override
        public boolean isFileCanceled() {
            return file.getStatus() == CANCELED;
        }

        @Override
        public boolean isFileInterrupted() {
            return file.getStatus() == STARTED && file.getUploadRecoveryId() != null;
        }

        @Override
        public String getFileRecoveryId() {
            return file.getUploadRecoveryId();
        }

        @Override
        public void setFileRecoveryId(String recoveryId) {
            file.setUploadRecoveryId(recoveryId);
            fileImportHelper.saveFileDetails(file);
        }

        @Override
        public void setFileId(long fileId) {
            file.setFileId(fileId);
            fileImportHelper.saveFileDetails(file);
        }
    }
}
