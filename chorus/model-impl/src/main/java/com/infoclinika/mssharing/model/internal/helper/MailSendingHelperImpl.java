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
package com.infoclinika.mssharing.model.internal.helper;

import com.infoclinika.mssharing.model.helper.MailSendingHelper;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate.UserDetails;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultMailSendingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Stanislav Kurilin
 */
@Service("mailSendingHelper")
public class MailSendingHelperImpl extends DefaultMailSendingHelper<UserDetails> implements MailSendingHelper {


    private static final Logger LOGGER = LoggerFactory.getLogger(MailSendingHelperImpl.class);

    @Inject
    private ExperimentRepository experimentRepository;

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public String fileName(long fileMetaDataId) {
        return fileMetaDataRepository.findOne(fileMetaDataId).getName();
    }

    @Override
    public ExperimentDetails experimentDetails(long experiment) {
        final ActiveExperiment ex = experimentRepository.findOne(experiment);
        return new ExperimentDetails(ex.getName(), ex.getCreator().getEmail());
    }

    @Override
    public boolean isSkipSending(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email)).map(User::isSkipEmailsSending).orElseGet(() -> {
            LOGGER.warn("User not found by email, return default value 'false': " + email);
            return false;
        });

    }

    @Override
    public UserDetails transform(UserTemplate userTemplate) {
        return transformDefault(userTemplate);
    }
}
