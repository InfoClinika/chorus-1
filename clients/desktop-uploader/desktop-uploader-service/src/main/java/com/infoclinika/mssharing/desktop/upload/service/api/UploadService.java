package com.infoclinika.mssharing.desktop.upload.service.api;

import com.infoclinika.mssharing.desktop.upload.model.UploadConfig;
import com.infoclinika.mssharing.desktop.upload.model.UploadFileItem;
import com.infoclinika.mssharing.desktop.upload.service.api.listener.UploadZipListener;

import java.util.List;

/**
 * @author timofey.kasyanov
 *     date:   28.01.14
 */
public interface UploadService {

    void upload(List<UploadFileItem> items, UploadConfig config, UploadZipListener listener);

    void pause(UploadConfig uploadConfig);

    void resume(UploadConfig uploadConfig);
}
