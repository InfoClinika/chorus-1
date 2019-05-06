package com.infoclinika.mssharing.desktop.upload.gui.swing;

import com.infoclinika.mssharing.desktop.upload.gui.swing.util.FormLazyFactory;
import com.infoclinika.mssharing.desktop.upload.model.ConfigurationInfo;
import com.infoclinika.mssharing.desktop.upload.service.util.LoggerInitializer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * @author timofey.kasyanov
 *     date:   27.01.14
 */
public class DesktopUploaderApplicationStart implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopUploaderApplicationStart.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new DesktopUploaderApplicationStart());
    }

    private static void setStyle() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        setStyle();

        final ApplicationContext context
            = new ClassPathXmlApplicationContext("classpath:desktop_context_ui.xml");

        final ConfigurationInfo configurationInfo = context.getBean(ConfigurationInfo.class);
        final String zipFolderPath = configurationInfo.getZipFolderPath();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {

                try {

                    FileUtils.forceDelete(new File(zipFolderPath));

                } catch (Exception ex) {
                    LOGGER.error("Error while running the app", ex);
                }

            }
        });

        final LoggerInitializer loggerInitializer = context.getBean(LoggerInitializer.class);

        try {
            loggerInitializer.initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Java version: {}", System.getProperty("java.version"));

        final FormLazyFactory formLazyFactory = context.getBean(FormLazyFactory.class);

        formLazyFactory.getLoginForm().open();

    }

}
