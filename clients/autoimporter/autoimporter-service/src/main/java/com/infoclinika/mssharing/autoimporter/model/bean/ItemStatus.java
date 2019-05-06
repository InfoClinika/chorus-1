package com.infoclinika.mssharing.autoimporter.model.bean;

/**
 * @author timofey.kasyanov
 *     date: 17.02.14.
 */
public enum ItemStatus {

    WAITING,
    ZIPPING,
    UPLOADING,
    COMPLETE,
    CANCELED,
    UPLOAD_UNAVAILABLE,
    SIZE_MISMATCH,
    ERROR,
    RETRYING

}
