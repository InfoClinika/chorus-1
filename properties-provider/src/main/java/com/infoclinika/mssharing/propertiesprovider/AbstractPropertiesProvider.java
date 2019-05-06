package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import javax.inject.Inject;
import java.io.IOException;

import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

public abstract class AbstractPropertiesProvider {

    @Inject
    private ConfigurableEnvironment env;

    @Inject
    private ApplicationContext applicationContext;

    public void setLocation(String location) {
        try {
            env.getPropertySources().addFirst(new ResourcePropertySource(location));

            final AutowireCapableBeanFactory autowireBeanFactory = applicationContext.getAutowireCapableBeanFactory();
            autowireBeanFactory.autowireBeanProperties(this, AUTOWIRE_BY_NAME, true);
        } catch (IOException e) {
            throw new RuntimeException("Can't read properties file: " + location, e);
        }
    }
}
