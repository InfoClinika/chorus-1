package com.infoclinika.mssharing.web.controller.v2.service;

import com.amazonaws.services.s3.AmazonS3URI;
import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus;
import com.infoclinika.mssharing.model.internal.s3client.AWSConfigService;
import com.infoclinika.mssharing.web.controller.v2.service.TrackingUploadService.UploadFileTracker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Vitalii Petkanych
 */
@Service
public class S3ReferencesService {

    @Inject
    private AWSConfigService awsConfigService;

    @Inject
    private FileImportHelper fileImportHelper;

    @Inject
    private TrackingUploadService trackingService;

    @Async
    public void createReferences(long userId, long instrumentId, String url, List<FileDetails> files) {
        final String bucket = new AmazonS3URI(url).getBucket();
        files.forEach(fileReferenceCreator(bucket, instrumentId, userId));
    }

    private Consumer<FileDetails> fileReferenceCreator(String bucket, long instrumentId, long userId) {
        final boolean readOnly = !Objects.equals(bucket, awsConfigService.getActiveBucket());
        return f -> {
            final UploadFileTracker tracker =
                trackingService.startFileTracking(trackingService.trackingId(bucket, f.getFileName()));
            try {
                final long fileId = fileImportHelper.createFileReference(bucket,
                    f.getFileName(),
                    readOnly,
                    f.getSize(),
                    labelsAsString(f.getLabels()),
                    userId,
                    instrumentId,
                    f.getSpecieId()
                );
                tracker.setFileId(fileId);
                tracker.updateFileStatus(UploadFileStatus.UPLOADED);
            } catch (Exception e) {
                tracker.updateFileStatus(UploadFileStatus.FAILED);
            }
        };
    }

    private String labelsAsString(String[] labels) {
        return labels == null || labels.length == 0
            ? null
            : String.join(",", labels);
    }
}
