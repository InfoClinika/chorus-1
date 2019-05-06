package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.internal.helper.AccountRemovalHelper;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author timofei.kasianov 7/23/18
 */
@Service
public class AccountRemovalJob {
    @Inject
    private AccountRemovalHelper accountRemovalHelper;

    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    /**
     * Scheduled task to process user removal request.
     * Default cron delay - one hour.
     */
    @Scheduled(fixedDelayString = "${scheduled.account.removal.delay:3600000}")
    public void handleUserAccountsWithRequestedRemoval() {
        final long delay = chorusPropertiesProvider.getUserAccountRemovalDelay();
        accountRemovalHelper.removeUsersWithRemovalRequestOlderThan(delay);
    }
}
