package com.infoclinika.mssharing.desktop.upload.service.api.listener;

import com.infoclinika.mssharing.desktop.upload.model.ZipFileItem;

/**
 * @author timofey.kasyanov
 *     date:   28.01.14
 */
public interface ZipListener {

    void onZipStart(ZipFileItem item);

    void zipProgressChanged(ZipFileItem item, long bytes);

    void onZipComplete(ZipFileItem item);

    void onZipError(ZipFileItem item);

}
