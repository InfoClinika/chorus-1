package com.infoclinika.sso.model.test.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;

import javax.inject.Inject;

/**
 * @author andrii.loboda
 */
@Configuration
@ImportResource("test-applicationContext.cfg.xml")
@ContextConfiguration(classes = AbstractTest.class)
public abstract class AbstractTest extends AbstractTestNGSpringContextTests {
    @Inject
    private Repositories repositories;

    @AfterMethod
    public void tearDown() {
        for (CrudRepository repository : repositories.get()) {
            repository.deleteAll();
        }
    }

}
