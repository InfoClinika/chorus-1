package com.infoclinika.mssharing.web.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;

/**
 * @author Pavel Kaplin
 */
@ContextConfiguration(locations = "classpath:testApplicationContext.cfg.xml")
@Configuration
@WebAppConfiguration
public class SpringSupportTest extends AbstractTestNGSpringContextTests {
    /**
     * inspired by http://stackoverflow.com/a/3522070/1338758
     */
    @BeforeMethod
    public void setUpContext() throws Exception {
        //this is where the magic happens, we actually do "by hand" what the spring runner would do for us,
        // read the JavaDoc for the class bellow to know exactly what it does, the method names are quite accurate
        // though
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
    }
}
