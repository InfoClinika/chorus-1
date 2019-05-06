package com.infoclinika.mssharing.model.internal.helper;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.SSEAlgorithm;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.UploadLimitException;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.s3client.AWSConfigService;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.dto.UploadLimitCheckResult;
import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * @author slava
 *      on 5/31/17.
 */
@Service
@Transactional
public class ProcessedFileServiceImpl implements ProcessedFileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessedFileServiceImpl.class);

    private final ExperimentRepository experimentRepository;
    private final AWSConfigService awsConfigService;
    private final StoredObjectPathsTemplate storedObjectPaths;
    private final FileMetaDataRepository fileMetaDataRepository;
    private final FileManagementTemplate<FileMetaDataInfo> fileManagement;
    private final InstrumentManagement instrumentManagement;
    private final RuleValidator ruleValidator;
    private final CloudStorageClientsProvider cloudStorageClientsProvider;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public ProcessedFileServiceImpl(ExperimentRepository experimentRepository,
                                    AWSConfigService awsConfigService,
                                    StoredObjectPathsTemplate storedObjectPaths,
                                    FileMetaDataRepository fileMetaDataRepository,
                                    FileManagementTemplate<FileMetaDataInfo> fileManagement,
                                    InstrumentManagement instrumentManagement,
                                    RuleValidator ruleValidator,
                                    CloudStorageClientsProvider cloudStorageClientsProvider,
                                    AmazonPropertiesProvider amazonPropertiesProvider) {
        this.experimentRepository = experimentRepository;
        this.awsConfigService = awsConfigService;
        this.storedObjectPaths = storedObjectPaths;
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.fileManagement = fileManagement;
        this.instrumentManagement = instrumentManagement;
        this.ruleValidator = ruleValidator;
        this.cloudStorageClientsProvider = cloudStorageClientsProvider;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    public Long create(long userId, long experimentId, String filename) {
        ActiveExperiment experiment = experimentRepository.findOne(experimentId);

        if (!ruleValidator.usersInSameLab(experiment.getCreator().getId(), userId)) {
            throw new AccessDenied("User doesn't have access to experiment");
        }

        final long instrumentId = experiment.getInstrumentRestriction().getInstrument().getId();

        final List<ActiveFileMetaData> filesByInstrumentAndName =
            fileMetaDataRepository.findByNameAndInstrument_id(filename, instrumentId);

        if (!filesByInstrumentAndName.isEmpty()) {
            throw new IllegalArgumentException(
                "File with name: " + filename + " for instrument with ID: " + instrumentId + " already exists");
        }

        String s3Path = storedObjectPaths.rawFilePath(userId, instrumentId, filename).getPath();

        final FileMetaDataInfo meta = new FileMetaDataInfo(
            filename,
            0,
            null,
            s3Path,
            0,
            false
        );
        final long fileId = fileManagement.createFile(userId, instrumentId, meta);
        final ActiveFileMetaData fmd = fileMetaDataRepository.findOne(fileId);
        fmd.setContentId(s3Path);
        fileMetaDataRepository.save(fmd);

        return fmd.getId();
    }

    public String get(long userId, long experimentId, long fileId) {
        final ActiveFileMetaData fmd = getActiveFile(userId, experimentId, fileId);

        return generateUrl(fmd.getContentId());
    }

    public void uploadDone(long userId, long experimentId, long fileId) {
        final ActiveFileMetaData fmd = getActiveFile(userId, experimentId, fileId);
        final ObjectMetadata objectMetadata = awsConfigService.s3Client().getObjectMetadata(
            amazonPropertiesProvider.getActiveBucket(),
            fmd.getContentId()
        );
        final long labId = fmd.getInstrument().getLab().getId();
        final long contentLength = objectMetadata.getContentLength();

        final UploadLimitCheckResult uploadLimit = instrumentManagement.checkUploadLimit(userId, labId, contentLength);

        if (uploadLimit.exceeded) {
            deleteS3Content(amazonPropertiesProvider.getActiveBucket(), fmd.getContentId());
            throw new UploadLimitException(uploadLimit.message);
        }

        fmd.setSizeInBytes(objectMetadata.getContentLength());
    }

    private void deleteS3Content(String bucket, String key) {
        try {
            final CloudStorageService cloudStorageService = cloudStorageClientsProvider.getCloudStorageService();
            cloudStorageService.deleteFromCloud(new CloudStorageItemReference(bucket, key));
        } catch (Exception ex) {
            LOGGER.warn("Couldn't delete S3 object. Bucket: " + bucket + ", key: " + key, ex);
        }
    }

    private ActiveFileMetaData getActiveFile(long userId, long experimentId, long fileId) {
        ActiveExperiment experiment = experimentRepository.findOne(experimentId);

        if (!ruleValidator.usersInSameLab(experiment.getCreator().getId(), userId)) {
            throw new AccessDenied("User doesn't have access to experiment");
        }

        final ActiveFileMetaData fmd = fileMetaDataRepository.findOne(fileId);

        if (!ruleValidator.usersInSameLab(fmd.getOwner().getId(), userId)) {
            throw new AccessDenied("User doesn't have access to the file");
        }

        return fmd;
    }

    private String generateUrl(String s3Path) {
        java.util.Date expiration = new java.util.Date();
        long milliSeconds = expiration.getTime();
        milliSeconds += 24_000 * 60 * 60;
        expiration.setTime(milliSeconds);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
            amazonPropertiesProvider.getActiveBucket(),
            s3Path,
            HttpMethod.PUT
        );

        if (amazonPropertiesProvider.isServersideEncryptionEnabled()) {
            generatePresignedUrlRequest =
                generatePresignedUrlRequest.withSSEAlgorithm(SSEAlgorithm.AES256.getAlgorithm());
        }

        generatePresignedUrlRequest.setExpiration(expiration);

        return awsConfigService.s3Client().generatePresignedUrl(generatePresignedUrlRequest).toString();
    }
}
