package com.infoclinika.mssharing.web.security;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Loads properties from application.properties in the following order: classpath, user.home, user.dir
 *
 * @author Andrii Loboda
 */
@SuppressWarnings("UtilityClass")
public class ApplicationPropertiesReader {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertiesReader.class);

    private static final String USER_HOME = "user.home";
    private static final String USER_DIR = "user.dir";

    private static final Properties properties = loadProperties();

    /*package*/ static final String APPLICATION_PROPERTIES = "application.properties";
    /* The property specified in application.properties */
    public static final String SSO_ENABLED_PROPERTY = "chorus.sso";

    private ApplicationPropertiesReader() {
    }

    /*package*/
    static String getProperty(String key) {
        return properties.getProperty(key);
    }

    @SuppressWarnings("AccessOfSystemProperties")
    private static Properties loadProperties() {
        final Properties properties = new Properties();

        final URL classpathProperties = Resources.getResource(APPLICATION_PROPERTIES);
        loadProperties(properties, classpathProperties);

        final File userHomeProperties = new File(System.getProperty(USER_HOME), APPLICATION_PROPERTIES);
        loadPropertiesFromFile(properties, userHomeProperties);

        final File userDirProperties = new File(System.getProperty(USER_DIR), APPLICATION_PROPERTIES);
        loadPropertiesFromFile(properties, userDirProperties);
        return properties;

    }

    private static void loadPropertiesFromFile(Properties properties, File file) {
        try {
            final URL userHomeProperties = file.toURI().toURL();
            loadProperties(properties, userHomeProperties);
        } catch (MalformedURLException ignored) {
            LOG.error("Failed to load properties from file: " + file);
        }
    }

    private static void loadProperties(Properties properties, URL propertiesUrl) {
        LOG.info("Loading properties from " + propertiesUrl);

        final ByteSource byteSource = Resources.asByteSource(propertiesUrl);

        try (final InputStream inputStream = byteSource.openBufferedStream()) {

            properties.load(inputStream);
        } catch (IOException ignored) {
            LOG.error("Failed to load properties from: " + propertiesUrl);
        }
    }
}
