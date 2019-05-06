package com.infoclinika.mssharing.desktop.upload.model;

/**
 * @author timofey.kasyanov
 *     date:   29.01.14
 */
public enum UploadStatus {

    WAITING,
    ZIPPING,
    ZIP_COMPLETE,
    UPLOADING,
    UPLOAD_COMPLETE,
    DUPLICATE,
    ERROR,
    UPLOAD_UNAVAILABLE

}
