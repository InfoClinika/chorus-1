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
package com.infoclinika.mssharing.model.helper;

import com.infoclinika.mssharing.model.AdminNotifier;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.internal.RuleValidatorImpl;
import com.infoclinika.mssharing.model.internal.jira.JiraRestClientProvider;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.StoredObject;
import com.infoclinika.mssharing.platform.fileserver.impl.InMemoryStorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultFileUploadManagement;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import javax.inject.Named;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @author Stanislav Kurilin
 */
@Configuration
@ImportResource({
    "test.cfg.xml",
    "persistence.cfg.xml",
    "h2.cfg.xml",
    "workflow-test.cfg.xml"
})
@ComponentScan(
    basePackages = {
        "com.infoclinika.mssharing.model",
        "com.infoclinika.mssharing.platform",
        "com.infoclinika.mssharing.propertiesprovider"
    },
    excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.*DefaultRuleValidator*"),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = DefaultFileUploadManagement.class)})
public class SpringConfig {

    public static final String ARCHIVE_ID = "archiveId";

    @Bean()
    public StorageService inMemoryStorage() {
        final InMemoryStorageService mock = Mockito.mock(InMemoryStorageService.class);
        doCallRealMethod().when(mock).put(Matchers.<NodePath>anyObject(), Matchers.<StoredObject>anyObject());
        doCallRealMethod().when(mock).get(Matchers.<NodePath>anyObject());
        doCallRealMethod().when(mock).delete(Matchers.<NodePath>anyObject());
        return mock;
    }

    @Bean
    @Scope("prototype")
    @Named("current")
    public Date current() {
        return new Date();
    }

    @Bean
    public Repositories repositories() {
        return new Repositories();
    }

    @Bean
    public RuleValidator ruleValidator() {
        return new RuleValidatorImpl();
    }

    @Bean
    public WriteServices writeServices() {
        return new WriteServices();
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

    @Bean
    public PasswordEncoder encoder() {
        return new StandardPasswordEncoder();
    }

    @Primary
    @Bean
    public JiraRestClientProvider getJiraRestClientProvider() {
        return mock(JiraRestClientProvider.class);
    }

    @Bean(name = "billingService")
    public BillingService billingService() {
        BillingService billingService = mock(BillingService.class);
        return billingService;
    }

    @Bean
    public FileArchivingHelper fileArchivingHelper() {
        final FileArchivingHelper mock = mock(FileArchivingHelper.class);
        when(mock.isArchiveReadyToRestore(anyString())).thenReturn(true);
        when(mock.moveToAnalyzableStorage(anyString())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return "contentId" + UUID.randomUUID().toString();
            }
        });
        when(mock.moveToArchiveStorage(anyString())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ARCHIVE_ID + UUID.randomUUID().toString();
            }
        });
        when(mock.requestUnarchive(anyString(), anyBoolean())).thenReturn(true);
        return mock;
    }

    @Bean
    public TimeZone timeZone() {
        return TimeZone.getDefault();
    }

}
