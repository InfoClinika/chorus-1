package com.infoclinika.mssharing.autoimporter.service.util;

import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.clients.common.web.impl.WebServiceImpl;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.inject.Inject;

/**
 * author Ruslan Duboveckij
 */
@Configuration
@ImportResource({"classpath:autoimporter-context.xml"})
public class SpringConfig {
    @Inject
    private ApplicationContext context;

    @Bean
    public WebService webService() {
        return Mockito.mock(WebService.class);
    }

    @Bean
    public WebService webServiceReal() {
        return context.getBean(WebServiceImpl.class);
    }
}
