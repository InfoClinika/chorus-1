package com.infoclinika.mssharing.desktop.upload.service.api;

import com.infoclinika.mssharing.desktop.upload.model.ZipConfig;
import com.infoclinika.mssharing.desktop.upload.model.ZipFileItem;
import com.infoclinika.mssharing.desktop.upload.service.api.listener.ZipListener;

/**
 * @author timofey.kasyanov
 *     date:   29.01.14
 */
public interface ZipService {

    void zip(ZipFileItem item, ZipConfig config, ZipListener listener);

}
