package com.infoclinika.mssharing.autoimporter.os.service;

import com.infoclinika.mssharing.autoimporter.service.LoggerInitializer;
import com.infoclinika.mssharing.autoimporter.service.api.internal.UploadService;
import com.infoclinika.mssharing.autoimporter.service.util.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;

/**
 * @author Ruslan Duboveckij
 */
public class Driver {
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String JAVA_VERSION = "java.version";
    private static final double JAVA_7 = 1.7;
    private static Logger LOGGER = LoggerFactory.getLogger(Driver.class);

    private static volatile Thread startThread;

    public static void start(String[] args) throws Exception {

        final String javaVersion = System.getProperty(JAVA_VERSION);

        LOGGER.info("Java version: {}", javaVersion);
        final float version = Float.parseFloat(javaVersion.substring(0, 3));

        if (version < JAVA_7) {
            LOGGER.info("The application requires JDK 1.7.x. Current version is {}", javaVersion);
            LOGGER.info("Application will exit now");
            return;
        }

        final ClassPathXmlApplicationContext context =
            new ClassPathXmlApplicationContext("classpath:context_service.xml");
        final Configuration configuration = context.getBean(Configuration.class);
        final String zipFolderPath = configuration.getZipFolderPath();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    LOGGER.info("Deleting zip folder: {}", zipFolderPath);
                    FileUtils.forceDelete(new File(zipFolderPath));
                } catch (Exception ex) {
                    LOGGER.info("Cannot delete zip folder: {}", zipFolderPath);
                }
            }
        });

        final LoggerInitializer loggerInitializer = context.getBean(LoggerInitializer.class);
        try {
            loggerInitializer.initialize();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        final UploadService uploadService = context.getBean(UploadService.class);
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("disableFileChecking")) {
                LOGGER.info("Raw file checking is disabled by user");
                uploadService.disableFileChecking();
            }
        }
        try {
            LOGGER.info("Service reads authorization");
            uploadService.readAuthorization();
            LOGGER.info("Service has read authorization successfully");
        } catch (Exception ex) {
            LOGGER.info("Error on service startup, trying to read authorization.");
            LOGGER.info(String.valueOf(ex));
        }

        LOGGER.info("Service started");

        startThread = Thread.currentThread();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception ex) {
            LOGGER.error("Exceprion during thread sleep:", ex);
        }

        LOGGER.info("Service stopped");
    }

    public static void stop(String[] args) {
        LOGGER.info("Interrupt start thread.");
        startThread.interrupt();
        LOGGER.info(STOP);
    }

    public static void main(String[] args) throws Exception {

        LOGGER.info("Java version: {}", System.getProperty(JAVA_VERSION));

        String mode = args[0];
        if (mode.equals(START)) {
            start(args);
        } else if (mode.equals(STOP)) {
            stop(args);
        }
    }
}
