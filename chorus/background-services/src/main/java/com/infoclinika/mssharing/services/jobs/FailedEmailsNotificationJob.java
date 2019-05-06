package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.internal.FailedEmailsSnsNotificationHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
public class FailedEmailsNotificationJob {

    @Inject
    private FailedEmailsSnsNotificationHandler failedEmailsSnsNotificationHandler;

    /**
     * Scheduled task to handle failed emails notification.
     * Default cron rate - five minutes.
     */
    @Scheduled(fixedRateString = "${scheduled.failed.emails.notification.rate:300000}")
    public void handle() {
        if (failedEmailsSnsNotificationHandler.handlingIsEnabled()) {
            failedEmailsSnsNotificationHandler.handleMessages();
        }
    }
}
