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
package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.repository.RestTokenRepository;
import com.infoclinika.mssharing.model.internal.repository.UserPreferencesRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.entity.ChangeEmailRequest;
import com.infoclinika.mssharing.platform.entity.PersonData;
import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultUserManagement;
import com.infoclinika.mssharing.platform.repository.ChangeEmailRequestRepository;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRequestRepositoryTemplate;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.Subscriptions.Subscription.Status.NOT_SUBSCRIBED;

/**
 * @author Stanislav Kurilin
 */
@Service("userManagement")
@Transactional
public class UserManagementImpl extends DefaultUserManagement<User, Lab> implements UserManagement {
    private static final String STRING_EMPTY_VALUE = "";
    private static final String STRING_NO_VALUE = null;
    private static final String REMOVED_USER_EMAIL_PREFIX = "Removed ";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementImpl.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>>
        userLabMembershipRequestRepository;

    @Inject
    private Provider<Date> current;

    @Inject
    private SecretTokenGenerator secretTokenGenerator;

    @Inject
    private Notifier notifier;

    @Inject
    private RestTokenRepository restTokenRepository;

    @Inject
    private ChangeEmailRequestRepository changeEmailRequestRepository;

    @Inject
    private UserPreferencesRepository userPreferencesRepository;

    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    @Override
    public long createPersonAndApproveMembership(PersonInfo user,
                                                 String password,
                                                 Long lab,
                                                 String emailVerificationUrl) {
        return createPersonAndApproveMembership(user, password, newHashSet(lab), emailVerificationUrl);
    }

    @Override
    public void updatePersonAndApproveMembership(long userId, PersonInfo user, Set<Long> labs) {
        updatePerson(userId, user, labs);
        final List<UserLabMembershipRequestTemplate<User, Lab>> requests =
            userLabMembershipRequestRepository.findPendingByUser(userId);
        for (UserLabMembershipRequestTemplate request : requests) {
            approveLabMembershipRequest(request.getLab().getHead().getId(), request.getId());
        }
    }

    @Override
    public void changeFirstName(long userId, String newFirstName) {
        final User user = findUser(userId);
        user.setFirstName(newFirstName);
        saveUser(user);
    }

    @Override
    public void changeLastName(long userId, String newLastName) {
        final User user = findUser(userId);
        user.setLastName(newLastName);
        saveUser(user);
    }

    @Override
    public void generateSecretToken(long userId) {
        final User user = findUser(userId);
        if (user.getSecretToken() != null) {
            LOGGER.warn("Secret Token for user: {} has been already generated. Skipping generation.", userId);
            return;
        }
        final String secretToken = secretTokenGenerator.generate();
        user.setSecretToken(secretToken);
        saveUser(user);
    }

    @Override
    public void cleanSecretToken(long userId) {
        LOGGER.warn("Removing secret token. Should be invoked only in test environment.");
        final User user = findUser(userId);
        user.setSecretToken(null);
        saveUser(user);
    }

    @Override
    public void removeInactiveUserAccountsOlderThan(Duration acceptableAge) {
        final List<User> withEmailUnverified = userRepository.findWithEmailUnverified();
        final List<User> toDelete = withEmailUnverified.stream().filter(user -> {

            final Duration userRecordAge =
                Duration.between(user.getEmailVerificationSentOnDate().toInstant(), Instant.now());

            // check if greater than allowed
            return userRecordAge.compareTo(acceptableAge) == 1;
        }).collect(Collectors.toList());

        userRepository.delete(toDelete);
    }

    @Override
    public void resendActivationEmail(long userId, String emailVerificationUrl) {
        super.resendActivationEmail(userId, emailVerificationUrl);
        final User user = userRepository.findOne(userId);
        user.resetEmailVerificationDate();
        userRepository.save(user);
    }

    @Override
    public void sendPasswordRecoveryInstructions(long userId, String passwordRecoveryUrl) {
        super.sendPasswordRecoveryInstructions(userId, passwordRecoveryUrl);
        final User user = userRepository.findOne(userId);
        user.resetPasswordResetDate();
        userRepository.save(user);
    }

