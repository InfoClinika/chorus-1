package com.infoclinika.mssharing.platform.model.mailing;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Pavel Kaplin
 */
@Configuration
public class VelocityEngineProvider {

    @Bean
    public VelocityEngine getVelocityEngine() throws IOException, VelocityException {
        VelocityEngineFactory factory = new VelocityEngineFactory();
        Properties properties = new Properties();
        properties.put(Velocity.RESOURCE_LOADER, "class");
        properties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        factory.setVelocityProperties(properties);
        return factory.createVelocityEngine();
    }

}
