package com.infoclinika.mssharing.web.security;

import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.infoclinika.mssharing.web.security.ApplicationPropertiesReader.SSO_ENABLED_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Andrii Loboda
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class ApplicationPropertiesReaderShouldTest {

    @SuppressWarnings("AccessOfSystemProperties")
    @BeforeClass
    public static void setUp() {
        final String userDirPropertiesPath = Resources.getResource("userdir/" + ApplicationPropertiesReader.APPLICATION_PROPERTIES).getFile();
        System.setProperty("user.dir", new File(userDirPropertiesPath).getParentFile().getAbsolutePath());

        final String userHomePropertiesPath = Resources.getResource("userhome/" + ApplicationPropertiesReader.APPLICATION_PROPERTIES).getFile();
        System.setProperty("user.home", new File(userHomePropertiesPath).getParentFile().getAbsolutePath());
    }

    @Test
    public void get_property_from_classpath() {
        final String propValue = ApplicationPropertiesReader.getProperty(SSO_ENABLED_PROPERTY);
        assertNotNull(propValue);
    }

    @Test
    public void get_property_from_userdir() {

        final String propValue = ApplicationPropertiesReader.getProperty("test.property-key-1");
        assertEquals(propValue, "userdir.test.property-key-1-value");
    }

    @Test
    public void get_property_from_userhome() {

        final String propValue = ApplicationPropertiesReader.getProperty("test.property-key-3");
        assertEquals(propValue, "userhome.property-key-3-value");
    }
}
