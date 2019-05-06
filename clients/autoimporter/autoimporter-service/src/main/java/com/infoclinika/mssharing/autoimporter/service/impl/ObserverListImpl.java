package com.infoclinika.mssharing.autoimporter.service.impl;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.stereotype.Service;

import javax.management.Notification;
import java.io.Serializable;

/**
 * @author Ruslan Duboveckij
 */
@Service
@Qualifier("observerListImpl")
public class ObserverListImpl implements ObserverList<WaitItem>, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObserverListImpl.class);

    private Optional<NotificationPublisher> notificationPublisher = Optional.absent();

    public static <T> void publish(Optional<NotificationPublisher> notificationPublisher,
                                   Object source,
                                   String type,
                                   String folder,
                                   T t) {

        if (notificationPublisher.isPresent()) {

            final Notification add = new Notification(type, source, 0, folder);

            add.setUserData(t);
            notificationPublisher.get().sendNotification(add);

            LOGGER.debug("Publish notification type= {} folder= {} object={}", type, folder, t);

        }
    }

    @Override
    public void notify(NotificationType type, String folder, WaitItem item) {
        publish(notificationPublisher, this, type.name(), folder, item);
    }

    @Override
    public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = Optional.of(notificationPublisher);
    }
}
