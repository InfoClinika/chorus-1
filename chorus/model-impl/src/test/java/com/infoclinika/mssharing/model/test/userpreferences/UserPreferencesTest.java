package com.infoclinika.mssharing.model.test.userpreferences;

import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.read.UserPreferencesReader.UserPreferencesInfo;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Orlov, Andrii Loboda
 */
public class UserPreferencesTest extends AbstractTest {

    @Test
    public void readUserPreferencesForNewUserIfBillingEnabled() {
        setBilling(true);

        final long bob = uc.createLab3AndBob();
        final UserPreferencesInfo userPreferences = userPreferencesReader.readUserPreferences(bob);
        assertEquals(
            userPreferences.shouldShowBillingNotification,
            true,
            "Billing Notification is not shown for new user, but billing feature is enabled"
        );
    }

    @Test
    public void readUserPreferencesForNewUserIfBillingDisabled() {
        setBilling(false);

        final long bob = uc.createLab3AndBob();
        final UserPreferencesInfo userPreferences = userPreferencesReader.readUserPreferences(bob);
        assertEquals(
            userPreferences.shouldShowBillingNotification,
            false,
            "Billing Notification is shown despite the fact that billing feature is disabled"
        );
    }

    @Test
    public void disableBillingNotification() {
        final long bob = uc.createLab3AndBob();
        userPreferencesManagement.removeBillingNotification(bob);
        final UserPreferencesInfo userPreferences = userPreferencesReader.readUserPreferences(bob);
        assertEquals(userPreferences.shouldShowBillingNotification, false,
            "Billing Notification is still shown for user, it should be disabled, though"
        );
    }

}
