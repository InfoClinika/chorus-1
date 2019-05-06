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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.platform.entity.ChangeEmailRequest;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultSecurityHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Stanislav Kurilin
 */
@Service("securityHelper")
public class SecurityHelperImpl extends DefaultSecurityHelper<User, SecurityHelper.UserDetails>
    implements SecurityHelper {

    @Inject
    private FeaturesHelper featuresHelper;

    private static final Function<User, SecurityHelper.UserDetails> USER_DETAILS_FROM_USER = input -> {
        if (input == null) {
            return null;
        }
        final ImmutableSet<Long> labIds = from(input.getLabs()).transform(EntityUtil.ENTITY_TO_ID).toSet();
        final ChangeEmailRequest changeEmailRequest = input.getChangeEmailRequest();
        final String newEmail = changeEmailRequest != null ? changeEmailRequest.getEmail() : null;
        final boolean labHead = input.getLabs().stream().anyMatch(lab -> lab.getHead().equals(input));
        return new SecurityHelper.UserDetails(
            input.getId(),
            input.getPersonData().getFirstName(),
            input.getPersonData().getLastName(),
            input.getEmail(),
            input.getPasswordHash(),
            input.isAdmin(),
            input.isEmailVerified(),
            newEmail,
            input.getLastModification(),
            labIds,
            labHead,
            input.getSecretToken(),
            input.getEmailVerificationSentOnDate(),
            input.getPasswordResetSentOnDate(),
            input.isLocked()
        );
    };

    @Override
    public boolean isBillingEnabledForLab(long lab) {
        return featuresHelper.isEnabledForLab(ApplicationFeature.BILLING, lab);
    }

    @Override
    public boolean isFeatureEnabledForLab(String feature, long lab) {
        return featuresHelper.isEnabledForLab(ApplicationFeature.valueOf(feature), lab);
    }

    @Override
    public SecurityHelper.UserDetails transform(User user) {
        return USER_DETAILS_FROM_USER.apply(user);
    }

    @Nullable
    @Override
    public SecurityHelper.UserDetails getUserDetailsByEmailAndSecretToken(String email, String userSecretKey) {
        final SecurityHelper.UserDetails userDetails = getUserDetailsByEmail(email);
        if (userDetails != null && userSecretKey != null && userSecretKey.equals(userDetails.secretToken)) {
            return userDetails;
        }

        return null;
    }
}

