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
package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

/**
 * @author Stanislav Kurilin
 */
@Transactional(readOnly = true)
public interface UserReader {
    UserShortForm shortForm(long actor);

    AccountSettingsForm accountSettingsForm(long actor);

    UserManagementTemplate.PersonInfo readPersonInfo(long actor);

    Date readConsentToPrivacyPolicyDate(long actor);

    Date readAccountRemovalRequestDate(long actor);

    class UserShortForm {
        public final long id;
        public final String name;
        public final String email;
        public final ImmutableSet<String> units;

        public UserShortForm(long id, String name, String email, Set<String> units) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.units = ImmutableSet.copyOf(units);
        }

        public UserManagementTemplate.PersonInfo toPersonInfo() {
            return new UserManagementTemplate.PersonInfo(name, name, email);
        }
    }

    class AccountSettingsForm {
        public final String firstName;
        public final String lastName;
        public final ImmutableSet<String> laboratories;
        public final String email;

        public AccountSettingsForm(String firstName, String lastName, Set<String> laboratories, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.laboratories = ImmutableSet.copyOf(laboratories);
            this.email = email;
        }
    }
}
