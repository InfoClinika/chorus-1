package com.infoclinika.mssharing.services.jobs;

import com.infoclinika.mssharing.model.write.IssuesService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Vladislav Kovchug
 */
@Service
public class UpdateIssuesStatusesJob {
    private final IssuesService issuesService;

    @Inject
    public UpdateIssuesStatusesJob(IssuesService issuesService) {
        this.issuesService = issuesService;
    }

    /**
     * Scheduled task to update issues status.
     * Default cron rate - two hours.
     */
    @Scheduled(fixedRateString = "${scheduled.issues.status.update.rate:7200000}")
    public void updateIssuesStatus() {
        issuesService.updateIssuesStatus();
    }
}
