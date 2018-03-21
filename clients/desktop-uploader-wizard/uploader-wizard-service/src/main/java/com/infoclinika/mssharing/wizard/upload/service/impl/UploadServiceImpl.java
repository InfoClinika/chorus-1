package com.infoclinika.mssharing.wizard.upload.service.impl;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.infoclinika.mssharing.dto.VendorEnum;
import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.response.*;
import com.infoclinika.mssharing.upload.common.transfer.api.Uploader;
import com.infoclinika.mssharing.upload.common.transfer.impl.UploadItem;
import com.infoclinika.mssharing.upload.common.web.api.WebService;
import com.infoclinika.mssharing.upload.common.web.api.exception.RestServiceException;
import com.infoclinika.mssharing.upload.common.web.api.exception.UploadLimitExceededException;
import com.infoclinika.mssharing.web.rest.RestExceptionType;
import com.infoclinika.mssharing.wizard.upload.model.UploadConfig;
import com.infoclinika.mssharing.wizard.upload.model.UploadFileItem;
import com.infoclinika.mssharing.wizard.upload.model.ZipConfig;
import com.infoclinika.mssharing.wizard.upload.service.api.UploadService;
import com.infoclinika.mssharing.wizard.upload.service.api.ZipService;
import com.infoclinika.mssharing.wizard.upload.service.api.listener.UploadListener;
import com.infoclinika.mssharing.wizard.upload.service.api.listener.UploadZipListener;
import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
@Service
public class UploadServiceImpl implements UploadService {
    private static final Logger LOGGER = Logger.getLogger(UploadServiceImpl.class);

    @Inject
    private ZipService zipService;

    @Inject
    private WebService webService;

    @Override
    public void upload(List<UploadFileItem> items, UploadConfig config, UploadZipListener listener) {
        for (UploadFileItem item : items) {
            upload(item, config, listener);
        }
    }

    @Override
    public void upload(UploadFileItem item, UploadConfig config, UploadZipListener listener) {
        LOGGER.info("Start uploading an item: " + item.getName());

        final InstrumentDTO instrument = config.getInstrument();
        final VendorDTO vendor = instrument.getVendor();
        if (vendor.folderArchiveUploadSupport || vendor.multipleFiles) {
            final ZipConfig zipConfig = config.getZipConfig();
            zipService.zip(item, zipConfig, listener);

            if (!StringUtils.isEmpty(item.getError()) || item.isCanceled()) {
                return;
            }

            uploadItem(item, config, listener, true);
        } else {
            uploadItem(item, config, listener, false);
        }
    }

    private void uploadItem(UploadFileItem item, UploadConfig config, UploadListener listener, boolean isArchive) {
        final Uploader uploader = config.getUploader();
        final InstrumentDTO instrument = config.getInstrument();

        //post upload request to the server
        //after that item will have contentId, fileId set
        final boolean canUpload = postUploadRequest(item, instrument.getId(), isArchive, listener);
        if (!canUpload) {
            return;
        }

        LOGGER.info("Upload item content ID: " + item.getContentId());

        final UploadItem uploadItem = new UploadItem(
                item.getResultFile(),
                item.getContentId(),
                item.getAuthorization(),
                item.getFormattedDate(),
                item.isSseEnabled()
        );

        final UploadProgressListener internalListener =
                new UploadProgressListener(item, listener, instrument.getId(), isArchive);

        uploader.upload(uploadItem, internalListener);
    }

    private boolean postUploadRequest(UploadFileItem item, long instrumentId, boolean isArchive, UploadListener listener) {
        LOGGER.info("Post upload file request. File: " + item.getName() + " Size: " + item.getFileSize());

        final UploadFilesDTORequest.UploadFile uploadFile = new UploadFilesDTORequest.UploadFile(
                item.getName(),
                item.getLabels(),
                item.getFileSize(),
                item.getSpecieId(),
                isArchive
        );

        final UploadFilesDTORequest request = new UploadFilesDTORequest(instrumentId, newArrayList(uploadFile));

        SSEUploadFilesDTOResponse response;

        try {

            response = webService.postStartSSEUploadRequest(request);

        } catch (RestServiceException ex) {
            LOGGER.info("Cannot post request to start upload. Item name: " + item.getName());

            listener.onUploadError(item);

            if (ex.getExceptionType() == RestExceptionType.UPLOAD_LIMIT_EXCEEDED) {
                throw new UploadLimitExceededException(ex.getMessage());
            }

            return false;
        }

        LOGGER.info("Upload file request posted successfully");

        if (response.getFiles().size() == 0) {
            LOGGER.info("Upload is unavailable. Item: " + item.getName());
            listener.onUploadUnavailable(item);
            return false;
        }

        final SSEUploadFilesDTOResponse.UploadFileItem uploadFileItem = response.getFiles().get(0);

        item.setContentId(uploadFileItem.getPath());
        item.setAuthorization(uploadFileItem.getAuthorization());
        item.setFormattedDate(uploadFileItem.getFormattedDate());
        item.setSseEnabled(uploadFileItem.isSseEnabled());

        if(uploadFileItem.getPath() == null){

            LOGGER.info("File already exists. It is a duplicate. File name: " + item.getName());
            listener.onDuplicate(item);

            return false;
        }

        return true;
    }

