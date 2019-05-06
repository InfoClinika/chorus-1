package com.infoclinika.mssharing.autoimporter.service.util;


import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.api.ProgressListener;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;

/**
 * author Ruslan Duboveckij
 */
public abstract class ProgressListenerFactory {
    protected abstract ProgressListener createProgressListener();

    public ProgressListener newProgressListener(UploadItem uploadItem, ObserverList<UploadItem> observer,
                                                String folder) {
        ProgressListener listener = createProgressListener();
        listener.init(uploadItem, observer, folder);
        return listener;
    }
}
