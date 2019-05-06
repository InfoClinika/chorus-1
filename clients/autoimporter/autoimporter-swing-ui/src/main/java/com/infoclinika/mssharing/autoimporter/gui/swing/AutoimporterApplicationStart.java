package com.infoclinika.mssharing.autoimporter.gui.swing;

import com.infoclinika.mssharing.autoimporter.gui.swing.util.*;
import com.infoclinika.mssharing.autoimporter.service.LoggerInitializer;
import com.infoclinika.mssharing.autoimporter.service.api.internal.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.management.MBeanServerConnection;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.swing.*;
import java.io.IOException;

public class AutoimporterApplicationStart extends NotificationBroadcasterSupport implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoimporterApplicationStart.class);

    public static void main(String[] args) {

        final String javaVersion = System.getProperty("java.version");

        LOGGER.info("Java version: {}", javaVersion);
        final float version = Float.parseFloat(javaVersion.substring(0, 3));

        if (version < 1.7) {

            LOGGER.info("The application requires at least JDK 1.7. Current version is {}", javaVersion);
            LOGGER.info("Application will exit now");

            return;
        }

        SwingUtilities.invokeLater(new AutoimporterApplicationStart());
    }

    private static void setStyle() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.error(String.valueOf(e));
        }
    }

    @Override
    public void run() {
        setStyle();

        try {

            final ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:autoimporter_context_ui.xml");

            final LoggerInitializer loggerInitializer = context.getBean(LoggerInitializer.class);

            try {

                loggerInitializer.initialize();

            } catch (IOException ex) {
                LOGGER.error(String.valueOf(ex));
            }

            final MBeanServerConnection connection = (MBeanServerConnection) context.getBean("clientConnector");

            final NotificationListener notificationWaitListener = context.getBean(WaitListNotificationListener.class);
            final ObjectName waitObjectName = ObjectName.getInstance("bean:name=waitObserverList");
            connection.addNotificationListener(waitObjectName, notificationWaitListener, null, null);

            final NotificationListener notificationUploadListener =
                context.getBean(UploadListNotificationListener.class);
            final ObjectName objectName = ObjectName.getInstance("bean:name=uploadObserverList");
            connection.addNotificationListener(objectName, notificationUploadListener, null, null);

            final NotificationListener notificationDuplicateListener =
                context.getBean(DuplicateNotificationListener.class);
            final ObjectName duplicateObjectName = ObjectName.getInstance("bean:name=duplicateObserverList");
            connection.addNotificationListener(duplicateObjectName, notificationDuplicateListener, null, null);

            final NotificationListener appEventListener = context.getBean(AppEventListener.class);
            final ObjectName appEventObjectName = ObjectName.getInstance("bean:name=appEventPublisher");
            connection.addNotificationListener(appEventObjectName, appEventListener, null, null);

            final FrameLazyFactory frameLazyFactory = context.getBean(FrameLazyFactory.class);
            final UploadService uploadServiceProxy = context.getBean("uploadServiceProxy", UploadService.class);

            try {

                final boolean authorized = uploadServiceProxy.readAuthorization();

                if (authorized) {

                    frameLazyFactory.getMainForm().open();

                } else {

                    frameLazyFactory.getLoginForm().open();

                }

            } catch (Exception ex) {
                FormUtils.logError(ex);
                frameLazyFactory.getLoginForm().open();
            }
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }

    }
}