    private boolean postUploadRequestBeforeComplete(UploadFileItem item,
                                                    long instrumentId,
                                                    boolean isArchive,
                                                    UploadListener listener) {
        final UploadFilesDTORequest.UploadFile uploadFile = new UploadFilesDTORequest.UploadFile(
                item.getName(),
                item.getLabels(),
                item.getFileSize(),
                item.getSpecieId(),
                isArchive
        );

        final UploadFilesDTORequest request = new UploadFilesDTORequest(instrumentId, newArrayList(uploadFile));

        UploadFilesDTOResponse response;

        try {
            response = webService.postStartUploadRequestBeforeFinish(request);
        } catch (RestServiceException ex) {
            LOGGER.info("Cannot post request before complete upload. Item name: " + item.getName());
            listener.onUploadError(item);

            return false;
        }

        LOGGER.info("Upload file before complete request posted successfully");

        if (response.getFiles().size() == 0) {
            LOGGER.info("Upload is not available. Item: " + item.getName());
            listener.onUploadUnavailable(item);
            return false;
        }

        final UploadFilesDTOResponse.UploadFile responseFile = response.getFiles().get(0);
        item.setFileId(responseFile.getFileId());

        return true;
    }

    private void postUploadCompleteRequest(UploadFileItem item,
                                           long instrumentId,
                                           boolean isArchive,
                                           UploadListener listener) {
        final boolean result = postUploadRequestBeforeComplete(item, instrumentId, isArchive, listener);
        if (!result) {
            return;
        }

        LOGGER.info("Upload complete, posting confirm request... Item name: " + item.getName());

        final ConfirmMultipartUploadDTO request = new ConfirmMultipartUploadDTO(
                item.getFileId(),
                item.getContentId()
        );

        try {
            final CompleteUploadDTO response = webService.postCompleteUploadRequest(request);
            LOGGER.info("Confirmation response for item:" + item.getName() + " -> confirmed - " + response.isConfirmed());

            if (!response.isConfirmed()) {
                LOGGER.info("Upload is not available. Item: " + item.getName());
                listener.onUploadUnavailable(item);
            } else {
                LOGGER.info("Upload complete for item: " + item.getName());
                listener.onUploadComplete(item);
            }

        } catch (RestServiceException ex) {
            LOGGER.info("Cannot post request to finish upload. Item name: " + item.getName());
            listener.onUploadError(item);
        }
    }

    private class UploadProgressListener implements ProgressListener {
        private final UploadFileItem item;
        private final UploadListener listener;
        private final long instrumentId;
        private final boolean isArchive;

        private UploadProgressListener(UploadFileItem item,
                                       UploadListener listener,
                                       long instrumentId,
                                       boolean isArchive) {

            this.item = checkNotNull(item);
            this.listener = checkNotNull(listener);
            this.instrumentId = instrumentId;
            this.isArchive = isArchive;
        }

        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            if (item.isCanceled()) {
                LOGGER.info("Uploading has been canceled for item: " + item.getName());
                return;
            }

            final ProgressEventType eventType = progressEvent.getEventType();
            switch (eventType) {
                case TRANSFER_STARTED_EVENT:
                    listener.onUploadStart(item);
                    break;
                case TRANSFER_CANCELED_EVENT:
                    LOGGER.info("Uploading canceled for item: " + item.getName());
                    item.setCanceled(true);
                    listener.onUploadError(item);
                    break;
                case TRANSFER_COMPLETED_EVENT:
                    postUploadCompleteRequest(item, instrumentId, isArchive, listener);
                    break;
                case TRANSFER_FAILED_EVENT:
                    LOGGER.info("Uploading failed for item: " + item.getName());
                    listener.onUploadError(item);
                    break;
                case HTTP_REQUEST_CONTENT_RESET_EVENT:
                    LOGGER.info("Uploading reset for item: " + item.getName());
                    listener.onUploadReset(item);
                    break;
                default:
                    listener.uploadProgressChanged(item, progressEvent.getBytesTransferred());
            }
        }
    }
}
