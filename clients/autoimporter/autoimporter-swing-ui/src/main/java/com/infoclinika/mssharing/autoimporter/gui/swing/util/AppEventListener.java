package com.infoclinika.mssharing.autoimporter.gui.swing.util;

import com.infoclinika.mssharing.autoimporter.messages.MessageKey;
import com.infoclinika.mssharing.autoimporter.messages.MessagesSource;
import com.infoclinika.mssharing.autoimporter.service.api.AppEventNotifier.AppEventCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * @author timofey.kasyanov
 *     date: 08.05.2014
 */
@Component
public class AppEventListener implements NotificationListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(AppEventListener.class);

    @Override
    public void handleNotification(Notification notification, Object handback) {

        LOGGER.debug(" *** Handle notification");

        if (notification.getUserData() != null) {

            final AppEventCode eventCode = (AppEventCode) notification.getUserData();

            if (eventCode == AppEventCode.UPLOAD_LIMIT_ERROR) {
                FormUtils.showError(MessagesSource.getMessage(MessageKey.APP_ERROR_UPLOAD_LIMIT_EXCEEDED));
            }

        }

    }
}