    @Override
    public void logUnsuccessfulLoginAttempt(long userId) {
        LOGGER.info("### Unsuccessful login attempt made by user with id: {}", userId);
        final User user = userRepository.findOne(userId);
        user.setUnsuccessfulLoginAttempts(user.getUnsuccessfulLoginAttempts() + 1);

        if (user.getUnsuccessfulLoginAttempts() == chorusPropertiesProvider.getMaxLoginAttempts()) {
            lockUser(user.getId());
        }

        userRepository.save(user);
    }

    @Override
    public void resetUnsuccessfulLoginAttempts(long userId) {
        LOGGER.info("### Resetting unsuccessful login attempts for user with id: {}", userId);
        final User user = userRepository.findOne(userId);
        user.setUnsuccessfulLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    public void lockUser(long userId) {
        LOGGER.info("### Locking user with id: {}", userId);
        setUserLocked(userId, true);
    }

    @Override
    public void unlockUser(long userId) {
        LOGGER.info("### Unlocking user with id: {}", userId);
        setUserLocked(userId, false);
    }

    @Override
    public void setConsentToPrivacyPolicyDate(long userId, Date date) {
        final User user = findUser(userId);
        user.setConsentToPrivacyPolicyDate(date);
        saveUser(user);
    }

    @Override
    public void setAccountRemovalRequestDate(long userId, Date date) {

        final User user = findUser(userId);
        user.setAccountRemovalRequestDate(date);
        saveUser(user);

        if (date != null) {
            notifier.sendAccountRemovalRequestedNotification(userId, date);
        } else {
            notifier.sendAccountRemovalRevokedNotification(userId, new Date());
        }
    }

    @Override
    public void markUserAsDeleted(long userId) {
        LOGGER.info("### Mark user as deleted. User ID: {}", userId);
        final User user = findUser(userId);
        final String userEmail = user.getEmail();
        removeReferences(user);
        erasePersonalInfo(user);
        saveUser(user);
        notifier.sendAccountRemovedNotification(userEmail);
    }

    private void setUserLocked(long userId, boolean locked) {
        final User user = userRepository.findOne(userId);
        user.setLocked(locked);
        userRepository.save(user);
    }

    private User findUser(long userId) {
        return checkNotNull(userRepository.findOne(userId), "Couldn't find user with id %s", userId);
    }

    private long saveUser(User user) {
        return saveAndGetUser(user).getId();
    }

    private User saveAndGetUser(User user) {
        user.setLastModification(current.get());
        return userRepository.save(user);
    }

    private void erasePersonalInfo(User user) {
        final String email = REMOVED_USER_EMAIL_PREFIX + UUID.randomUUID().toString();
        final PersonData emptyPersonData = new PersonData(
            email,
            STRING_EMPTY_VALUE,
            STRING_EMPTY_VALUE
        );
        user.setPersonData(emptyPersonData);
        user.setSecretToken(STRING_NO_VALUE);
        user.setClientToken(STRING_NO_VALUE);
        user.setPasswordHash(STRING_EMPTY_VALUE);
        user.setLocked(true);
        user.setDeleted(true);
    }

    private void removeReferences(User user) {

        final RestToken restToken = user.getRestToken();

        if (restToken != null) {
            restTokenRepository.delete(restToken);
            user.setRestToken(null);
        }

        final Subscription subscription = user.getSubscription();

        if (subscription.getStatus() != NOT_SUBSCRIBED) {
            subscription.setStatus(NOT_SUBSCRIBED);
            subscription.setLastStatusChange(new Date());
        }

        final ChangeEmailRequest changeEmailRequest = user.getChangeEmailRequest();

        if (changeEmailRequest != null) {
            changeEmailRequestRepository.delete(changeEmailRequest);
            user.setChangeEmailRequest(null);
        }

        final UserPreferences preferences = userPreferencesRepository.findByUserId(user.getId());

        if (preferences != null) {
            userPreferencesRepository.delete(preferences);
        }
    }
}
