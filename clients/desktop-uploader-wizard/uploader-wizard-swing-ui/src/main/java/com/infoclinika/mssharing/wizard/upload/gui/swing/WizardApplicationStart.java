package com.infoclinika.mssharing.wizard.upload.gui.swing;

import com.infoclinika.mssharing.wizard.upload.gui.swing.util.FormLazyFactory;
import com.infoclinika.mssharing.wizard.upload.model.ConfigurationInfo;
import com.infoclinika.mssharing.wizard.upload.service.util.LoggerInitializer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * @author timofey.kasyanov
 *         date:   27.01.14
 */
public class WizardApplicationStart implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(WizardApplicationStart.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new WizardApplicationStart());
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
                = new ClassPathXmlApplicationContext("classpath:wizard_context_ui.xml");

        final ConfigurationInfo configurationInfo = context.getBean(ConfigurationInfo.class);
        final String zipFolderPath = configurationInfo.getZipFolderPath();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {

                try {

                    FileUtils.forceDelete(new File(zipFolderPath));

                } catch (Exception ex) {

                }

            }
        });

        final LoggerInitializer loggerInitializer = context.getBean(LoggerInitializer.class);

        try {
            loggerInitializer.initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Java version: " + System.getProperty("java.version"));

        final FormLazyFactory formLazyFactory = context.getBean(FormLazyFactory.class);

        formLazyFactory.getLoginForm().open();

    }

}
