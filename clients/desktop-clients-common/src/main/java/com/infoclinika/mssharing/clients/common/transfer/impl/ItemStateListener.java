package com.infoclinika.mssharing.clients.common.transfer.impl;

/**
 * @author timofey.kasyanov
 *     14.03.14
 */
public interface ItemStateListener {

    void stateChanged(MultipartUpload multipartUpload, UploadItemState state);

}
