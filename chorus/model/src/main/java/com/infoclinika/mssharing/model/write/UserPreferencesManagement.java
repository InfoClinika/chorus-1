package com.infoclinika.mssharing.model.write;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alexander Orlov
 */
@Transactional
public interface UserPreferencesManagement {

    void removeBillingNotification(long userId);
}
