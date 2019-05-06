package com.infoclinika.mssharing.autoimporter.service.impl;

import com.amazonaws.event.ProgressEvent;
import com.google.common.base.Joiner;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.api.ProgressListener;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;
import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.response.CompleteUploadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;


/**
 * author Ruslan Duboveckij
 */
@Service
@Scope("prototype")
public class ProgressListenerImpl implements ProgressListener {
    private static final Logger LOG = LoggerFactory.getLogger(TaskUploadImpl.class);
    @Inject
    private WebService webService;
    private UploadItem uploadItem;
    private String watchFolder;
    private ObserverList<UploadItem> observer;
    private long currentTransferred;

    private static String joinerMsg(Object... objs) {
        return Joiner.on(" ").join(objs);
    }

    @Override
    public void init(UploadItem uploadItem, ObserverList<UploadItem> observer, String watchFolder) {
        this.uploadItem = uploadItem;
        this.observer = observer;
        this.watchFolder = watchFolder;
        LOG.info("Upload start: {}", uploadItem);
    }

    @Override
    public void progressChanged(ProgressEvent event) {
        currentTransferred += event.getBytesTransferred();
        uploadItem.setUploadedValue(currentTransferred);
        observer.notify(NotificationType.UPLOAD_VALUE, watchFolder, uploadItem);

        LOG.debug(joinerMsg(uploadItem.getName(), "-",
            byteCountToDisplaySize(currentTransferred), "/",
            byteCountToDisplaySize(uploadItem.getSize()), "/s"
        ));

        switch (event.getEventCode()) {
            case ProgressEvent.COMPLETED_EVENT_CODE:
                onCompleted();
                break;
            case ProgressEvent.FAILED_EVENT_CODE:
                LOG.error("Upload error: {}", uploadItem);
                break;
            default:
                throw new IllegalArgumentException("Unknown event code: " + event.getEventCode());
        }
    }

    private void onCompleted() {
        final CompleteUploadDTO completeUpload = webService.postCompleteUploadRequest(
            new ConfirmMultipartUploadDTO(uploadItem.getFileId(), uploadItem.getContentId()));
        LOG.info("Upload success: {} {}", completeUpload.isConfirmed(), uploadItem);
    }

    @Override
    public String toString() {
        return "ProgressListenerImpl{" +
            "uploadItem=" + uploadItem +
            '}';
    }

}
