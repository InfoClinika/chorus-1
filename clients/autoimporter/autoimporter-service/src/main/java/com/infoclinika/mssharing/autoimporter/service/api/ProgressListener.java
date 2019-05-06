package com.infoclinika.mssharing.autoimporter.service.api;

import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;

/**
 * author Ruslan Duboveckij
 */
public interface ProgressListener extends com.amazonaws.event.ProgressListener {
    void init(UploadItem uploadItem, ObserverList<UploadItem> observer, String watchFolder);
}
