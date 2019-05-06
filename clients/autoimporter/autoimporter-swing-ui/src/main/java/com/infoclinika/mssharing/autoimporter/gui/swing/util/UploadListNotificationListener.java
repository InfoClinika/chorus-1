package com.infoclinika.mssharing.autoimporter.gui.swing.util;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.gui.swing.model.AppModel;
import com.infoclinika.mssharing.autoimporter.gui.swing.model.UploadModel;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.api.DefaultInitUtil;
import com.infoclinika.mssharing.autoimporter.service.impl.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * @author Ruslan Duboveckij
 */
@Component
public class UploadListNotificationListener implements NotificationListener, DefaultInitUtil<UploadModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadListNotificationListener.class);

    @Inject
    private AppModel appModel;
    private Optional<UploadModel> uploadModel = Optional.absent();

    @Override
    public void handleNotification(Notification notification, Object handback) {

        final String folder = notification.getMessage();

        if (appModel.getFolder().equals(folder) && uploadModel.isPresent()) {

            final NotificationType type = NotificationType.valueOf(notification.getType());

            if (type == NotificationType.CLEAR_ITEMS) {

                uploadModel.get().notify(type, null);

            } else {
                final UploadItem uploadItem = (UploadItem) notification.getUserData();

                uploadModel.get().notify(type, uploadItem);
            }


        }
    }

    @Override
    public void init(UploadModel uploadModel) {
        this.uploadModel = Optional.of(uploadModel);
    }
}
