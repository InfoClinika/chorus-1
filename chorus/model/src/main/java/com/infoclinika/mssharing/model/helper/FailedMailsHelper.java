package com.infoclinika.mssharing.model.helper;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author Herman Zamula
 */
public interface FailedMailsHelper {

    Set<Long> handleFailedEmails(String bounceType, String bounceSubType, String timestamp, Set<FailedEmailItem> failedEmails, String rawJson);

    class FailedEmailItem {
        public final String email;
        @Nullable
        public final String reason;

        public FailedEmailItem(String email, String reason) {
            this.email = email;
            this.reason = reason;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FailedEmailItem that = (FailedEmailItem) o;

            return email != null ? email.equals(that.email) : that.email == null;

        }

        @Override
        public int hashCode() {
            return email != null ? email.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "FailedEmailItem{" +
                    "email='" + email + '\'' +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }

}
