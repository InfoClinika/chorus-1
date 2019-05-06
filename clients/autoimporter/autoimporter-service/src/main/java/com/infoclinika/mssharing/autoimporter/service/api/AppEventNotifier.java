package com.infoclinika.mssharing.autoimporter.service.api;

import org.springframework.jmx.export.notification.NotificationPublisherAware;

/**
 * @author timofey.kasyanov
 *     date: 08.05.2014
 */
public interface AppEventNotifier extends NotificationPublisherAware {

    enum AppEventCode {
        UPLOAD_LIMIT_ERROR
    }

    void notify(AppEventCode appEventCode);

}
