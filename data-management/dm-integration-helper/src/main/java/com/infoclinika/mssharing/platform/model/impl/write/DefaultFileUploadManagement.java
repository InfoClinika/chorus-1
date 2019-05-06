package com.infoclinika.mssharing.platform.model.impl.write;

import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ActionsNotAllowedException;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.write.FileUploadManagementTemplate;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author : Alexander Serebriyan
 */
@Transactional
@Component
public class DefaultFileUploadManagement implements FileUploadManagementTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFileUploadManagement.class);
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private FileRepositoryTemplate<FileMetaDataTemplate> fileMetaDataRepository;
    @Inject
    private TransactionTemplate transactionTemplate;

    @Override
    public void startMultipartUpload(long actor, long file, String uploadId, String destinationPath) {
        final FileMetaDataTemplate entity = load(file);
        if (!entity.getOwner().getId().equals(actor)) {
            throw new AccessDenied("Only owner can set upload ID");
        }
        if (ruleValidator.isUploadComplete(file)) {
            throw new AccessDenied("Content already set, cannot set upload ID");
        }
        entity.setUploadId(uploadId);
        entity.setDestinationPath(destinationPath);
        LOGGER.debug("The multipart upload ID = {} for user {} has been set: {}", file, actor, uploadId);
        fileMetaDataRepository.save(entity);
    }

    private FileMetaDataTemplate load(long file) {
        return checkNotNull(fileMetaDataRepository.findOne(file));
    }

    @Override
    public void completeMultipartUpload(long actor, long file, String contentId) {

        final FileMetaDataTemplate entity = load(file);

        beforeCompleteMultipartUpload(actor, entity, contentId);

        onCompleteMultipartUpload(actor, file, contentId, entity);
    }

    protected Long onCompleteMultipartUpload(final long actor, final long file, final String contentId,
                                             final FileMetaDataTemplate entity) {

        return transactionTemplate.execute(status -> {

            entity.setContentId(contentId);
            LOGGER.debug(
                "The content for file with ID = {} for user {} has been set. Path = {}", file, actor, contentId
            );

            entity.setUploadId(null);
            entity.setDestinationPath(null);
            fileMetaDataRepository.save(entity);
            return entity.getId();
        });
    }

    protected void beforeCompleteMultipartUpload(long actor, FileMetaDataTemplate file, String contentId) {
        if (!ruleValidator.canUserPerformActions(actor)) {
            throw new ActionsNotAllowedException(actor);
        }
        if (!file.getOwner().getId().equals(actor)) {
            throw new AccessDenied("Only owner can set content");
        }
        if (ruleValidator.isUploadComplete(file.getId())) {
            throw new AccessDenied("Content already set");
        }
    }

    @Override
    public void cancelUpload(long actor, long file) {
        final FileMetaDataTemplate entity = load(file);
        if (!entity.getOwner().getId().equals(actor)) {
            throw new AccessDenied("Only owner is able to cancel file upload");
        }
        fileMetaDataRepository.delete(entity);
    }

    @Override
    public boolean checkMultipleFilesValidForUpload(long instrument, List<String> files) {
        throw new NotImplementedException(
            "Method DefaultFileUploadManagement#checkMultipleFilesValidForUpload is not implemented yet");
    }

    @Override
    public boolean isFileAlreadyUploadedForInstrument(long actor, long instrument, String fileName) {
        return fileMetaDataRepository.isFileAlreadyUploadedForInstrument(actor, instrument, fileName);
    }
}
