package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.internal.entity.UserPreferences;
import com.infoclinika.mssharing.model.internal.repository.UserPreferencesRepository;
import com.infoclinika.mssharing.model.read.UserPreferencesReader;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.infoclinika.mssharing.model.features.ApplicationFeature.BILLING;

/**
 * @author Alexander Orlov
 */
@Component
public class UserPreferencesReaderImpl implements UserPreferencesReader {

    @Inject
    private UserPreferencesRepository userPreferencesRepository;
    @Inject
    private FeaturesHelper featuresHelper;

    public static final Function<UserPreferences, UserPreferencesInfo> USER_PREFERENCES_TRANSFORMER =
        new Function<UserPreferences, UserPreferencesInfo>() {

            @Override
            public UserPreferencesInfo apply(UserPreferences userPreferences) {
                return new UserPreferencesInfo(
                    userPreferences.getUser().getId(),
                    userPreferences.isShowBillingNotification()
                );
            }
        };


    @Override
    public UserPreferencesInfo readUserPreferences(long actor) {
        final UserPreferences userPreferences = userPreferencesRepository.findByUserId(actor);
        if (userPreferences == null) {
            final boolean shouldShowBillingNotification = featuresHelper.isEnabled(BILLING);
            return new UserPreferencesInfo(actor, shouldShowBillingNotification);
        } else {
            return USER_PREFERENCES_TRANSFORMER.apply(userPreferences);
        }
    }
}
