package com.infoclinika.mssharing.autoimporter.gui.swing.util;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.gui.swing.model.AppModel;
import com.infoclinika.mssharing.autoimporter.gui.swing.model.DuplicateModel;
import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;
import com.infoclinika.mssharing.autoimporter.service.api.DefaultInitUtil;
import com.infoclinika.mssharing.autoimporter.service.impl.NotificationType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * @author timofey.kasyanov
 *     date:   21.01.14
 */
@Component
public class DuplicateNotificationListener implements NotificationListener, DefaultInitUtil<DuplicateModel> {

    @Inject
    private AppModel appModel;
    private Optional<DuplicateModel> duplicateModel = Optional.absent();

    @Override
    public void handleNotification(Notification notification, Object handback) {

        final String folder = notification.getMessage();

        if (appModel.getFolder().equals(folder)
            && duplicateModel.isPresent()
            && notification.getUserData() != null) {

            final NotificationType type = NotificationType.valueOf(notification.getType());

            final DuplicateItem duplicateItem = (DuplicateItem) notification.getUserData();

            if (type == NotificationType.ADD_ITEM) {

                duplicateModel.get().notifyAdd(duplicateItem);

            } else if (type == NotificationType.REMOVE_ITEM) {

                duplicateModel.get().notifyRemove(duplicateItem);

            }

        }
    }

    @Override
    public void init(DuplicateModel duplicateModel) {
        this.duplicateModel = Optional.of(duplicateModel);
    }

}
