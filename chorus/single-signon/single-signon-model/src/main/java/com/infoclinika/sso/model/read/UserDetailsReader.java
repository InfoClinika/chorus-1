package com.infoclinika.sso.model.read;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.infoclinika.sso.model.ApplicationType;

import java.util.Set;

/**
 * @author andrii.loboda
 */
public interface UserDetailsReader {
    Optional<UserDetails> getDetails(ApplicationType applicationType, String username);

    Optional<UserDetails> getDetails(long user);

    class UserDetails {
        public final long ID;
        public final boolean linked;
        public final Set<ApplicationCredential> credentials;

        public UserDetails(long id, boolean linked, Set<ApplicationCredential> credentials) {
            this.ID = id;
            this.linked = linked;
            this.credentials = credentials;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("ID", ID)
                .add("linked", linked)
                .add("credentials", credentials)
                .toString();
        }

        public static class ApplicationCredential {
            public final ApplicationType applicationType;
            public final String username;
            public final String secretToken;

            public ApplicationCredential(ApplicationType applicationType, String username, String secretToken) {
                this.applicationType = applicationType;
                this.username = username;
                this.secretToken = secretToken;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                ApplicationCredential that = (ApplicationCredential) o;
                return Objects.equal(applicationType, that.applicationType) &&
                    Objects.equal(username, that.username) &&
                    Objects.equal(secretToken, that.secretToken);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(applicationType, username, secretToken);
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                    .add("applicationType", applicationType)
                    .add("username", username)
                    .toString();
            }
        }

    }
}
