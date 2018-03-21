package com.infoclinika.mssharing.wizard.upload.service.api;

import com.infoclinika.mssharing.wizard.upload.model.UploadConfig;
import com.infoclinika.mssharing.wizard.upload.model.UploadFileItem;
import com.infoclinika.mssharing.wizard.upload.service.api.listener.UploadZipListener;

import java.util.List;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public interface UploadService {

    void upload(List<UploadFileItem> items, UploadConfig config, UploadZipListener listener);

    void upload(UploadFileItem item, UploadConfig config, UploadZipListener listener);
}
