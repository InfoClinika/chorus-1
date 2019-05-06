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
package com.infoclinika.mssharing.model.write;

import com.google.common.annotations.VisibleForTesting;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;

import java.time.Duration;
import java.util.Date;
import java.util.Set;

/**
 * Handles Organizational structure changes operations. With include Users and Labs.
 * <p/>
 * Each user works in laboratory. On user creation he should select one of existing or send request to create new.
 * Requests should be handled by admins. Admins is role for users.
 * <p/>
 * Since we can not create admin without lab and can not create lab without admin,
 * some predefined admin should exist in system.
 *
 * @author Stanislav Kurilin
 */
public interface UserManagement extends UserManagementTemplate {

    long createPersonAndApproveMembership(PersonInfo user, String password, Long lab, String emailVerificationUrl);

    void updatePersonAndApproveMembership(long userId, PersonInfo user, Set<Long> labs);

    void changeFirstName(long userId, String newFirstName);

    void changeLastName(long userId, String newLastName);

    void generateSecretToken(long userId);

    @VisibleForTesting
    void cleanSecretToken(long userId);

    void removeInactiveUserAccountsOlderThan(Duration acceptableAge);

    void logUnsuccessfulLoginAttempt(long userId);

    void resetUnsuccessfulLoginAttempts(long userId);

    void lockUser(long userId);

    void unlockUser(long userId);

    void setConsentToPrivacyPolicyDate(long userId, Date date);

    void setAccountRemovalRequestDate(long userId, Date date);

    void markUserAsDeleted(long userId);
}
