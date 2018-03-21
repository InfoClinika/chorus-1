package com.infoclinika.mssharing.wizard.upload.service.api;

import com.infoclinika.mssharing.wizard.upload.model.ZipConfig;
import com.infoclinika.mssharing.wizard.upload.model.ZipFileItem;
import com.infoclinika.mssharing.wizard.upload.service.api.listener.ZipListener;

/**
 * @author timofey.kasyanov
 *         date:   29.01.14
 */
public interface ZipService {

    void zip(ZipFileItem item, ZipConfig config, ZipListener listener);

}
