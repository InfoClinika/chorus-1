package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.entity.UserPreferences;
import com.infoclinika.mssharing.model.internal.repository.UserPreferencesRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.write.UserPreferencesManagement;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexander Orlov
 */
@Service
public class UserPreferencesManagementImpl implements UserPreferencesManagement {

    @Inject
    private UserPreferencesRepository userPreferencesRepository;
    @Inject
    private UserRepository userRepository;

    @Override
    public void removeBillingNotification(long actor) {
        UserPreferences userPreferences = userPreferencesRepository.findByUserId(actor);
        if (userPreferences == null) {
            userPreferences = new UserPreferences(userRepository.findOne(actor), false);
        } else {
            userPreferences.setShowBillingNotification(false);
        }
        userPreferencesRepository.save(userPreferences);
    }
}
