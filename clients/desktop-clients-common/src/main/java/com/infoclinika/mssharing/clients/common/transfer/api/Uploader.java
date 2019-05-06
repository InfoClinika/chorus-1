package com.infoclinika.mssharing.clients.common.transfer.api;

import com.amazonaws.event.ProgressListener;
import com.infoclinika.mssharing.clients.common.transfer.impl.UploadItem;

/**
 * @author timofey.kasyanov
 *     date: 24.02.14.
 */
public interface Uploader {

    boolean upload(UploadItem item, ProgressListener listener);

    boolean cancel();

    boolean isCanceled();

}
