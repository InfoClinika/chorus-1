package com.infoclinika.mssharing.autoimporter.service.impl;

import com.infoclinika.mssharing.autoimporter.service.api.AppEventNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.stereotype.Component;

import javax.management.Notification;

/**
 * @author timofey.kasyanov
 *     date: 08.05.2014
 */
@Component
@Qualifier("appEventNotifierImpl")
public class AppEventNotifierImpl implements AppEventNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppEventNotifierImpl.class);

    private NotificationPublisher notificationPublisher;

    @Override
    public void notify(AppEventCode appEventCode) {

        if (notificationPublisher != null) {

            LOGGER.debug(" *** Publish app event: {}", appEventCode);

            final Notification notification = new Notification("", this, 0);
            notification.setUserData(appEventCode);

            notificationPublisher.sendNotification(notification);
        }

    }

    @Override
    public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }
}
