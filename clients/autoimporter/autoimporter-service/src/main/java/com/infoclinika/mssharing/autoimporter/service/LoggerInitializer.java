package com.infoclinika.mssharing.autoimporter.service;

import com.infoclinika.mssharing.autoimporter.service.util.Configuration;
import org.apache.log4j.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author timofey.kasyanov
 *     date: 14.02.14.
 */
@Component
public class LoggerInitializer {

    private static final String PATTERN_LAYOUT = "%d{DATE} %5p %t %c{2}:%L - %m%n";
    private static final int MAX_BACKUP_INDEX = 10;
    private static final String MAX_FILE_SIZE = "10MB";

    @Inject
    private Configuration configuration;

    public void initialize() throws IOException {

        final RollingFileAppender appender = new RollingFileAppender();
        final Layout layout = new PatternLayout(PATTERN_LAYOUT);
        final String logFilePath = configuration.getLogFilePath();

        appender.setLayout(layout);
        appender.setFile(logFilePath, true, true, 8192);
        appender.setAppend(true);
        appender.setThreshold(Level.ALL);
        appender.setMaxBackupIndex(MAX_BACKUP_INDEX);
        appender.setMaxFileSize(MAX_FILE_SIZE);
        appender.setImmediateFlush(true);

        BasicConfigurator.configure(appender);

    }

}
