package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadStatus;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;
import com.infoclinika.mssharing.model.internal.repository.UploadDetailsRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus.QUEUE;
import static com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus.STARTED;
import static java.util.stream.Collectors.toList;

/**
 * @author Vitalii Petkanych
 */
@Component
public class S3FileImportRecovery {
    @Inject
    public S3FileImportService s3ImportService;
    @Inject
    private UploadDetailsRepository fileDetailsRepository;
    @Inject
    private ActiveBucketService activeBucketService;
    @Inject
    private TrackingUploadService trackingService;

    @PostConstruct
    private void restartUploads() {
        fileDetailsRepository.findByTypeAndStatus(UploadType.S3_COPY, UploadStatus.STARTED)
            .stream()
            .filter(u -> u.getFiles().stream().anyMatch(uploadNotFinished()))
            .forEach(u -> {
                final List<String> files = u.getFiles()
                    .stream()
                    .filter(f -> f.getStatus() == QUEUE || f.getStatus() == STARTED)
                    .map(FileDetails::getFileName)
                    .collect(toList());
                final Function<String, String> keyGenerator =
                    activeBucketService.importKeyGenerator(u.getUserId(), u.getInstrumentId());
                trackingService.startTracking(u);
                s3ImportService.copyFiles(u.getLogin(), u.getPassword(), u.getUrl(), files, keyGenerator);
            });
    }

    private Predicate<FileDetails> uploadNotFinished() {
        return f -> f.getStatus() == QUEUE
            || f.getStatus() == STARTED && f.getUploadRecoveryId() != null;
    }
}
