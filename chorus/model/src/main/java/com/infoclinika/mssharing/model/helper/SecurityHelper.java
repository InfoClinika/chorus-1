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


import com.infoclinika.mssharing.platform.model.helper.SecurityHelperTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stanislav Kurilin
 */
@Transactional(readOnly = true)
public interface SecurityHelper extends SecurityHelperTemplate<SecurityHelper.UserDetails> {

    boolean isBillingEnabledForLab(long lab);

    boolean isFeatureEnabledForLab(String feature, long lab);

    @Nullable
    UserDetails getUserDetailsByEmailAndSecretToken(String email, String userSecretKey);

    class UserDetails extends SecurityHelperTemplate.UserDetails {

        public final boolean labHead;
        @Nullable
        public final String secretToken;
        @Nullable
        public final Date emailVerificationSentOnDate;
        @Nullable
        public final Date passwordResetSentOnDate;
        public final boolean locked;

        public UserDetails(long id,
                           String firstName,
                           String lastName,
                           String email,
                           String password,
                           boolean admin,
                           boolean emailVerified,
                           String emailRequest,
                           Date lastModification,
                           Set<Long> labs,
                           boolean labHead,
                           String secretToken,
                           Date emailVerificationSentOnDate, Date passwordResetSentOnDate, boolean locked) {
            super(id, firstName, lastName, email, password, admin, emailVerified, lastModification, labs, emailRequest);
            this.labHead = labHead;
            this.secretToken = secretToken;
            this.emailVerificationSentOnDate = emailVerificationSentOnDate;
            this.passwordResetSentOnDate = passwordResetSentOnDate;
            this.locked = locked;
        }

        public UserDetails(long id,
                           String firstName,
                           String lastName,
                           String email,
                           String password, boolean locked) {
            super(id, firstName, lastName, email, password, false, false, null, new HashSet<>(), null);
            this.locked = locked;
            this.labHead = false;
            this.secretToken = "";
            this.emailVerificationSentOnDate = null;
            this.passwordResetSentOnDate = null;
        }

        @Override
        public String toString() {
            return "UserDetails{" +
                "labHead=" + labHead +
                ", secretToken='" + secretToken + '\'' +
                ", emailVerificationSentOnDate=" + emailVerificationSentOnDate +
                ", passwordResetSentOnDate=" + passwordResetSentOnDate +
                ", locked=" + locked +
                "} " + super.toString();
        }
    }
}
