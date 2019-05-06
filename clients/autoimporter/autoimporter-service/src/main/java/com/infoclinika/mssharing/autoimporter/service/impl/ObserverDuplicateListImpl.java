package com.infoclinika.mssharing.autoimporter.service.impl;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * @author timofey.kasyanov
 *     date:   21.01.14
 */
@Service
@Qualifier("observerDuplicateListImpl")
public class ObserverDuplicateListImpl implements ObserverList<DuplicateItem>, Serializable {

    private Optional<NotificationPublisher> notificationPublisher = Optional.absent();

    @Override
    public void notify(NotificationType type, String folder, DuplicateItem item) {
        ObserverListImpl.publish(notificationPublisher, this, type.name(), folder, item);
    }

    @Override
    public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = Optional.of(notificationPublisher);
    }

}
