package com.infoclinika.mssharing.wizard.upload.service.api.listener;

import com.infoclinika.mssharing.wizard.upload.model.UploadFileItem;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public interface UploadListener {

    void onUploadStart(UploadFileItem item);

    void uploadProgressChanged(UploadFileItem item, long bytes);

    void onUploadComplete(UploadFileItem item);

    void onUploadError(UploadFileItem item);

    void onUploadReset(UploadFileItem item);

    void onDuplicate(UploadFileItem item);

    void onUploadUnavailable(UploadFileItem item);

}
