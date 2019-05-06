package com.infoclinika.mssharing.autoimporter.service.impl;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * @author Ruslan Duboveckij
 */
@Service
@Qualifier("observerUploadListImpl")
public class ObserverUploadListImpl implements ObserverList<UploadItem>, Serializable {

    private Optional<NotificationPublisher> notificationPublisher = Optional.absent();

    @Override
    public void notify(NotificationType type, String folder, UploadItem item) {
        ObserverListImpl.publish(notificationPublisher, this, type.name(), folder, item);
    }

    @Override
    public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = Optional.of(notificationPublisher);
    }
}
