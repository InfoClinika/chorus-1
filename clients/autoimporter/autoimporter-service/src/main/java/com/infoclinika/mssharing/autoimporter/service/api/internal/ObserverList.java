package com.infoclinika.mssharing.autoimporter.service.api.internal;

import com.infoclinika.mssharing.autoimporter.service.impl.NotificationType;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

/**
 * author Ruslan Duboveckij
 */
public interface ObserverList<T> extends NotificationPublisherAware {

    void notify(NotificationType type, String folder, T item);

}
