package com.infoclinika.mssharing.autoimporter.gui.swing.util;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.gui.swing.model.AppModel;
import com.infoclinika.mssharing.autoimporter.gui.swing.model.WaitModel;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.service.api.DefaultInitUtil;
import com.infoclinika.mssharing.autoimporter.service.impl.NotificationType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * @author Ruslan Duboveckij
 */
@Component
public class WaitListNotificationListener implements NotificationListener, DefaultInitUtil<WaitModel> {
    @Inject
    private AppModel appModel;
    private Optional<WaitModel> waitModel = Optional.absent();

    @Override
    public void handleNotification(Notification notification, Object handback) {
        final String folder = notification.getMessage();

        if (appModel.getFolder().equals(folder)
            && waitModel.isPresent()
            && notification.getUserData() != null) {

            final NotificationType type = NotificationType.valueOf(notification.getType());
            final WaitItem waitItem = (WaitItem) notification.getUserData();

            if (type == NotificationType.ADD_ITEM) {

                waitModel.get().notifyAdd(waitItem);

            } else if (type == NotificationType.REMOVE_ITEM) {

                waitModel.get().notifyRemove(waitItem);

            }
        }
    }

    @Override
    public void init(WaitModel waitModel) {
        this.waitModel = Optional.of(waitModel);
    }
}
