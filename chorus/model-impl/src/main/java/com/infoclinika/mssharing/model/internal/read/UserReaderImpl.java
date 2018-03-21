/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.read.UserReader;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;

/**
 * @author Stanislav Kurilin
 */
@Service
public class UserReaderImpl implements UserReader {
    @Inject
    private UserRepository repository;

    @Override
    public UserShortForm shortForm(long actor) {
        final User user = find(actor);
        return new UserShortForm(user.getId(),
                user.getFullName(),
                user.getEmail(),
                getLabNames(user));
    }

    @Override
    public AccountSettingsForm accountSettingsForm(long actor) {
        final User user = find(actor);
        return new AccountSettingsForm(
                user.getPersonData().getFirstName(),
                user.getPersonData().getLastName(),
                getLabNames(user),
                user.getEmail());
    }

    @Override
    public UserManagementTemplate.PersonInfo readPersonInfo(long actor) {
        final User user = find(actor);
        return new UserManagementTemplate.PersonInfo(user.getFirstName(), user.getLastName(), user.getEmail());
    }

    private User find(long id) {
        return checkNotNull(repository.findOne(id), "Couldn't find user with id %s", id);
    }

    private static ImmutableSet<String> getLabNames(User user) {
        return from(user.getLabs()).transform(new Function<Lab, String>(){
            @Override
            public String apply(Lab input) {
                return input.getName();
            }
        }).toSet();
    }
}
