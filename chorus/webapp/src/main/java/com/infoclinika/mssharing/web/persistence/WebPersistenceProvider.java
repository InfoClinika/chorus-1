package com.infoclinika.mssharing.web.persistence;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;

/**
 * @author Pavel Kaplin
 */
public abstract class WebPersistenceProvider {
    protected ConfigurableApplicationContext context;

    @PostConstruct
    public void createContext() {
        context = new ClassPathXmlApplicationContext("/webPersistence.cfg.xml", getDatabaseConfig());
    }

    protected abstract String getDatabaseConfig();

}
