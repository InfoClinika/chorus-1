package com.infoclinika.mssharing.model.read;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alexander Orlov
 */
@Transactional(readOnly = true)
public interface UserPreferencesReader {

    UserPreferencesInfo readUserPreferences(long actor);

    public static class UserPreferencesInfo{

        public long userId;
        public boolean shouldShowBillingNotification;

        public UserPreferencesInfo(long userId, boolean shouldShowBillingNotification) {
            this.userId = userId;
            this.shouldShowBillingNotification = shouldShowBillingNotification;
        }
    }
}
