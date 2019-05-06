/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.testing.helper;

import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

/**
 * @author Stanislav Kurilin
 */
@Configuration
@PropertySource("file:../../configs/test-application.properties")
@ComponentScan(basePackages = {"com.infoclinika.mssharing.platform"})
public class SpringConfigTemplate {

    @Bean
    public Repositories repositories() {
        return new Repositories();
    }

    @Bean
    public WriteServices writeServices() {
        return new WriteServices();
    }

    @Bean
    public NotifierTemplate notificator() {
        return Mockito.mock(NotifierTemplate.class);
    }

    @Bean
    public PasswordEncoder encoder() {
        return new StandardPasswordEncoder();
    }

}
