/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.search;

import com.infoclinika.mssharing.model.AdminNotifier;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.Searcher;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.RuleValidatorImpl;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.impl.InMemoryStorageService;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultFileUploadManagement;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import javax.inject.Named;
import java.util.Date;
import java.util.TimeZone;

import static org.mockito.Mockito.mock;

/**
 * @author Stanislav Kurilin
 */
@Configuration
@ImportResource({
        "test.cfg.xml",
        "persistence.cfg.xml",
        "h2.cfg.xml"
})
@ComponentScan(
        basePackages = {
                "com.infoclinika.mssharing.model",
                "com.infoclinika.mssharing.platform"
        },
        excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.*DefaultRuleValidator*"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = DefaultFileUploadManagement.class)})
class SpringConfig {

    @Bean
    @Scope("prototype")
    @Named("current")
    public Date current() {
        return new Date();
    }

    @Bean
    public StorageService inMemoryStorage() {
        return new InMemoryStorageService();
    }

    @Primary
    @Bean
    public Notifier notificator() {
        return mock(Notifier.class);
    }

    @Bean
    public AdminNotifier adminNotifier() {
        return mock(AdminNotifier.class);
    }

    @Bean(name = "billingService")
    public BillingService billingService() {
        BillingService billingService = mock(BillingService.class);
        return billingService;
    }

    @Bean
    public Searcher searcher() {
        return new SearcherImpl();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new StandardPasswordEncoder();
    }

    @Bean
    public RuleValidator ruleValidator() {
        return new RuleValidatorImpl();
    }

    @Bean
    public TimeZone timeZone() {
        return TimeZone.getDefault();
    }

}
