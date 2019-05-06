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
package com.infoclinika.mssharing.platform.model.test.helper;

import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.impl.InMemoryStorageService;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.impl.DefaultRuleValidator;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;

import java.util.TimeZone;

/**
 * @author Stanislav Kurilin
 */
@Configuration
@PropertySource("file:../../configs/test-application.properties")
@ImportResource({"platform.persistence.cfg.xml", "platform.h2.cfg.xml", "platform.cfg.xml", "platform.test.cfg.xml"})
@ComponentScan(basePackages = {"com.infoclinika.mssharing.platform"})
public class SpringConfig {

    @Bean
    public StorageService inMemoryStorage() {
        final InMemoryStorageService mock = Mockito.mock(InMemoryStorageService.class);
        Mockito.doCallRealMethod().when(mock).put(Matchers.anyObject(), Matchers.anyObject());
        Mockito.doCallRealMethod().when(mock).get(Matchers.anyObject());
        Mockito.doCallRealMethod().when(mock).delete(Matchers.anyObject());
        return mock;
    }


    @Bean
    public RuleValidator ruleValidator() {
        return new DefaultRuleValidator<>();
    }

    @Bean
    public TimeZone timeZone() {
        return TimeZone.getDefault();
    }

    @Bean
    public EntityFactories entityFactories() {
        return new EntityFactories.Builder().build();
    }

    @Bean
    public PagedItemsTransformerTemplate pagedItemsTransformer() {
        return new PagedItemsTransformerTemplate();
    }

}
