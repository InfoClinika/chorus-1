package com.infoclinika.mssharing.web.demo;

import org.junit.Before;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

/**
 * @author Pavel Kaplin
 */
@ContextConfiguration(locations = "classpath:testApplicationContext.cfg.xml")
@Configuration
public class SpringSupportTest {
    /**
     * inspired by http://stackoverflow.com/a/3522070/1338758
     */
    @Before
    public void setUpContext() throws Exception {
        //this is where the magic happens, we actually do "by hand" what the spring runner would do for us,
        // read the JavaDoc for the class bellow to know exactly what it does, the method names are quite accurate though
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
    }

}
