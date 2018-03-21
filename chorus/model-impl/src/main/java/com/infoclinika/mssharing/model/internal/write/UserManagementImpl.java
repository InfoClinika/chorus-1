/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultUserManagement;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRequestRepositoryTemplate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Stanislav Kurilin
 */
@Service("userManagement")
@Transactional
public class UserManagementImpl extends DefaultUserManagement<User, Lab> implements UserManagement {
    private static final Logger LOG = Logger.getLogger(UserManagementImpl.class);
    @Inject
    private UserRepository userRepository;
    @Inject
    private UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>> userLabMembershipRequestRepository;
    @Inject
    private Provider<Date> current;
    @Inject
    private SecretTokenGenerator secretTokenGenerator;

    @Value("${max.login.attempts}")
    private int maxLoginAttempts;

    @Override
    public long createPersonAndApproveMembership(PersonInfo user, String password, Long lab, String emailVerificationUrl) {
        return createPersonAndApproveMembership(user, password, newHashSet(lab), emailVerificationUrl);
    }

    @Override
    public void updatePersonAndApproveMembership(long userId, PersonInfo user, Set<Long> labs) {
        updatePerson(userId, user, labs);
        final List<UserLabMembershipRequestTemplate<User, Lab>> requests = userLabMembershipRequestRepository.findPendingByUser(userId);
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
            LOG.warn("Secret Token for user: " + userId + " has been already generated. Skipping generation.");
            return;
        }
        final String secretToken = secretTokenGenerator.generate();
        user.setSecretToken(secretToken);
        saveUser(user);
    }

    @Override
    public void cleanSecretToken(long userId) {
        LOG.warn("Removing secret token. Should be invoked only in test environment.");
        final User user = findUser(userId);
        user.setSecretToken(null);
        saveUser(user);
    }

    @Override
    public void removeInactiveUserAccountsOlderThan(Duration acceptableAge) {
        final List<User> withEmailUnverified = userRepository.findWithEmailUnverified();
        final List<User> toDelete = withEmailUnverified.stream().filter(user -> {

            final Duration userRecordAge = Duration.between(user.getEmailVerificationSentOnDate().toInstant(), Instant.now());

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
        LOG.info("### Unsuccessful login attempt made by user with id: " + userId);
        final User user = userRepository.findOne(userId);
        user.setUnsuccessfulLoginAttempts(user.getUnsuccessfulLoginAttempts() + 1);

        if (user.getUnsuccessfulLoginAttempts() == maxLoginAttempts) {
            lockUser(user.getId());
        }

        userRepository.save(user);
    }

    @Override
    public void resetUnsuccessfulLoginAttempts(long userId) {
        LOG.info("### Resetting unsuccessful login attempts for user with id: " + userId);
        final User user = userRepository.findOne(userId);
        user.setUnsuccessfulLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    public void lockUser(long userId) {
        LOG.info("### Locking user with id: " + userId);
        setUserLocked(userId, true);
    }

    @Override
    public void unlockUser(long userId) {
        LOG.info("### Unlocking user with id: " + userId);
        setUserLocked(userId, false);
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
}
