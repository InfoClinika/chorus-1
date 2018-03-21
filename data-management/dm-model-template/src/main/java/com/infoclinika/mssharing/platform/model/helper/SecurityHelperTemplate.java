package com.infoclinika.mssharing.platform.model.helper;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Set;

/**
 * @author : Alexander Serebriyan
 */

public interface SecurityHelperTemplate<USER_DETAILS extends SecurityHelperTemplate.UserDetails> {

    @Nullable
    USER_DETAILS getUserDetailsByEmail(String email);

    USER_DETAILS getUserDetails(long id);

    USER_DETAILS getUserDetailsByInvitationLink(String link);

    class UserDetails extends UserManagementTemplate.PersonInfo {
        public final String password;
        public final long id;
        public final boolean admin;
        public final boolean emailVerified;
        public final Date lastModification;
        public final ImmutableSet<Long> labs;
        public final String emailRequest;

        public UserDetails(long id, String firstName, String lastName, String email, String password, boolean admin, boolean emailVerified, Date lastModification, Set<Long> labs, String emailRequest) {
            super(firstName, lastName, email);
            this.id = id;
            this.password = password;
            this.admin = admin;
            this.emailVerified = emailVerified;
            this.lastModification = lastModification;
            this.emailRequest = emailRequest;
            this.labs = ImmutableSet.copyOf(labs);
        }
    }
}
