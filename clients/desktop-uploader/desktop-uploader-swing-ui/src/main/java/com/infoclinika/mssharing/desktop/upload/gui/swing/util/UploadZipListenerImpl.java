package com.infoclinika.mssharing.desktop.upload.gui.swing.util;

import com.infoclinika.mssharing.desktop.upload.model.UploadFileItem;
import com.infoclinika.mssharing.desktop.upload.model.ZipFileItem;
import com.infoclinika.mssharing.desktop.upload.service.api.listener.UploadZipListener;
import com.infoclinika.mssharing.desktop.upload.service.impl.list.UploadZipList;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static com.infoclinika.mssharing.desktop.upload.gui.swing.util.ListChangeType.*;
import static com.infoclinika.mssharing.desktop.upload.model.UploadStatus.*;

/**
 * @author timofey.kasyanov
 *     date:   29.01.14
 */
@Component
public class UploadZipListenerImpl implements UploadZipListener {

    @Inject
    private UploadZipList list;

    @Inject
    private DesktopUploaderHelper helper;

    @Override
    public void onUploadStart(UploadFileItem item) {

        item.setStatus(UPLOADING);

        list.change(item, UPLOAD_CHANGE);

    }

    @Override
    public void uploadProgressChanged(UploadFileItem item, long bytes) {

        final long newUploadedValue = item.getUploadedValue() + bytes;

        item.setUploadedValue(newUploadedValue);

        list.change(item, UPLOAD_CHANGE);

    }

    @Override
    public void onUploadComplete(UploadFileItem item) {

        item.setUploadedValue(item.getFileSize());

        item.setStatus(UPLOAD_COMPLETE);

        list.change(item, UPLOAD_CHANGE);
        list.change(item, UPLOAD_COMPLETE_CHANGE);

        helper.deleteZipIfNeeded(item);

    }

    @Override
    public void onUploadError(UploadFileItem item) {

        item.setStatus(ERROR);

        list.change(item, UPLOAD_ERROR_CHANGE);

        helper.deleteZipIfNeeded(item);

        helper.deleteS3Object(item.getContentId());

    }

    @Override
    public void onUploadReset(UploadFileItem item) {

        final long newUploadedValue = 0;

        item.setUploadedValue(newUploadedValue);

        list.change(item, UPLOAD_CHANGE);

    }

    @Override
    public void onZipStart(ZipFileItem item) {

        item.setStatus(ZIPPING);

        list.change((UploadFileItem) item, ZIP_CHANGE);

    }

    @Override
    public void zipProgressChanged(ZipFileItem item, long bytes) {

        final long newZippedValue = item.getZippedValue() + bytes;

        item.setZippedValue(newZippedValue);

        list.change((UploadFileItem) item, ZIP_CHANGE);

    }

    @Override
    public void onZipComplete(ZipFileItem item) {


        list.change((UploadFileItem) item, FILE_SIZE_CHANGE);

        item.setZippedValue(item.getZipSize());

        item.setStatus(ZIP_COMPLETE);

        list.change((UploadFileItem) item, ZIP_CHANGE);

    }

    @Override
    public void onZipError(ZipFileItem item) {

        item.setStatus(ERROR);

        list.change((UploadFileItem) item, ZIP_ERROR_CHANGE);

        helper.deleteZipIfNeeded((UploadFileItem) item);

    }

    @Override
    public void onDuplicate(UploadFileItem item) {

        item.setStatus(DUPLICATE);

        list.change(item, UPLOAD_ERROR_CHANGE);

        helper.deleteZipIfNeeded(item);

    }

    @Override
    public void onUploadUnavailable(UploadFileItem item) {

        item.setStatus(UPLOAD_UNAVAILABLE);

        list.change(item, UPLOAD_ERROR_CHANGE);

        helper.deleteZipIfNeeded(item);

        helper.deleteS3Object(item.getContentId());
    }

    @Override
    public void onAllItemsUploaded(List<UploadFileItem> items) {
        helper.handleFilesUploadCompleted(items);
    }

    @Override
    public void onUploadLimitExceeded() {
        helper.showUploadLimitExceededErrorMessage();
    }
}
